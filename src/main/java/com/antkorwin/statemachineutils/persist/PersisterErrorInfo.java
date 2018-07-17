package com.antkorwin.statemachineutils.persist;

import com.antkorwin.commonutils.validation.ErrorInfo;
import com.antkorwin.statemachineutils.service.ErrorInfoUnique;

/**
 * Created on 10.07.2018.
 *
 * @author Korovin Anatoliy
 */
@ErrorInfoUnique
public enum PersisterErrorInfo implements ErrorInfo{
    COULD_NOT_READ_STATEMACHINE_FROM_PERSIST("could not read the StateMachine form the Persist");

    private static final int BASE = 2000;
    private String msg;

    PersisterErrorInfo(String msg) {
        this.msg = msg;
    }

    @Override
    public String getMessage() {
        return this.msg;
    }

    @Override
    public Integer getCode() {
        return BASE + ordinal();
    }
}
