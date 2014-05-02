package org.github.alexwibowo.opentext.client;

/**
 * User: alexwibowo
 */
public abstract class VRDRuntimeException extends RuntimeException{

    protected VRDRuntimeException(String message) {
        super(message);
    }

    protected VRDRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}