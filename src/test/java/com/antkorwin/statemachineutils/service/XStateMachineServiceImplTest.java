package com.antkorwin.statemachineutils.service;

import com.antkorwin.commonutils.exceptions.NotFoundException;
import com.antkorwin.commonutils.validation.GuardCheck;
import com.antkorwin.statemachineutils.TransactionalTestConfig;
import com.antkorwin.statemachineutils.config.Events;
import com.antkorwin.statemachineutils.config.StateMachineConfig;
import com.antkorwin.statemachineutils.config.States;
import com.antkorwin.statemachineutils.persist.PersisterErrorInfo;
import com.antkorwin.statemachineutils.persist.StateMachineContextEvaluator;
import com.antkorwin.statemachineutils.wrapper.EnableStateMachineWrapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created on 09.07.2018.
 *
 * @author Korovin Anatoliy
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@Import({StateMachineConfig.class, TransactionalTestConfig.class})
@EnableStateMachineWrapper
public class XStateMachineServiceImplTest {

    private static final String PERSISTED_MACHINE_ID = UUID.randomUUID().toString();

    @Autowired
    private XStateMachineService<States, Events> xStateMachineService;

    @Autowired
    private StateMachinePersist<States, Events, String> persist;

    @Autowired
    private StateMachineFactory<States, Events> factory;

    @Autowired
    private StateMachinePersister<States, Events, String> persister;

    private StateMachine<States, Events> mockMachine;

    @Autowired
    private TransactionalTestConfig.TestService testService;

    @Before
    public void setUp() throws Exception {
        testService.clear();

        mockMachine = factory.getStateMachine(PERSISTED_MACHINE_ID.toString());
        persister.persist(mockMachine, PERSISTED_MACHINE_ID);
    }


    @Test
    public void diTest() {
        // Arrange
        // Act
        assertThat(xStateMachineService).isNotNull();
        // Asserts
    }

    @Test
    public void testCreate() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        StateMachine<States, Events> machine = xStateMachineService.create(id.toString());

