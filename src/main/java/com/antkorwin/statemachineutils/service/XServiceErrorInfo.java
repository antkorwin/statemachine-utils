package com.antkorwin.statemachineutils.service;

import com.antkorwin.commonutils.validation.ErrorInfo;

/**
 * Created on 18.07.2018.
 *
 * @author Korovin Anatoliy
 */
@ErrorInfoUnique
public enum XServiceErrorInfo implements ErrorInfo {

    UNABLE_TO_PERSIST_NEW_STATE_MACHINE("Unable to persist new state machine."),
    UNABLE_TO_READ_STATE_MACHINE_FROM_STORE("Unable to read state machine from storage."),
    UNABLE_TO_PERSIST_STATE_MACHINE_DURING_UPDATE("Unable to persist the state machine during the update");

    private static final int BASE = 3000;
    private final String message;

    XServiceErrorInfo(String msg) {
        this.message = msg;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public Integer getCode() {
        return BASE + ordinal();
    }
}
