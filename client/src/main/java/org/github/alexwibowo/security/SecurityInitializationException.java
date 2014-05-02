package org.github.alexwibowo.security;

/**
 * An exception to indicate that there is an error during the initialization process of the security module.
 * <p/>
 * User: alexwibowo
 */
public class SecurityInitializationException extends SecurityException {

    public SecurityInitializationException(String msg) {
        super(msg);
    }

    public SecurityInitializationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
