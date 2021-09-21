package com.antkorwin.statemachineutils.service;

import com.antkorwin.statemachineutils.persist.DefaultPersistConfig;
import com.antkorwin.statemachineutils.resolver.EnableStateMachineResolver;
import com.antkorwin.statemachineutils.resolver.StateMachineResolver;
import com.antkorwin.statemachineutils.service.usecases.CreateStateMachineUseCase;
import com.antkorwin.statemachineutils.service.usecases.GetStateMachineUseCase;
import com.antkorwin.statemachineutils.service.usecases.UpdateStateMachineUseCase;
import com.antkorwin.statemachineutils.wrapper.EnableStateMachineWrapper;
import com.antkorwin.statemachineutils.wrapper.StateMachineWrapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;

/**
 * Created on 09.07.2018.
 *
 * @author Korovin Anatoliy
 */
@Configuration
@EnableStateMachineWrapper
@EnableStateMachineResolver
@Import(DefaultPersistConfig.class)
public class XServiceConfig {

	@Bean
	public <StatesT, EventsT> XStateMachineService<StatesT, EventsT> xStateMachineService(
			@Qualifier("stateMachineRollbackWrapper")
					StateMachineWrapper<StatesT, EventsT> stateMachineRollbackWrapper,
			@Qualifier("stateMachineTransactionalWrapper")
					StateMachineWrapper<StatesT, EventsT> stateMachineTransactionalWrapper,
			StateMachineFactory<StatesT, EventsT> stateMachineFactory,
			StateMachinePersister<StatesT, EventsT, String> persister,
			StateMachinePersist<StatesT, EventsT, String> persist,
			StateMachineResolver<StatesT, EventsT> resolver) {

		CreateStateMachineUseCase<StatesT, EventsT> createStateMachineUseCase =
				new CreateStateMachineUseCase<>(persister,
				                                stateMachineFactory,
				                                stateMachineRollbackWrapper,
				                                stateMachineTransactionalWrapper);

		GetStateMachineUseCase<StatesT, EventsT> getStateMachineUseCase =
				new GetStateMachineUseCase<>(persister,
				                             persist,
				                             stateMachineFactory,
				                             resolver);

		UpdateStateMachineUseCase<StatesT, EventsT> updateStateMachineUseCase =
				new UpdateStateMachineUseCase<>(stateMachineRollbackWrapper,
				                                stateMachineTransactionalWrapper,
				                                persister,
				                                persist,
				                                stateMachineFactory,
				                                resolver,
				                                getStateMachineUseCase);

		return new XStateMachineServiceImpl<>(createStateMachineUseCase,
		                                      getStateMachineUseCase,
		                                      updateStateMachineUseCase);
	}

}
