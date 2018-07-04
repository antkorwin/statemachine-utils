package com.antkorwin.statemachineutils.wrapper;

import com.antkorwin.commonutils.validation.ErrorInfo;

/**
 * Created by Korovin Anatolii on 05.07.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
public enum StateMachineWrapperErrorInfo implements ErrorInfo {
    STATE_MACHINE_IS_MANDATORY_ARGUMENT("state machine is a mandatory argument for this method in a wrapper."),
    PROCESSING_FUNCTION_IS_MANDATORY_ARGUMENT("processing function is a mandatory argument for this method in a wrapper");

    private static final int BASE = 1000;
    private String msg;

    StateMachineWrapperErrorInfo(String msg) {
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
