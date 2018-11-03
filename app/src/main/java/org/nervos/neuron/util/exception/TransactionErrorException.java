package org.nervos.neuron.util.exception;

public class TransactionErrorException extends Exception {

    public TransactionErrorException() {
        super();
    }

    public TransactionErrorException(String message) {
        super(message);
    }

    public TransactionErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionErrorException(Throwable cause) {
        super(cause);
    }

}
