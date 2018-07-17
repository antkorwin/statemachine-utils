package com.antkorwin.statemachineutils.service;

import com.antkorwin.commonutils.exceptions.BaseException;
import com.antkorwin.commonutils.validation.ErrorInfo;

/**
 * Created on 18.07.2018.
 *
 * @author Korovin Anatoliy
 */
public class XStateMachineException extends BaseException {
    public XStateMachineException(String message, Integer code) {
        super(message, code);
    }

    public XStateMachineException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public XStateMachineException(Throwable cause) {
        super(cause);
    }

    public XStateMachineException(String message, Integer code, Throwable cause) {
        super(message, code, cause);
    }

    public XStateMachineException(ErrorInfo errorInfo, Throwable cause) {
        super(errorInfo, cause);
    }
}
