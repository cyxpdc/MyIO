package com.pdc.jsonparser.parser;

import com.pdc.jsonparser.exception.JsonParseException;
import com.pdc.jsonparser.storage.TokenStore;
import com.pdc.jsonparser.style.JsonArray;
import com.pdc.jsonparser.style.JsonObject;
import com.pdc.jsonparser.token.Token;
import com.pdc.jsonparser.token.TokenType;

/**
 * author PDC
 */
public class Parser {
    private static final int BEGIN_OBJECT_TOKEN = 1;// {
    private static final int END_OBJECT_TOKEN = 2;//{
    private static final int BEGIN_ARRAY_TOKEN = 4;// [
    private static final int END_ARRAY_TOKEN = 8;// ]
    private static final int NULL_TOKEN = 16;// null
    private static final int NUMBER_TOKEN = 32;// 数字
    private static final int STRING_TOKEN = 64;// 字符串
    private static final int BOOLEAN_TOKEN = 128;// true or false
    private static final int SEPARATE_COLON_TOKEN = 256;// ：
    private static final int SEPARATE_COMMA_TOKEN = 512;// ，
    private static final int END_DOCUMENT_TOKEN = 1024;//表示JSON数据结束了

    private TokenStore tokens;

    public Object parse(TokenStore tokens) {
        this.tokens = tokens;
        System.out.println(tokens);
        return parse();
    }

    private Object parse() {
        Token token = tokens.next();
        if (token == null) {
            return new JsonObject();
        } else if (token.getTokenType() == TokenType.BEGIN_OBJECT) {// {
            return parseJsonObject();
        } else if (token.getTokenType() == TokenType.BEGIN_ARRAY) {// [
            return parseJsonArray();
        } else {
            throw new JsonParseException("Parse error, invalid Token.");
        }
    }

    private JsonObject parseJsonObject() {
        JsonObject jsonObject = new JsonObject();
        int expectToken = STRING_TOKEN | END_OBJECT_TOKEN;//希望为"或}
        String key = null;
        Object value = null;
        while (tokens.hasMore()) {
            //得到token的信息
            Token token = tokens.next();
            TokenType tokenType = token.getTokenType();
            String tokenValue = token.getValue();
            System.out.println("tokenType:  "+tokenType+","+"  tokenValue:  "+tokenValue);
            //根据token类型来封装JsonObject
            switch (tokenType) {
                case BEGIN_OBJECT://{
                    checkExpectToken(tokenType, expectToken);
                    //遇到{，跟parse中的情况一样递归解析jsonObject
                    jsonObject.put(key, parseJsonObject());
                    expectToken = SEPARATE_COMMA_TOKEN | END_OBJECT_TOKEN;//, }
                    break;
                case END_OBJECT://}
                    checkExpectToken(tokenType, expectToken);
                    return jsonObject;
                case BEGIN_ARRAY://[ 解析jsonArray
                    checkExpectToken(tokenType, expectToken);
                    jsonObject.put(key, parseJsonArray());//递归解析jsonArray
                    expectToken = SEPARATE_COMMA_TOKEN | END_OBJECT_TOKEN;//,|}
                    break;
                case NULL://null
                    checkExpectToken(tokenType, expectToken);
                    jsonObject.put(key, null);
                    expectToken = SEPARATE_COMMA_TOKEN | END_OBJECT_TOKEN;
                    break;
                case NUMBER://数字
                    checkExpectToken(tokenType, expectToken);
                    objectHandlerDigit(jsonObject, key, tokenValue);
                    expectToken = SEPARATE_COMMA_TOKEN | END_OBJECT_TOKEN;
                    break;
                case BOOLEAN://true或false
                    checkExpectToken(tokenType, expectToken);
                    jsonObject.put(key, Boolean.valueOf(token.getValue()));
                    expectToken = SEPARATE_COMMA_TOKEN | END_OBJECT_TOKEN;
                    break;
                case STRING://字符串
                    checkExpectToken(tokenType, expectToken);
                    expectToken = objectHandleString(token,expectToken,jsonObject,key,value);
                    break;
                case SEPARATE_COLON://：
                    checkExpectToken(tokenType, expectToken);
                    expectToken = NULL_TOKEN | NUMBER_TOKEN | BOOLEAN_TOKEN | STRING_TOKEN
                            | BEGIN_OBJECT_TOKEN | BEGIN_ARRAY_TOKEN;
                    break;
                case SEPARATE_COMMA://，
                    checkExpectToken(tokenType, expectToken);
                    expectToken = STRING_TOKEN;
                    break;
                case END_DOCUMENT://结束
                    checkExpectToken(tokenType, expectToken);
                    return jsonObject;
                default:
                    throw new JsonParseException("Unexpected Token.");
            }
        }
        throw new JsonParseException("Parse error, invalid Token.");
    }

