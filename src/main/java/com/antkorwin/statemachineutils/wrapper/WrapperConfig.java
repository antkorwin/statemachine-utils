package com.antkorwin.statemachineutils.wrapper;

import com.antkorwin.statemachineutils.wrapper.impl.StateMachineRollbackWrapper;
import com.antkorwin.statemachineutils.wrapper.impl.StateMachineTransactionalWrapper;
import com.antkorwin.xsync.XSync;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
//	@ConditionalOnMissingBean
	public StateMachineWrapper stateMachineWrapper(XSync<String> stateMachineXSync) {
		return new StateMachineRollbackWrapper<>(stateMachineXSync);
	}

	@Bean("stateMachineTransactionalWrapper")
//	@ConditionalOnProperty(value = "antkorwin.statemachine.rollback", havingValue = "transactional")
	public <StatesT, EventsT> StateMachineWrapper stateMachineTransactionalWrapper(
			@Qualifier("stateMachineRollbackWrapper")
					StateMachineWrapper<StatesT, EventsT> stateMachineRollbackWrapper) {

		return new StateMachineTransactionalWrapper<>(stateMachineRollbackWrapper);
	}
}
