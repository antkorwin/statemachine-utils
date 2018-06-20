package com.antkorwin.statemachineutils.wrapper.impl;

import com.antkorwin.statemachineutils.wrapper.StateMachineWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.function.Consumer;

/**
 * Created on 07.06.2018.
 * <p>
 * StateMachine rollback wrapper which putting
 * the whole a processing function in a new transaction boundary.
 *
 * @author Korovin Anatoliy
 */
@Slf4j
public class StateMachineTransactionalWrapper<StatesT, EventsT> implements StateMachineWrapper<StatesT, EventsT> {

    private final StateMachineWrapper<StatesT, EventsT> stateMachineRollbackWrapper;
    private JpaTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public StateMachineTransactionalWrapper(
            @Qualifier("stateMachineRollbackWrapper")
                    StateMachineWrapper<StatesT, EventsT> stateMachineRollbackWrapper) {

        this.stateMachineRollbackWrapper = stateMachineRollbackWrapper;
    }

    @PostConstruct
    public void init() {
        this.transactionManager = new JpaTransactionManager();
        this.transactionManager.setEntityManagerFactory(em.getEntityManagerFactory());
    }

    @Override
    public void runWithRollback(StateMachine<StatesT, EventsT> stateMachine,
                                Consumer<StateMachine<StatesT, EventsT>> processingFunction) {

        Consumer<StateMachine<StatesT, EventsT>> safety = (machine) -> {
            runInTransaction(() -> processingFunction.accept(stateMachine));
        };

        stateMachineRollbackWrapper.runWithRollback(stateMachine, safety);
    }


    private void runInTransaction(Runnable runnable) {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            runnable.run();
            transactionManager.commit(status);
        } catch (Throwable e) {
            log.error("transaction rollback in wrapper: ", e);
            transactionManager.rollback(status);
            throw e;
        }
    }
}
