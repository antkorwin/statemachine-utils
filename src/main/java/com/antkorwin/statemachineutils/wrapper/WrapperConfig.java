package com.antkorwin.statemachineutils.wrapper;

import com.antkorwin.xsync.XSync;
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
    public XSync<UUID> stateMachineXSync() {
        return new XSync<>();
    }

    @Bean("stateMachineRollbackWrapper")
    public StateMachineWrapper stateMachineWrapper(XSync<UUID> stateMachineXSync) {
        return new StateMachineRollbackWrapper<>(stateMachineXSync);
    }
}
