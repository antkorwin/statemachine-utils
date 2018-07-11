package com.antkorwin.statemachineutils.service;

import com.antkorwin.commonutils.exceptions.BaseException;
import com.antkorwin.statemachineutils.wrapper.StateMachineWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;

import java.util.UUID;
import java.util.function.Function;

/**
 * Created by Korovin Anatolii on 09.07.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
@Slf4j
public class XStateMachineServiceImpl<StatesT, EventsT> implements XStateMachineService<StatesT, EventsT> {

    private final StateMachineWrapper<StatesT, EventsT> rollbackWrapper;
    private final StateMachinePersister<StatesT, EventsT, UUID> persister;
    private final StateMachineFactory<StatesT, EventsT> factory;

    public XStateMachineServiceImpl(
            StateMachineWrapper<StatesT, EventsT> rollbackWrapper,
            StateMachinePersister<StatesT, EventsT, UUID> persister,
            StateMachineFactory<StatesT, EventsT> factory) {
        this.rollbackWrapper = rollbackWrapper;
        this.persister = persister;
        this.factory = factory;
    }

    @Override
    public StateMachine<StatesT, EventsT> create(UUID machineId) {
        log.debug("Creating new machine from factory with id " + machineId);
        StateMachine<StatesT, EventsT> machine = factory.getStateMachine(machineId.toString());
        try {
            persister.persist(machine, machineId);
            return machine;
        } catch (Exception e) {
            log.error("Unable to persist new state machine : " + machineId.toString(), e);
            throw new StateMachineException("Unable to persist new state machine", e);
        }
    }

    @Override
    public StateMachine<StatesT, EventsT> get(UUID machineId) {
        log.debug("Getting a new machine from factory with id " + machineId);
        StateMachine<StatesT, EventsT> machine = factory.getStateMachine(machineId.toString());
        try {
            return persister.restore(machine, machineId);
        } catch (BaseException baseExc) {
            throw baseExc;
        } catch (Exception e) {
            log.error("Error while restore state machine", e);
            throw new StateMachineException("Unable to read state machine from store", e);
        }
    }

    @Override
    public StateMachine<StatesT, EventsT> update(UUID machineId, StateMachine<StatesT, EventsT> machine) {
        try {
            persister.persist(machine, machineId);
            return machine;
        } catch (Exception e) {
            log.error("unable to persist the state machine during the update: " + machineId.toString(), e);
            throw new StateMachineException("Unable to persist the state machine during the update", e);
        }
    }

    @Override
    public <ResultT> ResultT evaluateWithRollback(UUID id,
                                                  Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction) {

        StateMachine<StatesT, EventsT> machine = get(id);
        ResultT result = rollbackWrapper.evaluateWithRollback(machine, processingFunction);
        update(id, machine);
        return result;
    }
}
