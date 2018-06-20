package com.antkorwin.statemachineutils.wrapper;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Korovin Anatolii on 20.06.2018.
 *
 * Annotation which imports configurations related to
 * create a StateMachineWrapper bean.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(WrapperConfig.class)
public @interface EnableStateMachineWrapper {
}
