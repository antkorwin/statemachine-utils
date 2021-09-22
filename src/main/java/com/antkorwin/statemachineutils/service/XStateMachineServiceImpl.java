package com.antkorwin.statemachineutils.service;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.antkorwin.statemachineutils.service.usecases.CreateStateMachineUseCase;
import com.antkorwin.statemachineutils.service.usecases.GetStateMachineUseCase;
import com.antkorwin.statemachineutils.service.usecases.UpdateStateMachineUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.statemachine.StateMachine;

/**
 * Created on 09.07.2018.
 *
 * @author Korovin Anatoliy
 */
@Slf4j
@RequiredArgsConstructor
public class XStateMachineServiceImpl<StatesT, EventsT> implements XStateMachineService<StatesT, EventsT> {

	private final CreateStateMachineUseCase<StatesT, EventsT> createStateMachineUseCase;
	private final GetStateMachineUseCase<StatesT, EventsT> getStateMachineUseCase;
	private final UpdateStateMachineUseCase<StatesT, EventsT> updateStateMachineUseCase;

	//region CREATE
	@Override
	public StateMachine<StatesT, EventsT> create(String machineId) {
		return createStateMachineUseCase.create(machineId);
	}

	@Override
	public StateMachine<StatesT, EventsT> create() {
		return createStateMachineUseCase.create();
	}

	@Override
	public StateMachine<StatesT, EventsT> createAndRun(String machineId,
	                                                   Consumer<StateMachine<StatesT, EventsT>> processingFunction) {
		return createStateMachineUseCase.createAndRun(machineId, processingFunction);
	}
	//endregion CREATE

	//region GET
	@Override
	public StateMachine<StatesT, EventsT> get(String machineId) {
		return getStateMachineUseCase.get(machineId);
	}

	@Override
	public boolean isExist(String machineId) {
		return getStateMachineUseCase.isExist(machineId);
	}

	@Override
	public List<EventsT> retrieveAvailableEvents(String stateMachineId) {
		return getStateMachineUseCase.retrieveAvailableEvents(get(stateMachineId));
	}

	@Override
	public List<EventsT> retrieveAvailableEvents(StateMachine<StatesT, EventsT> machine) {
		return getStateMachineUseCase.retrieveAvailableEvents(machine);
	}
	//endregion GET

	//region UPDATE
	@Override
	public <ResultT> ResultT evaluate(String stateMachineId,
	                                  Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction) {
		return updateStateMachineUseCase.evaluate(stateMachineId, processingFunction);
	}

	@Override
	public <ResultT> ResultT evaluate(StateMachine<StatesT, EventsT> machine,
	                                  Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction) {
		return updateStateMachineUseCase.evaluate(machine, processingFunction);
	}

	@Override
	public void run(String stateMachineId,
	                Consumer<StateMachine<StatesT, EventsT>> processingFunction) {

		updateStateMachineUseCase.evaluate(stateMachineId,
		                                   machine -> {
			                                   processingFunction.accept(machine);
			                                   return true;
		                                   });
	}

	@Override
	public void run(StateMachine<StatesT, EventsT> stateMachine,
	                Consumer<StateMachine<StatesT, EventsT>> processingFunction) {

		updateStateMachineUseCase.evaluate(stateMachine,
		                                   machine -> {
			                                   processingFunction.accept(machine);
			                                   return true;
		                                   });
	}

	@Override
	public StateMachine<StatesT, EventsT> update(String machineId, StateMachine<StatesT, EventsT> machine) {
		return updateStateMachineUseCase.update(machineId, machine);
	}
	//region UPDATE
}