    private void objectHandlerDigit(JsonObject jsonObject, String key, String tokenValue) {
        //浮点数或整数，整数又要看long是否能变为int
        if (tokenValue.contains(".") || tokenValue.contains("e") || tokenValue.contains("E")) {
            jsonObject.put(key, Double.valueOf(tokenValue));
        } else {
            Long num = Long.valueOf(tokenValue);
            if (num > Integer.MAX_VALUE || num < Integer.MIN_VALUE) {
                jsonObject.put(key, num);
            } else {
                jsonObject.put(key, num.intValue());
            }
        }
    }
    /**
     * 在 JSON 中，字符串既可以作为键，也可作为值。
     * 作为键时(preToken为")，只期待下一个 Token 类型为SEPARATE_COLON。
     * 作为值时(preToken为:)，期待下一个 Token 类型为 SEPARATE_COMMA或END_OBJECT
     */
    private int objectHandleString(Token token, int expectToken, JsonObject jsonObject, String key, Object value){
        Token preToken = tokens.peekPrevious();
        if (preToken.getTokenType() == TokenType.SEPARATE_COLON) {
            value = token.getValue();
            jsonObject.put(key, value);
            expectToken = SEPARATE_COMMA_TOKEN | END_OBJECT_TOKEN;
        } else {
            key = token.getValue();
            expectToken = SEPARATE_COLON_TOKEN;
        }
        return expectToken;
    }

    private JsonArray parseJsonArray() {
        //[ ] { null 数字 boolean 字符串
        int expectToken = BEGIN_ARRAY_TOKEN | END_ARRAY_TOKEN | BEGIN_OBJECT_TOKEN | NULL_TOKEN
                | NUMBER_TOKEN | BOOLEAN_TOKEN | STRING_TOKEN;
        JsonArray jsonArray = new JsonArray();
        while (tokens.hasMore()) {
            Token token = tokens.next();
            TokenType tokenType = token.getTokenType();
            String tokenValue = token.getValue();
            switch (tokenType) {
                case BEGIN_OBJECT:
                    checkExpectToken(tokenType, expectToken);
                    jsonArray.add(parseJsonObject());//递归
                    expectToken = SEPARATE_COMMA_TOKEN | END_ARRAY_TOKEN;
                    break;
                case BEGIN_ARRAY:
                    checkExpectToken(tokenType, expectToken);
                    jsonArray.add(parseJsonArray());//递归
                    expectToken = SEPARATE_COMMA_TOKEN | END_ARRAY_TOKEN;
                    break;
                case END_ARRAY:
                    checkExpectToken(tokenType, expectToken);
                    return jsonArray;
                case NULL:
                    checkExpectToken(tokenType, expectToken);
                    jsonArray.add(null);
                    expectToken = SEPARATE_COMMA_TOKEN | END_ARRAY_TOKEN;
                    break;
                case NUMBER:
                    checkExpectToken(tokenType, expectToken);
                    arrayHandleDigit(jsonArray, tokenValue);
                    expectToken = SEPARATE_COMMA_TOKEN | END_ARRAY_TOKEN;
                    break;
                case BOOLEAN:
                    checkExpectToken(tokenType, expectToken);
                    jsonArray.add(Boolean.valueOf(tokenValue));
                    expectToken = SEPARATE_COMMA_TOKEN | END_ARRAY_TOKEN;
                    break;
                case STRING:
                    checkExpectToken(tokenType, expectToken);
                    jsonArray.add(tokenValue);
                    expectToken = SEPARATE_COMMA_TOKEN | END_ARRAY_TOKEN;
                    break;
                case SEPARATE_COMMA:
                    checkExpectToken(tokenType, expectToken);
                    expectToken = STRING_TOKEN | NULL_TOKEN | NUMBER_TOKEN | BOOLEAN_TOKEN
                            | BEGIN_ARRAY_TOKEN | BEGIN_OBJECT_TOKEN;
                    break;
                case END_DOCUMENT:
                    checkExpectToken(tokenType, expectToken);
                    return jsonArray;
                default:
                    throw new JsonParseException("Unexpected Token.");
            }
        }
        throw new JsonParseException("Parse error, invalid Token.");
    }

    private void arrayHandleDigit(JsonArray jsonArray, String tokenValue) {
        if (tokenValue.contains(".") || tokenValue.contains("e") || tokenValue.contains("E")) {
            jsonArray.add(Double.valueOf(tokenValue));
        } else {
            Long num = Long.valueOf(tokenValue);
            if (num > Integer.MAX_VALUE || num < Integer.MIN_VALUE) {
                jsonArray.add(num);
            } else {
                jsonArray.add(num.intValue());
            }
        }
    }

    private void checkExpectToken(TokenType tokenType, int expectToken) {
        if ((tokenType.getTokenCode() & expectToken) == 0) {
            throw new JsonParseException("Parse error, invalid Token.");
        }
    }
}
