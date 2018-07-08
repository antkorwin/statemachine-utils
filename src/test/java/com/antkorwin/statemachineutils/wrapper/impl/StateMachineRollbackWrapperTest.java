package com.antkorwin.statemachineutils.wrapper.impl;

import com.antkorwin.commonutils.exceptions.WrongArgumentException;
import com.antkorwin.commonutils.validation.GuardCheck;
import com.antkorwin.statemachineutils.config.Events;
import com.antkorwin.statemachineutils.config.StateMachineConfig;
import com.antkorwin.statemachineutils.config.States;
import com.antkorwin.statemachineutils.wrapper.EnableStateMachineWrapper;
import com.antkorwin.statemachineutils.wrapper.StateMachineWrapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.stream.IntStream;

import static com.antkorwin.statemachineutils.wrapper.StateMachineWrapperErrorInfo.PROCESSING_FUNCTION_IS_MANDATORY_ARGUMENT;
import static com.antkorwin.statemachineutils.wrapper.StateMachineWrapperErrorInfo.STATE_MACHINE_IS_MANDATORY_ARGUMENT;
import static org.mockito.Mockito.mock;

/**
 * Created on 20.06.2018.
 *
 * @author Korovin Anatoliy
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@Import(StateMachineConfig.class)
@EnableStateMachineWrapper
public class StateMachineRollbackWrapperTest {

    public static final int ITERATION_NUMBER = 100000;

    @Autowired
    private StateMachineFactory<States, Events> stateMachineFactory;

    @Autowired
    @Qualifier("stateMachineRollbackWrapper")
    private StateMachineWrapper<States, Events> stateMachineWrapper;

    @Test
    public void testRollbackAfterThrowsException() throws Exception {

        // Arrange
        StateMachine<States, Events> stateMachine = stateMachineFactory.getStateMachine();
        RuntimeException expectedException = new RuntimeException("stop!");
        Exception actualException = null;

        try {
            // Act
            stateMachineWrapper.runWithRollback(stateMachine, machine -> {
                machine.sendEvent(Events.START_FEATURE);
                throw expectedException;
            });
        } catch (Exception e) {
            actualException = e;
        }

        // Assert
        Assertions.assertThat(actualException)
                  .isEqualTo(expectedException);

        Assertions.assertThat(stateMachine.getState().getId())
                  .isEqualTo(States.BACKLOG);
    }

    @Test
    public void testSuccessRunWithoutRollback() throws Exception {
        // Arrange
        StateMachine<States, Events> stateMachine = stateMachineFactory.getStateMachine();

        // Act
        stateMachineWrapper.runWithRollback(stateMachine, machine -> {
            machine.sendEvent(Events.START_FEATURE);
        });

        // Assert
        Assertions.assertThat(stateMachine.getState().getId())
                  .isEqualTo(States.IN_PROGRESS);
    }


    @Test
    public void testMultipleWrapperSectionsOnDifferentMachines() throws Exception {
        // Arrange
        StateMachine<States, Events> firstStateMachine = stateMachineFactory.getStateMachine();
        StateMachine<States, Events> secondStateMachine = stateMachineFactory.getStateMachine();
        secondStateMachine.sendEvent(Events.START_FEATURE);
        secondStateMachine.sendEvent(Events.DEPLOY);
        secondStateMachine.sendEvent(Events.FINISH_FEATURE);

        // Act
        try {
            stateMachineWrapper.runWithRollback(firstStateMachine, machine -> {
                machine.sendEvent(Events.START_FEATURE);
                throw new RuntimeException("first exception");
            });
        } catch (Exception e) {
            log.warn("EXC: ", e);
        }

        try {
            stateMachineWrapper.runWithRollback(secondStateMachine, machine -> {
                machine.sendEvent(Events.QA_CHECKED_UC);
                throw new RuntimeException("second exception");
            });
        } catch (Exception e) {
            log.warn("EXC: ", e);
        }

        // Assert
        Assertions.assertThat(firstStateMachine.getState().getId())
                  .isEqualTo(States.BACKLOG);

        Assertions.assertThat(secondStateMachine.getState().getId())
                  .isEqualTo(States.TESTING);
    }


    @Test
    public void testConcurrencyWrapper() throws Exception {
        // Arrange
        StateMachine<States, Events> stateMachine = stateMachineFactory.getStateMachine();
        NotAtomicInt notAtomic = new NotAtomicInt();

        // Act
        IntStream.range(0, ITERATION_NUMBER)
                 .boxed()
                 .parallel()
                 .forEach(i -> {
                     stateMachineWrapper.runWithRollback(stateMachine,
                                                         machine -> notAtomic.increment());
                 });

        // Assert
        Assertions.assertThat(notAtomic.getValue()).isEqualTo(ITERATION_NUMBER);
    }

    @Test
    public void testConcurrencyWrapperWithSendInternalEventInMachine() throws Exception {
        // Arrange
        StateMachine<States, Events> stateMachine = stateMachineFactory.getStateMachine();

        // Act
        IntStream.range(0, ITERATION_NUMBER)
                 .boxed()
                 .parallel()
                 .forEach(i -> {
                     stateMachineWrapper.runWithRollback(stateMachine, machine -> {
                         machine.sendEvent(Events.INCREMENT);
                     });
                 });

        // Assert
        Assertions.assertThat((int) stateMachine.getExtendedState().getVariables().get("counter"))
                  .isEqualTo(ITERATION_NUMBER);
    }


    @Test
    public void testWrongArgsStateMachine() {
        // Act & asserts
        GuardCheck.check(() -> stateMachineWrapper.runWithRollback(null, m -> m.start()),
                         WrongArgumentException.class,
                         STATE_MACHINE_IS_MANDATORY_ARGUMENT);
    }

    @Test
    public void testWrongArgsRunnable() {
        StateMachine<States, Events> machine = mock(StateMachine.class);
        // Act & asserts
        GuardCheck.check(() -> stateMachineWrapper.runWithRollback(machine, null),
                         WrongArgumentException.class,
                         PROCESSING_FUNCTION_IS_MANDATORY_ARGUMENT);
    }

    @Getter
    class NotAtomicInt {
        int value = 0;

        int increment() {
            value++;
            return value;
        }
    }
}