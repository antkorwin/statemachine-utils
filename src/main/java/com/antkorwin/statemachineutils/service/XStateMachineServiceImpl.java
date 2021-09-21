package com.antkorwin.statemachineutils.service;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.antkorwin.statemachineutils.resolver.StateMachineResolver;
import com.antkorwin.statemachineutils.service.usecases.CreateStateMachineUseCase;
import com.antkorwin.statemachineutils.service.usecases.GetStateMachineUseCase;
import com.antkorwin.statemachineutils.service.usecases.UpdateStateMachineUseCase;
import com.antkorwin.statemachineutils.wrapper.StateMachineWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;

import static com.antkorwin.statemachineutils.service.XServiceErrorInfo.UNABLE_TO_PERSIST_STATE_MACHINE_DURING_UPDATE;

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

	@Override
	public StateMachine<StatesT, EventsT> createAndRunTransactional(String machineId,
	                                                                Consumer<StateMachine<StatesT, EventsT>> processingFunction) {
		return createStateMachineUseCase.createAndRunTransactional(machineId, processingFunction);
	}

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
	public <ResultT> ResultT evaluateTransactional(String stateMachineId,
	                                               Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction) {
		return updateStateMachineUseCase.evaluateTransactional(stateMachineId, processingFunction);
	}

	@Override
	public <ResultT> ResultT evaluateTransactional(StateMachine<StatesT, EventsT> machine,
	                                               Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction) {
		return updateStateMachineUseCase.evaluateTransactional(machine, processingFunction);
	}

	@Override
	public StateMachine<StatesT, EventsT> update(String machineId, StateMachine<StatesT, EventsT> machine) {
		return updateStateMachineUseCase.update(machineId, machine);
	}
}
