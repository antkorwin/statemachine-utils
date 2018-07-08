package com.antkorwin.statemachineutils.wrapper;

import org.springframework.statemachine.StateMachine;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created on 04.06.2018.
 *
 * StateMachineWrapper interface
 *
 * @author Korovin Anatoliy
 */
public interface StateMachineWrapper<StatesT, EventsT> {

    void runWithRollback(StateMachine<StatesT, EventsT> stateMachine,
                         Consumer<StateMachine<StatesT, EventsT>> processingFunction);

    <ResultT> ResultT evaluateWithRollback(StateMachine<StatesT, EventsT> machine,
                                           Function<StateMachine<StatesT, EventsT>, ResultT> processingFunc);
}
