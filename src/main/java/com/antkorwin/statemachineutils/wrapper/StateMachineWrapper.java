package com.antkorwin.statemachineutils.wrapper;

import org.springframework.statemachine.StateMachine;

import java.util.function.Consumer;

/**
 * Created on 04.06.2018.
 *
 * @author Korovin Anatoliy
 */
public interface StateMachineWrapper<StatesT, EventsT> {

    void runWithRollback(StateMachine<StatesT, EventsT> machine,
                         Consumer<StateMachine<StatesT, EventsT>> runnable);
}
