package com.antkorwin.statemachineutils.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

import java.util.Optional;

/**
 * Created on 05.05.2018.
 *
 * @author Korovin Anatoliy
 */
@Slf4j
@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
        config.withConfiguration()
              .autoStartup(true);
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
        states.withStates()
              .initial(States.BACKLOG)
              .state(States.IN_PROGRESS)
              .state(States.TESTING)
              .state(States.DONE)
              .end(States.DONE);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
        transitions.withExternal()
                   .source(States.BACKLOG)
                   .target(States.IN_PROGRESS)
                   .event(Events.START_FEATURE)
                   .and()
                   // DEVELOPERS:
                   .withExternal()
                   .source(States.IN_PROGRESS)
                   .target(States.TESTING)
                   .event(Events.FINISH_FEATURE)
                   .guard(alreadyDeployedGuard())
                   .and()
                   // QA-TEAM:
                   .withExternal()
                   .source(States.TESTING)
                   .target(States.DONE)
                   .event(Events.QA_CHECKED_UC)
                   .and()
                   .withExternal()
                   .source(States.TESTING)
                   .target(States.IN_PROGRESS)
                   .event(Events.QA_REJECTED_UC)
                   .and()
                   // ROCK-STAR:
                   .withExternal()
                   .source(States.BACKLOG)
                   .target(States.TESTING)
                   .event(Events.ROCK_STAR_DOUBLE_TASK)
                   .and()
                   // DEVOPS:
                   .withInternal()
                   .source(States.IN_PROGRESS)
                   .event(Events.DEPLOY)
                   .action(deployPreProd())
                   .and()
                   .withInternal()
                   .source(States.BACKLOG)
                   .event(Events.DEPLOY)
                   .action(deployPreProd())
                   .and()
                   .withInternal()
                   .source(States.BACKLOG)
                   .event(Events.INCREMENT)
                   .action(increment())
                   .and()
                   .withInternal()
                   .source(States.IN_PROGRESS)
                   .event(Events.INCREMENT)
                   .action(increment());
    }

    private Guard<States, Events> alreadyDeployedGuard() {
        return context -> Optional.ofNullable(context.getExtendedState().getVariables().get("deployed"))
                                  .map(v -> (boolean) v)
                                  .orElse(false);
    }

    private Action<States, Events> deployPreProd() {
        return stateContext -> {
            log.warn("DEPLOY: Выкатываемся на препродакшен.");
            stateContext.getExtendedState().getVariables().put("deployed", true);
        };
    }

    private Action<States, Events> increment() {
        return context -> {
            int var = Optional.ofNullable(context.getExtendedState().getVariables().get("counter"))
                              .map(v -> (int) v)
                              .orElse(0);
            context.getExtendedState().getVariables().put("counter", var + 1);
        };
    }
}
