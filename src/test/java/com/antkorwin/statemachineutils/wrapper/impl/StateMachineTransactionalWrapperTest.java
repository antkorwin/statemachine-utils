package com.antkorwin.statemachineutils.wrapper.impl;

import com.antkorwin.commonutils.exceptions.WrongArgumentException;
import com.antkorwin.commonutils.validation.GuardCheck;
import com.antkorwin.statemachineutils.TransactionalTestConfig;
import com.antkorwin.statemachineutils.config.Events;
import com.antkorwin.statemachineutils.config.StateMachineConfig;
import com.antkorwin.statemachineutils.config.States;
import com.antkorwin.statemachineutils.wrapper.EnableStateMachineWrapper;
import com.antkorwin.statemachineutils.wrapper.StateMachineWrapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static com.antkorwin.statemachineutils.wrapper.StateMachineWrapperErrorInfo.PROCESSING_FUNCTION_IS_MANDATORY_ARGUMENT;
import static com.antkorwin.statemachineutils.wrapper.StateMachineWrapperErrorInfo.STATE_MACHINE_IS_MANDATORY_ARGUMENT;
import static org.mockito.Mockito.mock;

/**
 * Created on 21.06.2018.
 *
 * @author Korovin Anatoliy
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@Import({StateMachineConfig.class, TransactionalTestConfig.class})
@EnableStateMachineWrapper
@TestPropertySource(properties = "antkorwin.statemachine.rollback=transactional")
public class StateMachineTransactionalWrapperTest {

    @Autowired
    private StateMachineWrapper<States, Events> stateMachineTransactionalWrapper;

    @Autowired
    private StateMachineFactory<States, Events> stateMachineFactory;

    @Autowired
    private TransactionalTestConfig.TestService testService;

    @Before
    public void setUp() throws Exception {
        testService.clear();
    }

    @Test
    public void testWithOneOfTwoTransactionsIsFail() {
        // Arrange
        StateMachine<States, Events> stateMachine = stateMachineFactory.getStateMachine();
        Exception actualException = null;

        try {
            // Act
            stateMachineTransactionalWrapper.runWithRollback(stateMachine, machine -> {
                machine.sendEvent(Events.START_FEATURE);
                testService.ok();
                testService.fail();
            });
        } catch (Exception e) {
            actualException = e;
        }

        // Assert
        Assertions.assertThat(actualException.getMessage())
                  .contains("not-null property references a null or transient value");

        Assertions.assertThat(actualException)
                  .isInstanceOf(DataIntegrityViolationException.class);

        Assertions.assertThat(stateMachine.getState().getId())
                  .isEqualTo(States.BACKLOG);

        Assertions.assertThat(testService.size()).isEqualTo(0);
    }

    @Test
    public void testWithSuccessfulTransactionCommit() {
        // Arrange
        StateMachine<States, Events> stateMachine = stateMachineFactory.getStateMachine();

        // Act
        stateMachineTransactionalWrapper.runWithRollback(stateMachine, machine -> {
            machine.sendEvent(Events.START_FEATURE);
            testService.ok();
        });

        // Assert
        Assertions.assertThat(stateMachine.getState().getId())
                  .isEqualTo(States.IN_PROGRESS);

        Assertions.assertThat(testService.size()).isEqualTo(1);
    }

    @Test
    public void testWithSuccessfulTransactionCommitAndEvaluateResultFromWrapper() {
        // Arrange
        StateMachine<States, Events> stateMachine = stateMachineFactory.getStateMachine();

        // Act
        TransactionalTestConfig.Foo result = stateMachineTransactionalWrapper.evaluateWithRollback(stateMachine, machine -> {
            machine.sendEvent(Events.START_FEATURE);
            return testService.ok();
        });

        // Assert
        Assertions.assertThat(result).isNotNull();

        Assertions.assertThat(stateMachine.getState().getId())
                  .isEqualTo(States.IN_PROGRESS);

        Assertions.assertThat(testService.size()).isEqualTo(1);
    }


    @Test
    public void testWrongArgsStateMachine() {
        // Act & asserts
        GuardCheck.check(() -> stateMachineTransactionalWrapper.runWithRollback(null, m -> m.start()),
                         WrongArgumentException.class,
                         STATE_MACHINE_IS_MANDATORY_ARGUMENT);
    }

    @Test
    public void testWrongArgsRunnable() {
        StateMachine<States, Events> machine = mock(StateMachine.class);
        // Act & asserts
        GuardCheck.check(() -> stateMachineTransactionalWrapper.runWithRollback(machine, null),
                         WrongArgumentException.class,
                         PROCESSING_FUNCTION_IS_MANDATORY_ARGUMENT);
    }

    @Test
    public void testEvaluateWithWrongStateMachine() {
        // Act & asserts
        GuardCheck.check(() -> stateMachineTransactionalWrapper.evaluateWithRollback(null, m -> 123),
                         WrongArgumentException.class,
                         STATE_MACHINE_IS_MANDATORY_ARGUMENT);
    }

    @Test
    public void testEvaluateWithWrongRunnable() {
        StateMachine<States, Events> machine = mock(StateMachine.class);
        // Act & asserts
        GuardCheck.check(() -> stateMachineTransactionalWrapper.evaluateWithRollback(machine, null),
                         WrongArgumentException.class,
                         PROCESSING_FUNCTION_IS_MANDATORY_ARGUMENT);
    }
}