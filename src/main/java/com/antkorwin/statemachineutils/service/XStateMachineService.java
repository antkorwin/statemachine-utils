package com.antkorwin.statemachineutils.service;

import org.springframework.statemachine.StateMachine;

import java.util.UUID;
import java.util.function.Function;

/**
 * Created by Korovin Anatolii on 09.07.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
public interface XStateMachineService<StatesT, EventsT> {

    StateMachine<StatesT, EventsT> create(UUID machineId);

    StateMachine<StatesT, EventsT> get(UUID id);

    <ResultT> ResultT evaluateWithRollback(UUID id, Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction);

    StateMachine<StatesT, EventsT> update(UUID machineId, StateMachine<StatesT, EventsT> machine);

    <ResultT> ResultT evaluateWithTransactionalRollback(UUID id,
                                                        Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction);
}
