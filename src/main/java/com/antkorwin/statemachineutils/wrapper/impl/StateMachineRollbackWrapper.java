package com.antkorwin.statemachineutils.wrapper.impl;

import com.antkorwin.commonutils.validation.Guard;
import com.antkorwin.statemachineutils.wrapper.AbstractInMemoryStateMachinePersist;
import com.antkorwin.statemachineutils.wrapper.StateMachineWrapper;
import com.antkorwin.xsync.XSync;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

import java.util.UUID;
import java.util.function.Consumer;

import static com.antkorwin.statemachineutils.wrapper.StateMachineWrapperErrorInfo.PROCESSING_FUNCTION_IS_MANDATORY_ARGUMENT;
import static com.antkorwin.statemachineutils.wrapper.StateMachineWrapperErrorInfo.STATE_MACHINE_IS_MANDATORY_ARGUMENT;

/**
 * Created on 09.06.2018.
 * <p>
 * StateMachineWrapper implementation, which restore a previous state
 * of a machine if throws an exception in processing.
 *
 * @author Korovin Anatoliy
 */
@Slf4j
public class StateMachineRollbackWrapper<StatesT, EventsT> implements StateMachineWrapper<StatesT, EventsT> {

    private final XSync<UUID> stateMachineXSync;
    private final StateMachinePersister storage;
    private final AbstractInMemoryStateMachinePersist persist;

    @Autowired
    public StateMachineRollbackWrapper(XSync<UUID> stateMachineXSync) {
        this.stateMachineXSync = stateMachineXSync;
        this.persist = new AbstractInMemoryStateMachinePersist();
        this.storage = new DefaultStateMachinePersister(persist);
    }

    @Override
    public void runWithRollback(StateMachine<StatesT, EventsT> machine,
                                Consumer<StateMachine<StatesT, EventsT>> runnable) {

        Guard.checkArgumentExist(machine, STATE_MACHINE_IS_MANDATORY_ARGUMENT);
        Guard.checkArgumentExist(runnable, PROCESSING_FUNCTION_IS_MANDATORY_ARGUMENT);

        stateMachineXSync.execute(machine.getUuid(), () -> {
            UUID id = backupStateMachine(storage, machine);
            try {
                runnable.accept(machine);
            } catch (Throwable e) {
                log.warn("StateMachineWrapper rolling back after the error: ", e);
                restoreStateMachine(storage, machine, id);
                throw e;
            }
            removeBackup(id);
        });
    }

    private UUID backupStateMachine(StateMachinePersister storage,
                                    StateMachine<StatesT, EventsT> stateMachine) {
        UUID id = UUID.randomUUID();
        try {
            storage.persist(stateMachine, id);
            return id;
        } catch (Exception e) {
            log.error("Error while making backup of state machine: {}", e);
            throw new RuntimeException("StateMachine backup error");
        }
    }

    private void restoreStateMachine(StateMachinePersister storage,
                                     StateMachine<StatesT, EventsT> stateMachine,
                                     UUID id) {
        if (id == null) return;
        try {
            storage.restore(stateMachine, id);
        } catch (Exception e) {
            log.error("Error while restoring the state machine from backup");
            throw new RuntimeException("StateMachine restore error");
        }
    }

    private void removeBackup(UUID id) {
        this.persist.remove(id);
    }
}
