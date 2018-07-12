package com.antkorwin.statemachineutils.persist;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;

import static org.mockito.Mockito.spy;

/**
 * Created on 12.07.2018.
 *
 * A static helper to get a context of the state machine
 * by the instance.
 *
 * @author Korovin Anatoliy
 */
public class StateMachineContextEvaluator<StatesT, EventsT, IdentifierT>
        extends DefaultStateMachinePersister<StatesT, EventsT, IdentifierT> {

    /**
     * Instantiates a new default state machine persister.
     *
     * @param stateMachinePersist the state machine persist
     */
    private StateMachineContextEvaluator(StateMachinePersist<StatesT, EventsT, IdentifierT> stateMachinePersist) {
        super(stateMachinePersist);
    }

    /**
     * get a context of the state machine by its instance
     *
     * @param stateMachine instance of the state machine
     * @param <S>          type of the enum with states
     * @param <E>          type of the enum with events
     * @param <T>          type of the state machine identifier
     * @return state machine context
     */
    public static <S, E, T> StateMachineContext<S, E> getContext(StateMachine<S, E> stateMachine) {

        return new StateMachineContextEvaluator<S, E, T>(spy(StateMachinePersist.class))
                .buildStateMachineContext(stateMachine);
    }
}
