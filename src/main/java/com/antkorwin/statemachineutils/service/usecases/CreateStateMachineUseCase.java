package com.antkorwin.statemachineutils.service.usecases;

import java.util.UUID;
import java.util.function.Consumer;

import com.antkorwin.statemachineutils.service.XStateMachineException;
import com.antkorwin.statemachineutils.wrapper.StateMachineWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;

import static com.antkorwin.statemachineutils.service.XServiceErrorInfo.UNABLE_TO_PERSIST_STATE_MACHINE;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class CreateStateMachineUseCase<StatesT, EventsT> {

	private final StateMachinePersister<StatesT, EventsT, String> persister;
	private final StateMachineFactory<StatesT, EventsT> factory;
	private final StateMachineWrapper<StatesT, EventsT> rollbackWrapper;


	public StateMachine<StatesT, EventsT> create(String machineId) {
		StateMachine<StatesT, EventsT> machine = factory.getStateMachine(machineId);
		return save(machineId, machine);
	}

	public StateMachine<StatesT, EventsT> create() {
		return create(UUID.randomUUID().toString());
	}

	public StateMachine<StatesT, EventsT> createAndRun(String machineId,
	                                                             Consumer<StateMachine<StatesT, EventsT>> processingFunction) {
		StateMachine<StatesT, EventsT> machine = create(machineId);
		internalEvaluate(machine, processingFunction, rollbackWrapper);
		return machine;
	}

	private void internalEvaluate(StateMachine<StatesT, EventsT> machine,
	                              Consumer<StateMachine<StatesT, EventsT>> processingFunction,
	                              StateMachineWrapper<StatesT, EventsT> wrapper) {
		String machineId = machine.getId();
		try {
			wrapper.evaluateWithRollback(machine, m -> {
				processingFunction.accept(m);
				return true;
			});
			save(machineId, machine);
		} catch (Exception e) {
			save(machineId, machine); // persist old version of a roll-backed S.M.
			throw e;
		}
	}

	private StateMachine<StatesT, EventsT> save(String machineId, StateMachine<StatesT, EventsT> machine) {
		try {
			persister.persist(machine, machineId);
			return machine;
		} catch (Exception e) {
			log.error("unable to persist the state machine with id: " + machineId, e);
			throw new XStateMachineException(UNABLE_TO_PERSIST_STATE_MACHINE, e);
		}
	}
}
