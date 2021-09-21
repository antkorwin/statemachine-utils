package com.antkorwin.statemachineutils.service.usecases;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.antkorwin.statemachineutils.resolver.StateMachineResolver;
import com.antkorwin.statemachineutils.service.XStateMachineException;
import com.antkorwin.statemachineutils.service.XStateMachineService;
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
public class UpdateStateMachineUseCase<StatesT, EventsT> {

	private final StateMachineWrapper<StatesT, EventsT> rollbackWrapper;
	private final StateMachineWrapper<StatesT, EventsT> transactionalWrapper;
	private final StateMachinePersister<StatesT, EventsT, String> persister;
	private final StateMachinePersist<StatesT, EventsT, String> persist;
	private final StateMachineFactory<StatesT, EventsT> factory;
	private final StateMachineResolver<StatesT, EventsT> resolver;

	private final GetStateMachineUseCase<StatesT, EventsT> getStateMachineUseCase;


	public <ResultT> ResultT evaluate(String stateMachineId,
	                                  Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction) {
		StateMachine<StatesT, EventsT> machine = getStateMachineUseCase.get(stateMachineId);
		return internalEvaluate(machine, processingFunction, rollbackWrapper);
	}

	public <ResultT> ResultT evaluate(StateMachine<StatesT, EventsT> machine,
	                                  Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction) {
		return internalEvaluate(machine, processingFunction, rollbackWrapper);
	}

	public <ResultT> ResultT evaluateTransactional(String stateMachineId,
	                                               Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction) {
		StateMachine<StatesT, EventsT> machine = getStateMachineUseCase.get(stateMachineId);
		return internalEvaluate(machine, processingFunction, transactionalWrapper);
	}

	public <ResultT> ResultT evaluateTransactional(StateMachine<StatesT, EventsT> machine,
	                                               Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction) {
		return internalEvaluate(machine, processingFunction, transactionalWrapper);
	}

	private <ResultT> ResultT internalEvaluate(StateMachine<StatesT, EventsT> machine,
	                                           Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction,
	                                           StateMachineWrapper<StatesT, EventsT> wrapper) {
		try {
			ResultT result = wrapper.evaluateWithRollback(machine, processingFunction);
			update(machine.getId(), machine);
			return result;
		} catch (Exception e) {
			update(machine.getId(), machine); // persist old version of a roll-backed S.M.
			throw e;
		}
	}

	public StateMachine<StatesT, EventsT> update(String machineId, StateMachine<StatesT, EventsT> machine) {
		try {
			persister.persist(machine, machineId);
			return machine;
		} catch (Exception e) {
			log.error("unable to persist the state machine during the update: " + machineId, e);
			throw new XStateMachineException(UNABLE_TO_PERSIST_STATE_MACHINE_DURING_UPDATE, e);
		}
	}
}
