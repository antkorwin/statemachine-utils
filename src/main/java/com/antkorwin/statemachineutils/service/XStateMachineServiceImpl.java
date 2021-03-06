package com.antkorwin.statemachineutils.service;

import com.antkorwin.commonutils.exceptions.BaseException;
import com.antkorwin.statemachineutils.resolver.StateMachineResolver;
import com.antkorwin.statemachineutils.wrapper.StateMachineWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static com.antkorwin.statemachineutils.service.XServiceErrorInfo.*;

/**
 * Created on 09.07.2018.
 *
 * @author Korovin Anatoliy
 */
@Slf4j
public class XStateMachineServiceImpl<StatesT, EventsT> implements XStateMachineService<StatesT, EventsT> {

    private final StateMachineWrapper<StatesT, EventsT> rollbackWrapper;
    private final StateMachineWrapper<StatesT, EventsT> transactionalWrapper;
    private final StateMachinePersister<StatesT, EventsT, UUID> persister;
    private final StateMachineFactory<StatesT, EventsT> factory;
    private final StateMachineResolver<StatesT, EventsT> resolver;

    public XStateMachineServiceImpl(
            StateMachineWrapper<StatesT, EventsT> rollbackWrapper,
            StateMachineWrapper<StatesT, EventsT> transactionalWrapper,
            StateMachinePersister<StatesT, EventsT, UUID> persister,
            StateMachineFactory<StatesT, EventsT> factory,
            StateMachineResolver<StatesT, EventsT> resolver) {
        this.rollbackWrapper = rollbackWrapper;
        this.transactionalWrapper = transactionalWrapper;
        this.persister = persister;
        this.factory = factory;
        this.resolver = resolver;
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
            throw new XStateMachineException(UNABLE_TO_PERSIST_NEW_STATE_MACHINE, e);
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
            throw new XStateMachineException(UNABLE_TO_READ_STATE_MACHINE_FROM_STORE, e);
        }
    }

    @Override
    public StateMachine<StatesT, EventsT> update(UUID machineId, StateMachine<StatesT, EventsT> machine) {
        try {
            persister.persist(machine, machineId);
            return machine;
        } catch (Exception e) {
            log.error("unable to persist the state machine during the update: " + machineId.toString(), e);
            throw new XStateMachineException(UNABLE_TO_PERSIST_STATE_MACHINE_DURING_UPDATE, e);
        }
    }

    @Override
    public <ResultT> ResultT evaluate(UUID stateMachineId,
                                      Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction) {

        return internalEvaluate(stateMachineId, processingFunction, rollbackWrapper);
    }

    @Override
    public <ResultT> ResultT evaluateTransactional(UUID stateMachineId,
                                                   Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction) {

        return internalEvaluate(stateMachineId, processingFunction, transactionalWrapper);
    }

    @Override
    public List<EventsT> retrieveAvailableEvents(UUID stateMachineId) {
        return resolver.getAvailableEvents(get(stateMachineId));
    }

    private <ResultT> ResultT internalEvaluate(UUID machineId,
                                               Function<StateMachine<StatesT, EventsT>, ResultT> processingFunction,
                                               StateMachineWrapper<StatesT, EventsT> wrapper) {

        StateMachine<StatesT, EventsT> machine = get(machineId);
        try {
            ResultT result = wrapper.evaluateWithRollback(machine, processingFunction);
            update(machineId, machine);
            return result;
        } catch (Exception e) {
            update(machineId, machine); // persist old version of a roll-backed S.M.
            throw e;
        }
    }
}
