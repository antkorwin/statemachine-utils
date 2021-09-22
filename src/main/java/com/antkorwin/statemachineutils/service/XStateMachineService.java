package com.antkorwin.statemachineutils.service;

import org.springframework.statemachine.StateMachine;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created on 09.07.2018.
 *
 * @author Korovin Anatoliy
 */
public interface XStateMachineService<StatesT, EventsT> {

    /**
     * Create a state machine instance and persist it
     * in the state machine persist-storage.
     *
     * @param machineId UUID identifier of the S.M.
     * @return created state machine instance
     */
    StateMachine<StatesT, EventsT> create(String machineId);


    StateMachine<StatesT, EventsT> create();

	StateMachine<StatesT, EventsT> createAndRun(String machineId,
	                                            Consumer<StateMachine<StatesT, EventsT>> processingFunction);

	StateMachine<StatesT, EventsT> createAndRunTransactional(String machineId,
	                                                         Consumer<StateMachine<StatesT, EventsT>> processingFunction);

	/**
     * Load an instance of a state machine form the
     * state machine persist-storage.
     *
     * Throws an exception if doesn't find S.M. in storage by identifier.
     *
     * @param id identity of the S.M.
     * @return state machine instance
     */
    StateMachine<StatesT, EventsT> get(String id);


	List<EventsT> retrieveAvailableEvents(StateMachine<StatesT, EventsT> machine);

	<ResultT> ResultT evaluate(StateMachine<StatesT, EventsT> machine,
	                           Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction);

	<ResultT> ResultT evaluateTransactional(StateMachine<StatesT, EventsT> machine,
	                                        Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction);

	void run(String stateMachineId,
	         Consumer<StateMachine<StatesT, EventsT>> processingFunction);

	void run(StateMachine<StatesT, EventsT> stateMachine,
	         Consumer<StateMachine<StatesT, EventsT>> processingFunction);

	void runTransactional(String stateMachineId,
	                      Consumer<StateMachine<StatesT, EventsT>> processingFunction);

	void runTransactional(StateMachine<StatesT, EventsT> stateMachine,
	                      Consumer<StateMachine<StatesT, EventsT>> processingFunction);

	StateMachine<StatesT, EventsT> update(String machineId, StateMachine<StatesT, EventsT> machine);

    boolean isExist(String machineId);

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
    <ResultT> ResultT evaluate(String stateMachineId,
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
    <ResultT> ResultT evaluateTransactional(String stateMachineId,
                                            Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction);

    /**
     * Retrieve all available events from a current state of a state machine.
     *
     * @param stateMachineId identifier of the state machine
     * @return list of available events for the current state of the state machine
     */
    List<EventsT> retrieveAvailableEvents(String stateMachineId);
}
