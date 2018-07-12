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

    /**
     * Create a state machine instance and persist it
     * in the state machine persist-storage.
     *
     * @param machineId identifier of the S.M.
     * @return created state machine instance
     */
    StateMachine<StatesT, EventsT> create(UUID machineId);

    /**
     * Load an instance of a state machine form the
     * state machine persist-storage.
     *
     * Throws an exception if doesn't find S.M. in storage by identifier.
     *
     * @param id identity of the S.M.
     * @return state machine instance
     */
    StateMachine<StatesT, EventsT> get(UUID id);

    /**
     * Update the S.M. in persist-storage by identifier of an existing
     * machine and its instance that we need to save in storage.
     *
     * Throws an exception if doesn't find S.M. in storage by identifier.
     *
     * @param machineId identifier of the S.M.
     * @param machine   actual state of the S.M. that will save in storage
     * @return actual state machine instance
     */
    StateMachine<StatesT, EventsT> update(UUID machineId, StateMachine<StatesT, EventsT> machine);

    /**
     * Try to evaluate a result of processing function
     * on the loaded S.M. which loaded from a persist storage,
     * and save a result state of the S.M. back to the storage.
     *
     * If a processing function throws an exception then machine will
     * roll-back to previous state, even if you directly persist it
     * in the processing function. You can think about a processing
     * function as like as transactional function over the SM.
     *
     * @param stateMachineId     identifier of the state machine
     * @param processingFunction function that need to applies on the S.M.
     * @param <ResultT>          result of the function
     * @return result of the function
     */
    <ResultT> ResultT evaluate(UUID stateMachineId,
                               Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction);

    /**
     * Same as evaluate method, but wrap a processing function
     * in a JPA transaction in order to have an ability to roll-back
     * all items which you can persist in a DataBase through execute
     * a processing function.
     *
     * This function will roll-back if the transaction inside it does
     * not commit successful.
     *
     * @param stateMachineId     identifier of the state machine
     * @param processingFunction function that need to applies on the S.M.
     * @param <ResultT>          result of the function
     * @return result of the function
     */
    <ResultT> ResultT evaluateTransactional(UUID stateMachineId,
                                            Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction);
}
