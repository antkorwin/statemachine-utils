package com.antkorwin.statemachineutils.wrapper;

import com.antkorwin.statemachineutils.wrapper.impl.StateMachineRollbackWrapper;
import com.antkorwin.statemachineutils.wrapper.impl.StateMachineTransactionalWrapper;
import com.antkorwin.xsync.XSync;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
	@ConditionalOnProperty(name = "antkorwin.statemachine.rollback", havingValue = "default", matchIfMissing = true)
	public <StatesT, EventsT> StateMachineWrapper<StatesT, EventsT> stateMachineWrapper(XSync<String> stateMachineXSync) {
		return new StateMachineRollbackWrapper<>(stateMachineXSync);
	}

	@Primary
	@Bean("stateMachineRollbackWrapper")
	@ConditionalOnProperty(value = "antkorwin.statemachine.rollback", havingValue = "transactional")
	public <StatesT, EventsT> StateMachineWrapper<StatesT, EventsT> stateMachineTransactionalWrapper(XSync<String> stateMachineXSync) {
		StateMachineRollbackWrapper<StatesT, EventsT> rollbackWrapper = new StateMachineRollbackWrapper<>(stateMachineXSync);
		return new StateMachineTransactionalWrapper<>(rollbackWrapper);
	}
}