        // Asserts
        assertThat(machine).isNotNull();
        assertThat(machine.getId()).isEqualTo(id.toString());
    }

    @Test
    public void testPersistAfterCreate() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();

        // Check precondition
        assertThat(persist.read(id.toString())).isNull();

        // Act
        xStateMachineService.create(id.toString());

        // Asserts
        StateMachineContext<States, Events> persistedMachine = persist.read(id.toString());
        assertThat(persistedMachine.getId()).isEqualTo(id.toString());
    }

    //TODO: check getting from a factory and updating machine after create from service
    // I predict that a factory may return a same value as we return from create


    @Test
    public void testGet() {
        // Arrange
        // Act
        StateMachine<States, Events> machine = xStateMachineService.get(PERSISTED_MACHINE_ID);

        // Asserts
        assertThat(machine.getId()).isEqualTo(mockMachine.getId());
        //TODO: assert an equality of machines in more details
    }

    @Test
    public void testExist() {
        // Act & assert
        assertThat(xStateMachineService.isExist(PERSISTED_MACHINE_ID)).isTrue();
    }

    @Test
    public void testNotExist() {
        // Act & assert
        assertThat(xStateMachineService.isExist(UUID.randomUUID().toString())).isFalse();
    }

    @Test
    public void testGetWithWrongId() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        GuardCheck.check(() -> xStateMachineService.get(id.toString()),
                         NotFoundException.class,
                         PersisterErrorInfo.COULD_NOT_READ_STATEMACHINE_FROM_PERSIST);
    }

    @Test
    public void testUpdateBehavior() {
        // Arrange
        StateMachine<States, Events> machine = xStateMachineService.get(PERSISTED_MACHINE_ID);
        machine.sendEvent(Events.START_FEATURE);
        // Check precondition
        assertThat(machine.getState().getId()).isEqualTo(States.IN_PROGRESS);

        // Act
        StateMachine<States, Events> returnedMachine = xStateMachineService.update(PERSISTED_MACHINE_ID, machine);

        // Asserts
        assertThat(returnedMachine).isNotNull();
        assertThat(returnedMachine.getState().getId()).isEqualTo(States.IN_PROGRESS);
        // Check a machine in the storage
        StateMachine<States, Events> machineAfterCommit = xStateMachineService.get(PERSISTED_MACHINE_ID);
        assertThat(machineAfterCommit.getState().getId()).isEqualTo(States.IN_PROGRESS);
    }

    @Test
    public void testUpdateInternal() throws Exception {
        // Arrange
        StateMachine<States, Events> machine = xStateMachineService.get(PERSISTED_MACHINE_ID);
        machine.sendEvent(Events.START_FEATURE);
        StateMachineContext<States, Events> machineContext =
                StateMachineContextEvaluator.getContext(machine);

        // Act
        xStateMachineService.update(PERSISTED_MACHINE_ID, machine);

        // Asserts
        StateMachineContext<States, Events> actualContext = persist.read(PERSISTED_MACHINE_ID);
        assertThat(actualContext).isEqualToComparingFieldByFieldRecursively(machineContext);
    }

    @Test
    public void testEvaluate() {
        // Arrange
        // Act
        States initialState =
                xStateMachineService.evaluate(PERSISTED_MACHINE_ID,
                                              stateMachine -> stateMachine.getState().getId());

        // Asserts
        assertThat(initialState).isEqualTo(States.BACKLOG);
    }

    @Test
    public void testEvaluateWithChangeState() {

        // Act
        StateMachine<States, Events> machine = xStateMachineService.evaluate(PERSISTED_MACHINE_ID, stateMachine -> {
            stateMachine.sendEvent(Events.START_FEATURE);
            return stateMachine;
        });

        // Read a result from the storage
        StateMachine<States, Events> persistedMachine = xStateMachineService.get(PERSISTED_MACHINE_ID);

        // Asserts
        assertThatMachinesEqual(machine, persistedMachine);
    }

    @Test
    public void testRollbackInEvaluationWithError() {

        Function<StateMachine<States, Events>, String> func = stateMachine -> {
            stateMachine.sendEvent(Events.START_FEATURE);
            throw new NotFoundException(PersisterErrorInfo.COULD_NOT_READ_STATEMACHINE_FROM_PERSIST);
            //TODO: replace on a more relevant exception
        };

        // Act
        GuardCheck.check(() -> xStateMachineService.evaluate(PERSISTED_MACHINE_ID, func),
                         NotFoundException.class,
                         PersisterErrorInfo.COULD_NOT_READ_STATEMACHINE_FROM_PERSIST);


        // Read a result from the storage
        StateMachine<States, Events> persistedMachine = xStateMachineService.get(PERSISTED_MACHINE_ID);
        // Asserts
        assertThat(persistedMachine.getState().getId()).isEqualTo(States.BACKLOG);
    }

    /**
     * This case shows how to avoid an inconsistency while in a processing function
     * you try to call something with a side effect such as persist an intermediate state of SM.
     */
    @Test
    public void testRollbackInEvaluationWithErrorAndTryingToPersistMachineInProcessingFunction() {

        Function<StateMachine<States, Events>, String> processingFunc = stateMachine -> {
            stateMachine.sendEvent(Events.START_FEATURE);
            try {
                persister.persist(stateMachine, PERSISTED_MACHINE_ID);
            } catch (Exception e) {
                e.printStackTrace();
                Assertions.fail(e.getMessage());
            }
            throw new NotFoundException(PersisterErrorInfo.COULD_NOT_READ_STATEMACHINE_FROM_PERSIST);
            //TODO: replace on a more relevant exception
        };

        // Act
        GuardCheck.check(() -> xStateMachineService.evaluate(PERSISTED_MACHINE_ID, processingFunc),
                         NotFoundException.class,
                         PersisterErrorInfo.COULD_NOT_READ_STATEMACHINE_FROM_PERSIST);


        // Read a result from the storage
        StateMachine<States, Events> persistedMachine = xStateMachineService.get(PERSISTED_MACHINE_ID);
        // Asserts
        assertThat(persistedMachine.getState().getId()).isEqualTo(States.BACKLOG);
    }

    @Test
    public void testRetrieveAvailableEvents() {
        // Act
        List<Events> availableEvents = xStateMachineService.retrieveAvailableEvents(PERSISTED_MACHINE_ID);
        // Asserts
        assertThat(availableEvents)
                .containsOnly(Events.START_FEATURE,
                              Events.ROCK_STAR_DOUBLE_TASK,
                              Events.DEPLOY,
                              Events.INCREMENT);
    }

    private void assertThatMachinesEqual(StateMachine<States, Events> firstMachine,
                                         StateMachine<States, Events> secondMachine) {
        // Arrange
        StateMachineContext<States, Events> firstContext =
                StateMachineContextEvaluator.getContext(firstMachine);

        StateMachineContext<States, Events> secondContext =
                StateMachineContextEvaluator.getContext(secondMachine);

        // Assert
        assertThat(firstContext).isEqualToComparingFieldByFieldRecursively(secondContext);
    }
}