package com.pdc.jsonparser.storage;

import com.pdc.jsonparser.token.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * 存储解析出来的token流
 * author PDC
 */
public class TokenStore {
    private List<Token> tokens = new ArrayList<>();
    private int index = 0;

    public void add(Token token) {
        tokens.add(token);
    }

    public Token peek() {
        return index < tokens.size() ? tokens.get(index) : null;
    }

    public Token peekPrevious() {
        return index - 1 < 0 ? null : tokens.get(index - 2);
    }

    public Token next() {
        return tokens.get(index++);
    }

    public boolean hasMore() {
        return index < tokens.size();
    }

    @Override
    public String toString() {
        return "TokenList{" +
                "tokens=" + tokens +
                '}';
    }
}
