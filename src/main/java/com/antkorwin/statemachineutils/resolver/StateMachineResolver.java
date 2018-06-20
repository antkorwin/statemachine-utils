package com.antkorwin.statemachineutils.resolver;

import org.springframework.statemachine.StateMachine;

import java.util.List;

/**
 * Created on 02.06.2018.
 *
 * @author Korovin Anatoliy
 */
public interface StateMachineResolver<S, E> {

    /**
     * Evaluate available events from a current state of the state-machine
     *
     * @param stateMachine state machine
     *
     * @return available events collection
     */
    List<E> getAvailableEvents(StateMachine<S, E> stateMachine);
}
