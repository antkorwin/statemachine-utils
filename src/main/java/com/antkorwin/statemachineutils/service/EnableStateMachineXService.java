package com.antkorwin.statemachineutils.service;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 12.07.2018.
 *
 * Annotation which imports configurations related to
 * create a XStateMachineService bean.
 *
 * @author Korovin Anatoliy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(XServiceConfig.class)
public @interface EnableStateMachineXService {
}
