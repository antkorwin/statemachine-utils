package com.antkorwin.statemachineutils.service;

import com.antkorwin.commonutils.exceptions.NotFoundException;
import com.antkorwin.commonutils.validation.GuardCheck;
import com.antkorwin.statemachineutils.TransactionalTestConfig;
import com.antkorwin.statemachineutils.config.Events;
import com.antkorwin.statemachineutils.config.StateMachineConfig;
import com.antkorwin.statemachineutils.config.States;
import com.antkorwin.statemachineutils.persist.CustomStateMachinePersister;
import com.antkorwin.statemachineutils.persist.PersisterErrorInfo;
import com.antkorwin.statemachineutils.wrapper.EnableStateMachineWrapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Before;
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

import java.util.UUID;
import java.util.function.Function;

/**
 * Created by Korovin Anatolii on 09.07.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@Import({StateMachineConfig.class, TransactionalTestConfig.class})
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

    @Autowired
    private TransactionalTestConfig.TestService testService;

    @Before
    public void setUp() throws Exception {
        testService.clear();

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

    @Test
    public void testEvaluate() {
        // Arrange
        // Act
        States initialState =
                xStateMachineService.evaluate(PERSISTED_ID,
                                                          stateMachine -> stateMachine.getState().getId());

        // Asserts
        Assertions.assertThat(initialState).isEqualTo(States.BACKLOG);
    }

    @Test
    public void testEvaluateWithChangeState() {

        // Act
        StateMachine<States, Events> machine = xStateMachineService.evaluate(PERSISTED_ID, stateMachine -> {
            stateMachine.sendEvent(Events.START_FEATURE);
            return stateMachine;
        });

        // Read a result from the storage
        StateMachine<States, Events> persistedMachine = xStateMachineService.get(PERSISTED_ID);

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
        GuardCheck.check(() -> xStateMachineService.evaluate(PERSISTED_ID, func),
                         NotFoundException.class,
                         PersisterErrorInfo.COULD_NOT_READ_STATEMACHINE_FROM_PERSIST);


        // Read a result from the storage
        StateMachine<States, Events> persistedMachine = xStateMachineService.get(PERSISTED_ID);
        // Asserts
        Assertions.assertThat(persistedMachine.getState().getId()).isEqualTo(States.BACKLOG);
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
                persister.persist(stateMachine, PERSISTED_ID);
            } catch (Exception e) {
                e.printStackTrace();
                Assertions.fail(e.getMessage());
            }
            throw new NotFoundException(PersisterErrorInfo.COULD_NOT_READ_STATEMACHINE_FROM_PERSIST);
            //TODO: replace on a more relevant exception
        };

        // Act
        GuardCheck.check(() -> xStateMachineService.evaluate(PERSISTED_ID, processingFunc),
                         NotFoundException.class,
                         PersisterErrorInfo.COULD_NOT_READ_STATEMACHINE_FROM_PERSIST);


        // Read a result from the storage
        StateMachine<States, Events> persistedMachine = xStateMachineService.get(PERSISTED_ID);
        // Asserts
        Assertions.assertThat(persistedMachine.getState().getId()).isEqualTo(States.BACKLOG);
    }



    @Test
    public void testEvaluateTransactionalWithChangeStateAndFailOnTransactionCommit() {

        // Act
        Exception actualException = null;
        try {
            StateMachine<States, Events> machine = xStateMachineService
                    .evaluateWithTransactionalRollback(PERSISTED_ID, stateMachine -> {
                        stateMachine.sendEvent(Events.START_FEATURE);
                        testService.ok();
                        testService.fail();
                        return stateMachine;
                    });
        } catch (Exception e) {
            actualException = e;
        }

        // Asserts
        Assertions.assertThat(actualException.getMessage())
                  .contains("not-null property references a null or transient value");
        Assertions.assertThat(actualException)
                  .isInstanceOf(DataIntegrityViolationException.class);

        // Read a result from the storage
        StateMachine<States, Events> machine = xStateMachineService.get(PERSISTED_ID);

        Assertions.assertThat(machine.getState().getId()).isEqualTo(States.BACKLOG);

        // Check that entity not save in database
        Assertions.assertThat(testService.size()).isEqualTo(0);
    }

    @Test
    public void testEvaluateTransactionalSuccess() {

        // Act
        StateMachine<States, Events> machine = xStateMachineService
                .evaluateWithTransactionalRollback(PERSISTED_ID, stateMachine -> {
                    stateMachine.sendEvent(Events.START_FEATURE);
                    testService.ok();
                    return stateMachine;
                });

        // Read a machine from the storage
        StateMachine<States, Events> persistedMachine = xStateMachineService.get(PERSISTED_ID);

        // Asserts
        Assertions.assertThat(machine.getState().getId())
                  .isEqualTo(States.IN_PROGRESS);

        assertThatMachinesEqual(persistedMachine, machine);

        // Check that entity not save in database
        Assertions.assertThat(testService.size()).isEqualTo(1);
    }


    @Test
    public void testEvaluateWithRollbackWhileTryingToPersistMachineInProcessingFunction() {

        // Act
        Exception actualException = null;
        try {
            StateMachine<States, Events> machine = xStateMachineService
                    .evaluateWithTransactionalRollback(PERSISTED_ID, stateMachine -> {
                        stateMachine.sendEvent(Events.START_FEATURE);
                        try {
                            persister.persist(stateMachine, PERSISTED_ID);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Assertions.fail(e.getMessage());
                        }
                        testService.ok();
                        testService.fail();
                        return stateMachine;
                    });
        } catch (Exception e) {
            actualException = e;
        }

        // Asserts
        Assertions.assertThat(actualException.getMessage())
                  .contains("not-null property references a null or transient value");
        Assertions.assertThat(actualException)
                  .isInstanceOf(DataIntegrityViolationException.class);

        // Read a result from the storage
        StateMachine<States, Events> machine = xStateMachineService.get(PERSISTED_ID);

        Assertions.assertThat(machine.getState().getId()).isEqualTo(States.BACKLOG);

        // Check that entity not save in database
        Assertions.assertThat(testService.size()).isEqualTo(0);
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