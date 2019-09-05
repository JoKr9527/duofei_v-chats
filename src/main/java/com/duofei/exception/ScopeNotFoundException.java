package com.duofei.exception;

/**
 * 无法找到域异常
 * @author duofei
 * @date 2019/9/3
 */
public class ScopeNotFoundException extends ApplicationException {
    public ScopeNotFoundException() {
        super();
    }

    public ScopeNotFoundException(String message) {
        super(message);
    }

    public ScopeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScopeNotFoundException(Throwable cause) {
        super(cause);
    }

    public ScopeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
