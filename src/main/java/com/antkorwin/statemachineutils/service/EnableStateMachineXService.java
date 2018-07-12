package com.antkorwin.statemachineutils.service;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Korovin Anatolii on 12.07.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(XServiceConfig.class)
public @interface EnableStateMachineXService {
}
