package com.antkorwin.statemachineutils.service;

import com.antkorwin.commonutils.exceptions.NotFoundException;
import com.antkorwin.commonutils.validation.GuardCheck;
import com.antkorwin.statemachineutils.config.Events;
import com.antkorwin.statemachineutils.config.StateMachineConfig;
import com.antkorwin.statemachineutils.config.States;
import com.antkorwin.statemachineutils.persist.CustomStateMachinePersister;
import com.antkorwin.statemachineutils.persist.PersisterErrorInfo;
import com.antkorwin.statemachineutils.wrapper.EnableStateMachineWrapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * Created by Korovin Anatolii on 09.07.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@Import({StateMachineConfig.class})
@EnableStateMachineWrapper
public class XStateMachineServiceImplTest {

    private static final UUID PERSISTED_ID = UUID.randomUUID();

    @Autowired
    private XStateMachineService<States, Events> xStateMachineService;

    @Autowired
    private StateMachinePersist<States, Events, UUID> persist;

    @Autowired
    private StateMachineFactory<States, Events> factory;

    @Autowired
    private StateMachinePersister<States, Events, UUID> persister;

    private StateMachine<States, Events> mockMachine;

    @PostConstruct
    public void setUp() throws Exception {
        mockMachine = factory.getStateMachine(PERSISTED_ID.toString());
        persister.persist(mockMachine, PERSISTED_ID);
    }


    @Test
    public void diTest() {
        // Arrange
        // Act
        Assertions.assertThat(xStateMachineService).isNotNull();
        // Asserts
    }

    @Test
    public void testCreate() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        StateMachine<States, Events> machine = xStateMachineService.create(id);

        // Asserts
        Assertions.assertThat(machine).isNotNull();
        Assertions.assertThat(machine.getId()).isEqualTo(id.toString());
    }

    @Test
    public void testPersistAfterCreate() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();

        // Check precondition
        Assertions.assertThat(persist.read(id)).isNull();

        // Act
        xStateMachineService.create(id);

        // Asserts
        StateMachineContext<States, Events> persistedMachine = persist.read(id);
        Assertions.assertThat(persistedMachine.getId()).isEqualTo(id.toString());
    }

    //TODO: check getting from a factory and updating machine after create from service
    // I predict that a factory may return a same value as we return from create


    @Test
    public void testGet() {
        // Arrange
        // Act
        StateMachine<States, Events> machine = xStateMachineService.get(PERSISTED_ID);

        // Asserts
        Assertions.assertThat(machine.getId()).isEqualTo(mockMachine.getId());
        //TODO: assert an equality of machines in more details
    }

    @Test
    public void testGetWithWrongId() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        GuardCheck.check(() -> xStateMachineService.get(id),
                         NotFoundException.class,
                         PersisterErrorInfo.COULD_NOT_READ_STATEMACHINE_FROM_PERSIST);
    }

    @Test
    public void testEvaluate() {
        // Arrange
        // Act
        States initialState =
                xStateMachineService.evaluateWithRollback(PERSISTED_ID,
                                                          stateMachine -> stateMachine.getState().getId());

        // Asserts
        Assertions.assertThat(initialState).isEqualTo(States.BACKLOG);
    }

    @Test
    public void testEvaluateWithChangeState() {

        // Act
        StateMachine<States, Events> machine = xStateMachineService.evaluateWithRollback(PERSISTED_ID, stateMachine -> {
            stateMachine.sendEvent(Events.START_FEATURE);
            return stateMachine;
        });

        // Read a result from the storage
        StateMachine<States, Events> persistedMachine = xStateMachineService.get(PERSISTED_ID);

        // Asserts
        assertThatMachinesEqual(machine, persistedMachine);
    }

    @Test
    public void testUpdateBehavior() {
        // Arrange
        StateMachine<States, Events> machine = xStateMachineService.get(PERSISTED_ID);
        machine.sendEvent(Events.START_FEATURE);
        // Check precondition
        Assertions.assertThat(machine.getState().getId()).isEqualTo(States.IN_PROGRESS);

        // Act
        StateMachine<States, Events> returnedMachine = xStateMachineService.update(PERSISTED_ID, machine);

        // Asserts
        Assertions.assertThat(returnedMachine).isNotNull();
        Assertions.assertThat(returnedMachine.getState().getId()).isEqualTo(States.IN_PROGRESS);
        // Check a machine in the storage
        StateMachine<States, Events> machineAfterCommit = xStateMachineService.get(PERSISTED_ID);
        Assertions.assertThat(machineAfterCommit.getState().getId()).isEqualTo(States.IN_PROGRESS);
    }

    @Test
    public void testUpdateInternal() throws Exception {
        // Arrange
        StateMachine<States, Events> machine = xStateMachineService.get(PERSISTED_ID);
        machine.sendEvent(Events.START_FEATURE);
        StateMachineContext<States, Events> machineContext =
                CustomStateMachinePersister.buildStateMachineContext(machine);

        // Act
        xStateMachineService.update(PERSISTED_ID, machine);

        // Asserts
        StateMachineContext<States, Events> actualContext = persist.read(PERSISTED_ID);
        Assertions.assertThat(actualContext).isEqualToComparingFieldByFieldRecursively(machineContext);
    }

    private void assertThatMachinesEqual(StateMachine<States, Events> firstMachine,
                                         StateMachine<States, Events> secondMachine) {
        // Arrange
        StateMachineContext<States, Events> firstContext = CustomStateMachinePersister
                .buildStateMachineContext(firstMachine);
        StateMachineContext<States, Events> secondContext = CustomStateMachinePersister
                .buildStateMachineContext(secondMachine);
        // Assert
        Assertions.assertThat(firstContext).isEqualToComparingFieldByFieldRecursively(secondContext);
    }
}