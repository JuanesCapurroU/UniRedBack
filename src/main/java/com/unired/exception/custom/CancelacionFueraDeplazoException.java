package com.unired.exception.custom;

public class CancelacionFueraDeplazoException extends RuntimeException {

    public CancelacionFueraDeplazoException(String message) {
        super(message);
    }
}
