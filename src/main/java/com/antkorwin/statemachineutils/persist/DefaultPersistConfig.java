package com.antkorwin.statemachineutils.persist;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.StateMachinePersister;

import java.util.UUID;

/**
 * Created by Korovin Anatolii on 11.07.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
@Configuration
public class DefaultPersistConfig {

    @Bean
    @ConditionalOnMissingBean
    public <StatesT, EventsT> StateMachinePersist<StatesT, EventsT, UUID> persist() {
        return new InMemoryStateMachinePersist();
    }

    @Bean
    public <StatesT, EventsT> StateMachinePersister<StatesT, EventsT, UUID> persister(
            StateMachinePersist<StatesT, EventsT, UUID> defaultPersist) throws Exception {

        return new DefaultStateMachinePersisterDecorator<>(defaultPersist);
    }
}
