package com.pdc.jsonparser.exception;

/**
 * 自定义json解析异常，方便debug
 * author PDC
 */
public class JsonParseException extends  RuntimeException{

    public JsonParseException(String message) {
        super(message);
    }
}
