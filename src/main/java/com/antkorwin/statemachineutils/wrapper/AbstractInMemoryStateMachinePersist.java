package com.antkorwin.statemachineutils.wrapper;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Korovin A. on 05.06.2018.
 *
 * Untyped statemachine persist implementation, based on the HashMap
 *
 * @author Korovin Anatoliy
 * @version 1.0
 */
public class AbstractInMemoryStateMachinePersist implements StateMachinePersist {

    private HashMap<UUID, StateMachineContext> storage = new HashMap<>();

    @Override
    public void write(StateMachineContext context, Object contextObj) throws Exception {
        storage.put((UUID) contextObj, context);
    }

    @Override
    public StateMachineContext read(Object contextObj) throws Exception {
        return storage.get((UUID) contextObj);
    }

    public void remove(UUID id){
        storage.remove(id);
    }
}
