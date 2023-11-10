package com.example.backend.exception;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/30
 */
public class PluginException  extends RuntimeException{

    private static final long serialVersionUID = 267820398117258766L;

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
