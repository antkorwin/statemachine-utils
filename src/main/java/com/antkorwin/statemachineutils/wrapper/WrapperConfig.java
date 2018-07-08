package com.antkorwin.statemachineutils.wrapper;

import com.antkorwin.statemachineutils.wrapper.impl.StateMachineRollbackWrapper;
import com.antkorwin.statemachineutils.wrapper.impl.StateMachineTransactionalWrapper;
import com.antkorwin.xsync.XSync;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * Created on 20.06.2018.
 *
 * @author Korovin Anatoliy
 */
@Configuration
public class WrapperConfig {

    @Bean
    public XSync<String> stateMachineXSync() {
        return new XSync<>();
    }

    @Bean("stateMachineRollbackWrapper")
    public StateMachineWrapper stateMachineWrapper(XSync<String> stateMachineXSync) {
        return new StateMachineRollbackWrapper<>(stateMachineXSync);
    }

    @Bean("stateMachineTransactionalWrapper")
    public <StatesT, EventsT> StateMachineWrapper stateMachineTransactionalWrapper(
            @Qualifier("stateMachineRollbackWrapper")
                    StateMachineWrapper<StatesT, EventsT> stateMachineRollbackWrapper) {

        return new StateMachineTransactionalWrapper<>(stateMachineRollbackWrapper);
    }
}
