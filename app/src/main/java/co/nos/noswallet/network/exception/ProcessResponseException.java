package co.nos.noswallet.network.exception;

import co.nos.noswallet.network.nosModel.ProcessResponse;

public class ProcessResponseException extends Exception {
    public ProcessResponseException(ProcessResponse processResponse){
        super(String.valueOf(processResponse));
    }
}
