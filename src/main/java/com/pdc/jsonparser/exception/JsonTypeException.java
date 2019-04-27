package com.pdc.jsonparser.exception;

/**
 * 自定义json类型异常，方便debug
 * author PDC
 */
public class JsonTypeException extends RuntimeException{

    public JsonTypeException(String message) {
        super(message);
    }
}
