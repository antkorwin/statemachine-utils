package com.antkorwin.statemachineutils.resolver;

import com.antkorwin.statemachineutils.config.Events;
import com.antkorwin.statemachineutils.config.StateMachineConfig;
import com.antkorwin.statemachineutils.config.States;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


/**
 * Created on 20.06.2018.
 *
 * @author Korovin Anatoliy
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Import(StateMachineConfig.class)
@EnableStateMachineResolver
public class StateMachineResolverTest {

    private StateMachine<States, Events> stateMachine;

    @Autowired
    private StateMachineFactory<States, Events> stateMachineFactory;

    @Autowired
    private StateMachineResolver<States, Events> stateMachineResolver;

    @Before
    public void setUp() throws Exception {
        stateMachine = stateMachineFactory.getStateMachine();
    }


    @Test
    public void testResolverWithoutGuard() {
        // Arrange
        // Act
        List<Events> availableEvents = stateMachineResolver.getAvailableEvents(stateMachine);
        // Asserts
        Assertions.assertThat(availableEvents)
                  .containsOnly(Events.START_FEATURE,
                                Events.ROCK_STAR_DOUBLE_TASK,
                                Events.DEPLOY,
                                Events.INCREMENT);
    }

    @Test
    public void testResolverWithGuard() {
        // Arrange
        stateMachine.sendEvent(Events.START_FEATURE);
        // Act
        List<Events> availableEvents = stateMachineResolver.getAvailableEvents(stateMachine);
        // Asserts
        Assertions.assertThat(availableEvents)
                  .containsOnly(Events.DEPLOY,
                                Events.INCREMENT);
    }
}