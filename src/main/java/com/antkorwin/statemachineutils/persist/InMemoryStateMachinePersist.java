package com.antkorwin.statemachineutils.persist;

import java.util.HashMap;
import java.util.UUID;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;

/**
 * Created by Korovin A. on 05.06.2018.
 * <p>
 * Untyped statemachine persist implementation, based on the HashMap
 *
 * @author Korovin Anatoliy
 * @version 1.0
 */
public class InMemoryStateMachinePersist<StatesT, EventsT, IdentifierT> implements StateMachinePersist<StatesT, EventsT, IdentifierT> {

	private HashMap<IdentifierT, StateMachineContext<StatesT, EventsT>> storage = new HashMap<>();

	@Override
	public void write(StateMachineContext<StatesT, EventsT> context, IdentifierT contextObj) throws Exception {
		storage.put(contextObj, context);
	}

	@Override
	public StateMachineContext<StatesT, EventsT> read(IdentifierT contextObj) throws Exception {
		return storage.get(contextObj);
	}

	public void remove(IdentifierT id) {
		storage.remove(id);
	}
}
