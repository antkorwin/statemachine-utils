package com.antkorwin.statemachineutils.service;

import java.util.UUID;

import com.antkorwin.statemachineutils.TransactionalTestConfig;
import com.antkorwin.statemachineutils.config.Events;
import com.antkorwin.statemachineutils.config.StateMachineConfig;
import com.antkorwin.statemachineutils.config.States;
import com.antkorwin.statemachineutils.persist.StateMachineContextEvaluator;
import com.antkorwin.statemachineutils.wrapper.EnableStateMachineWrapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created on 09.07.2018.
 *
 * @author Korovin Anatoliy
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@Import({StateMachineConfig.class, TransactionalTestConfig.class})
@EnableStateMachineWrapper
@TestPropertySource(properties = "antkorwin.statemachine.rollback=transactional")
public class XStateMachineServiceImplTransactionalTest {

	private static final String PERSISTED_MACHINE_ID = UUID.randomUUID().toString();

	@Autowired
	private XStateMachineService<States, Events> xStateMachineService;

	@Autowired
	private StateMachineFactory<States, Events> factory;

	@Autowired
	private StateMachinePersister<States, Events, String> persister;

	private StateMachine<States, Events> mockMachine;

	@Autowired
	private TransactionalTestConfig.TestService testService;

	@Before
	public void setUp() throws Exception {
		testService.clear();
		mockMachine = factory.getStateMachine(PERSISTED_MACHINE_ID);
		persister.persist(mockMachine, PERSISTED_MACHINE_ID);
	}


	@Test
	public void testEvaluateTransactionalWithChangeStateAndFailOnTransactionCommit() {

		// Act
		Exception actualException = null;
		try {
			StateMachine<States, Events> machine =
					xStateMachineService.evaluate(PERSISTED_MACHINE_ID,
					                              stateMachine -> {
						                              stateMachine.sendEvent(Events.START_FEATURE);
						                              testService.ok();
						                              testService.fail();
						                              return stateMachine;
					                              });
		} catch (Exception e) {
			actualException = e;
		}

		// Asserts
		assertThat(actualException.getMessage())
				.contains("not-null property references a null or transient value");
		assertThat(actualException)
				.isInstanceOf(DataIntegrityViolationException.class);

		// Read a result from the storage
		StateMachine<States, Events> machine = xStateMachineService.get(PERSISTED_MACHINE_ID);

		assertThat(machine.getState().getId()).isEqualTo(States.BACKLOG);

		// Check that entity not save in database
		assertThat(testService.size()).isEqualTo(0);
	}

	@Test
	public void testEvaluateTransactionalSuccess() {

		// Act
		StateMachine<States, Events> machine =
				xStateMachineService.evaluate(PERSISTED_MACHINE_ID,
				                              stateMachine -> {
					                              stateMachine.sendEvent(Events.START_FEATURE);
					                              testService.ok();
					                              return stateMachine;
				                              });

		// Read a machine from the storage
		StateMachine<States, Events> persistedMachine = xStateMachineService.get(PERSISTED_MACHINE_ID);

		// Asserts
		assertThat(machine.getState().getId())
				.isEqualTo(States.IN_PROGRESS);

		assertThatMachinesEqual(persistedMachine, machine);

		// Check that entity not save in database
		assertThat(testService.size()).isEqualTo(1);
	}

	@Test
	public void testEvaluateWithRollbackWhileTryingToPersistMachineInProcessingFunction() {

		// Act
		Exception actualException = null;
		try {
			StateMachine<States, Events> machine =
					xStateMachineService.evaluate(PERSISTED_MACHINE_ID,
					                              stateMachine -> {
						                              stateMachine.sendEvent(Events.START_FEATURE);
						                              try {
							                              persister.persist(stateMachine, PERSISTED_MACHINE_ID);
						                              } catch (Exception e) {
							                              e.printStackTrace();
							                              Assertions.fail(e.getMessage());
						                              }
						                              testService.ok();
						                              testService.fail();
						                              return stateMachine;
					                              });
		} catch (Exception e) {
			actualException = e;
		}

		// Asserts
		assertThat(actualException.getMessage())
				.contains("not-null property references a null or transient value");
		assertThat(actualException)
				.isInstanceOf(DataIntegrityViolationException.class);

		// Read a result from the storage
		StateMachine<States, Events> machine = xStateMachineService.get(PERSISTED_MACHINE_ID);

		assertThat(machine.getState().getId()).isEqualTo(States.BACKLOG);

		// Check that entity not save in database
		assertThat(testService.size()).isEqualTo(0);
	}


	private void assertThatMachinesEqual(StateMachine<States, Events> firstMachine,
	                                     StateMachine<States, Events> secondMachine) {
		// Arrange
		StateMachineContext<States, Events> firstContext =
				StateMachineContextEvaluator.getContext(firstMachine);

		StateMachineContext<States, Events> secondContext =
				StateMachineContextEvaluator.getContext(secondMachine);

		// Assert
		assertThat(firstContext).isEqualToComparingFieldByFieldRecursively(secondContext);
	}
}