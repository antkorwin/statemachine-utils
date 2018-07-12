package com.antkorwin.statemachineutils.service;

import com.antkorwin.statemachineutils.persist.DefaultPersistConfig;
import com.antkorwin.statemachineutils.wrapper.EnableStateMachineWrapper;
import com.antkorwin.statemachineutils.wrapper.StateMachineWrapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;

import java.util.UUID;

/**
 * Created by Korovin Anatolii on 09.07.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
@Configuration
@EnableStateMachineWrapper
@Import(DefaultPersistConfig.class)
public class XServiceConfig {

    @Bean
    public <StatesT, EventsT> XStateMachineService<StatesT, EventsT> xStateMachineService(
            @Qualifier("stateMachineRollbackWrapper")
                    StateMachineWrapper<StatesT, EventsT> stateMachineRollbackWrapper,
            @Qualifier("stateMachineTransactionalWrapper")
                    StateMachineWrapper<StatesT, EventsT> stateMachineTransactionalWrapper,
            StateMachineFactory<StatesT, EventsT> stateMachineFactory,
            StateMachinePersister<StatesT, EventsT, UUID> persister) {

        return new XStateMachineServiceImpl<StatesT, EventsT>(stateMachineRollbackWrapper,
                                                              stateMachineTransactionalWrapper,
                                                              persister,
                                                              stateMachineFactory);
    }

}
