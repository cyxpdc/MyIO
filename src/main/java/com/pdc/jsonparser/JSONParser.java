package com.pdc.jsonparser;

import com.pdc.jsonparser.io.CharReader;
import com.pdc.jsonparser.parser.Parser;
import com.pdc.jsonparser.parser.Tokenizer;
import com.pdc.jsonparser.storage.TokenStore;

import java.io.IOException;
import java.io.StringReader;

/**
 * author PDC
 */
public class JSONParser {
    private Tokenizer tokenizer = new Tokenizer();
    private Parser parser = new Parser();

    public Object fromJSON(String json) throws IOException {
        CharReader charReader = new CharReader(new StringReader(json));
        TokenStore tokens = tokenizer.getTokenStream(charReader);
        return parser.parse(tokens);
    }
}
