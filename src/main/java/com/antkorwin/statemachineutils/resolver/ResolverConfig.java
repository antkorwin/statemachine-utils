package com.antkorwin.statemachineutils.resolver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 20.06.2018.
 *
 * @author Korovin Anatoliy
 */
@Configuration
public class ResolverConfig {

    @Bean
    public <S,E> StateMachineResolver<S,E> stateMachineResolver(){
        return new StateMachineResolverImpl<>();
    }
}
