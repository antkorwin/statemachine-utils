package com.antkorwin.statemachineutils.resolver;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 20.06.2018.
 *
 * Annotation which imports configurations related to
 * create a StateMachineResolver bean
 *
 * @author Korovin Anatoliy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ResolverConfig.class)
public @interface EnableStateMachineResolver {

}
