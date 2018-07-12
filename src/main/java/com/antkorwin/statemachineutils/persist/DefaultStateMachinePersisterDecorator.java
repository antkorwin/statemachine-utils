package com.antkorwin.statemachineutils.persist;

import com.antkorwin.commonutils.validation.Guard;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

/**
 * Created on 12.07.2018.
 *
 * A decorator under the {@link DefaultStateMachinePersister} class,
 * append a validating logic while restore object from store.
 *
 * @author Korovin Anatoliy
 */
public class DefaultStateMachinePersisterDecorator<StatesT, EventsT, IdentifierT> implements StateMachinePersister<StatesT, EventsT, IdentifierT> {

    private final StateMachinePersister<StatesT, EventsT, IdentifierT> stateMachinePersister;
    private final StateMachinePersist<StatesT, EventsT, IdentifierT> stateMachinePersist;

    public DefaultStateMachinePersisterDecorator(StateMachinePersist<StatesT, EventsT, IdentifierT> stateMachinePersist) {
        this.stateMachinePersist = stateMachinePersist;
        // Decorated object:
        this.stateMachinePersister = new DefaultStateMachinePersister<>(stateMachinePersist);
    }

    @Override
    public void persist(StateMachine<StatesT, EventsT> stateMachine, IdentifierT contextObj) throws Exception {
        stateMachinePersister.persist(stateMachine, contextObj);
    }

    @Override
    public StateMachine<StatesT, EventsT> restore(StateMachine<StatesT, EventsT> stateMachine, IdentifierT contextObj) throws Exception {
        final StateMachineContext<StatesT, EventsT> context = stateMachinePersist.read(contextObj);
        Guard.checkEntityExist(context, PersisterErrorInfo.COULD_NOT_READ_STATEMACHINE_FROM_PERSIST);
        return stateMachinePersister.restore(stateMachine, contextObj);
    }
}
