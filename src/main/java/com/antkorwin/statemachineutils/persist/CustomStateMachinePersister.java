package com.antkorwin.statemachineutils.persist;

import com.antkorwin.commonutils.validation.Guard;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.state.AbstractState;
import org.springframework.statemachine.state.HistoryPseudoState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.AbstractStateMachine;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 10.07.2018.
 *
 * This is a copy of AbstractStateMachinePersister,
 * because it's impossible to override some methods of it.
 *
 * @author Korovin Anatoliy
 */
public class CustomStateMachinePersister<S, E, T> implements StateMachinePersister<S, E, T> {


    private final StateMachinePersist<S, E, T> stateMachinePersist;

    /**
     * Instantiates a new abstract state machine persister.
     *
     * @param stateMachinePersist the state machine persist
     */
    public CustomStateMachinePersister(StateMachinePersist<S, E, T> stateMachinePersist) {
        Assert.notNull(stateMachinePersist, "StateMachinePersist must be set");
        this.stateMachinePersist = stateMachinePersist;
    }

    @Override
    public final void persist(StateMachine<S, E> stateMachine, T contextObj) throws Exception {
        stateMachinePersist.write(buildStateMachineContext(stateMachine), contextObj);
    }

    @Override
    public final StateMachine<S, E> restore(StateMachine<S, E> stateMachine, T contextObj) throws Exception {
        final StateMachineContext<S, E> context = stateMachinePersist.read(contextObj);

        Guard.checkEntityExist(context, PersisterErrorInfo.COULD_NOT_READ_STATEMACHINE_FROM_PERSIST);

        stateMachine.stop();
        stateMachine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<S, E>>() {

            @Override
            public void apply(StateMachineAccess<S, E> function) {
                function.resetStateMachine(context);
            }
        });
        stateMachine.start();
        return stateMachine;
    }

    protected StateMachineContext<S, E> buildStateMachineContext(StateMachine<S, E> stateMachine) {
        ExtendedState extendedState = new DefaultExtendedState();
        extendedState.getVariables().putAll(stateMachine.getExtendedState().getVariables());

        ArrayList<StateMachineContext<S, E>> childs = new ArrayList<StateMachineContext<S, E>>();
        S id = null;
        State<S, E> state = stateMachine.getState();
        if (state.isSubmachineState()) {
            id = getDeepState(state);
        } else if (state.isOrthogonal()) {
            Collection<Region<S, E>> regions = ((AbstractState<S, E>)state).getRegions();
            for (Region<S, E> r : regions) {
                StateMachine<S, E> rsm = (StateMachine<S, E>) r;
                childs.add(buildStateMachineContext(rsm));
            }
            id = state.getId();
        } else {
            id = state.getId();
        }

        // building history state mappings
        Map<S, S> historyStates = new HashMap<S, S>();
        PseudoState<S, E> historyState = ((AbstractStateMachine<S, E>) stateMachine).getHistoryState();
        if (historyState != null) {
            historyStates.put(null, ((HistoryPseudoState<S, E>)historyState).getState().getId());
        }
        Collection<State<S, E>> states = stateMachine.getStates();
        for (State<S, E> ss : states) {
            if (ss.isSubmachineState()) {
                StateMachine<S, E> submachine = ((AbstractState<S, E>) ss).getSubmachine();
                PseudoState<S, E> ps = ((AbstractStateMachine<S, E>) submachine).getHistoryState();
                if (ps != null) {
                    State<S, E> pss = ((HistoryPseudoState<S, E>)ps).getState();
                    if (pss != null) {
                        historyStates.put(ss.getId(), pss.getId());
                    }
                }
            }
        }
        return new DefaultStateMachineContext<S, E>(childs, id, null, null, extendedState, historyStates, stateMachine.getId());
    }

    private S getDeepState(State<S, E> state) {
        Collection<S> ids1 = state.getIds();
        @SuppressWarnings("unchecked")
        S[] ids2 = (S[]) ids1.toArray();
        // TODO: can this be empty as then we'd get error?
        return ids2[ids2.length-1];
    }
}
