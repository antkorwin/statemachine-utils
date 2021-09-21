package com.antkorwin.statemachineutils.service.usecases;

import java.util.List;

import com.antkorwin.commonutils.exceptions.BaseException;
import com.antkorwin.statemachineutils.resolver.StateMachineResolver;
import com.antkorwin.statemachineutils.service.XStateMachineException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;

import static com.antkorwin.statemachineutils.service.XServiceErrorInfo.UNABLE_TO_FETCH_STATE_MACHINE_FROM_PERSIST;
import static com.antkorwin.statemachineutils.service.XServiceErrorInfo.UNABLE_TO_READ_STATE_MACHINE_FROM_STORE;

/**
 * Created on 09.07.2018.
 *
 * @author Korovin Anatoliy
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class GetStateMachineUseCase<StatesT, EventsT> {

	private final StateMachinePersister<StatesT, EventsT, String> persister;
	private final StateMachinePersist<StatesT, EventsT, String> persist;
	private final StateMachineFactory<StatesT, EventsT> factory;
	private final StateMachineResolver<StatesT, EventsT> resolver;


	public StateMachine<StatesT, EventsT> get(String machineId) {
		log.debug("Getting a new machine from factory to restore from persister");
		StateMachine<StatesT, EventsT> machine = factory.getStateMachine(machineId);
		try {
			return persister.restore(machine, machineId);
		} catch (BaseException baseExc) {
			throw baseExc;
		} catch (Exception e) {
			log.error("Error while restore state machine", e);
			throw new XStateMachineException(UNABLE_TO_READ_STATE_MACHINE_FROM_STORE, e);
		}
	}

	public boolean isExist(String machineId) {
		try {
			return persist.read(machineId) != null;
		} catch (Exception e) {
			throw new XStateMachineException(UNABLE_TO_FETCH_STATE_MACHINE_FROM_PERSIST, e);
		}
	}

	public List<EventsT> retrieveAvailableEvents(String stateMachineId) {
		return resolver.getAvailableEvents(get(stateMachineId));
	}

	public List<EventsT> retrieveAvailableEvents(StateMachine<StatesT, EventsT> machine) {
		return resolver.getAvailableEvents(machine);
	}
}
