package com.pdc.jsonparser.parser;

import com.pdc.jsonparser.exception.JsonParseException;
import com.pdc.jsonparser.io.CharReader;
import com.pdc.jsonparser.storage.TokenStore;
import com.pdc.jsonparser.token.Token;
import com.pdc.jsonparser.token.TokenType;

import java.io.IOException;

/**
 * JSON词法分析器
 * 把json串和相关读取操作封装在charReader里面，体现单一职责原则
 * author PDC
 */
public class Tokenizer {
    private CharReader charReader;
    private TokenStore tokenStore;;

    public Tokenizer() {
        tokenStore = new TokenStore();
    }

    public TokenStore getTokenStream(CharReader readerChar) throws IOException {
        this.charReader = readerChar;
        //tokenStore = new TokenStore();
        // 词法解析，获取token流，存储在tokenStore种
        tokenizer();
        return tokenStore;
    }

    /**
     * 将JSON字符串解析成token流
     * @throws IOException
     */
    private void tokenizer() throws IOException {
        Token token;
        do {
            token = start();
            tokenStore.add(token);
        } while (token.getTokenType() != TokenType.END_DOCUMENT);
    }

    /**
     * 解析过程的具体实现方法
     * 核心思想：
     * 通过一个死循环不停的读取字符
     * 然后再根据字符值，执行不同的处理函数。
     * @return
     * @throws IOException
     * @throws JsonParseException
     */
    private Token start() throws IOException, JsonParseException {
        char ch;
        // 先读一个字符，若为空白符（ASCII码在[0, 20H]上）则接着读
        // 直到刚读的字符为非空白字符，开始判断
        while (true){
            if (!charReader.hasMore()) {
                return new Token(TokenType.END_DOCUMENT, null);
            }
            ch = charReader.next();
            if (!isWhiteSpace(ch)) {
                break;
            }
        }
        //已保证为非空白字符
        //根据字符获取对应的token
        Token token = getToken(ch);
        if (token != null) return token;

        if (isDigit(ch)) {
            return readNumber();
        }

        throw new JsonParseException("Illegal character");
    }
    /**       以下方法用来判断所属数据类型是否合法          */
    // 判断一个字符是否属于空白字符
    private boolean isWhiteSpace(Character ch) {
        return (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n');
    }

    private Token getToken(Character ch) throws IOException {
        switch (ch) {
            case '{':
                return new Token(TokenType.BEGIN_OBJECT, String.valueOf(ch));
            case '}':
                return new Token(TokenType.END_OBJECT, String.valueOf(ch));
            case '[':
                return new Token(TokenType.BEGIN_ARRAY, String.valueOf(ch));
            case ']':
                return new Token(TokenType.END_ARRAY, String.valueOf(ch));
            case ',':
                return new Token(TokenType.SEPARATE_COMMA, String.valueOf(ch));
            case ':':
                return new Token(TokenType.SEPARATE_COLON, String.valueOf(ch));
            case 'n':
                return readNull();
            case 't': case 'f':
                return readBoolean();
            case '"'://开始键/值的分析
                return readString();
            case '-':
                return readNumber();
        }
        return null;
    }

    /**
     * JSON中允许出现的转义字符有以下九种：\\"  \\\  \\b  \\f  \\n  \\r  \\t  \\u  \\/
     * 处理Unicode编码时要特别注意一下u的后面会出现四位十六进制数
     * 当读取到一个双引号或者读取到了非法字符（’\r’或’、’\n’）循环退出。
     * @return Token
     * @throws IOException
     */
    private Token readString() throws IOException {
        StringBuilder sb = new StringBuilder();
        while(true) {
            char ch = charReader.next();
            if (ch == '\\') {// 处理转义字符
                if (!isEscape()) {
                    throw new JsonParseException("Invalid escape character");
                }
                sb.append('\\');
                ch = charReader.curPos();
                sb.append(ch);
                //处理Unicode编码，形如\u4e2d。且只支持\u0000~ \uFFFF范围内的编码
                if (ch == 'u') {
                    for (int i = 0; i < 4; i++) {
                        ch = charReader.next();
                        if (isHex(ch)) {
                            sb.append(ch);
                        } else {
                            throw new JsonParseException("Invalid character");
                        }
                    }
                }
            }
            else if (ch == '"') {// 碰到另一个双引号，则字符串解析结束，返回Token
                return new Token(TokenType.STRING, sb.toString());
            }
            else if (ch == '\r' || ch == '\n') {//传入的JSON字符串不允许换行，报错
                throw new JsonParseException("Invalid character");
            }
            else {//常规字符，直接append
                sb.append(ch);
            }
        }
    }

    /**
     * 判断是否有乱传转义字符
     * @return
     * @throws IOException
     */
    private boolean isEscape() throws IOException {
        Character ch = charReader.next();//得到\\的下一个字符，组成转义
        return (ch == '"' || ch == '\\' || ch == 'u' || ch == 'r'
                || ch == 'n' || ch == 'b' || ch == 't' || ch == 'f' || ch == '/');
    }
    /**
     * 判断是否是十六进制数
     * @param ch
     * @return
     */
    private boolean isHex(char ch) {
        return ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f')
                || (ch >= 'A' && ch <= 'F'));
    }
    /**
     * 判断是否是整数
     * 分别处理负数、小数、正数，每种情况都需要判断是否有指数
     * 负数里还要判断小数、正数
     * 控制back和next：
     * 每次读取完，用back保持最后一个已读取的字符；每次开始读取，用next获取第一个还没读取的数
     * @return
     * @throws IOException
     */
    private Token readNumber() throws IOException {
        char ch = charReader.curPos();
        StringBuilder sb = new StringBuilder();
        if (ch == '-') {// 处理负数
            sb.append(ch);
            ch = charReader.next();
            if (ch == '0') {//处理负数里的小数，形式为-0.xxxx
                handlerDecimal(ch, sb);
            } else if (isDigitFromOneToNine(ch)) {//处理负数里的正数
                handlerPositive(ch, sb);
            } else {
                throw new JsonParseException("Invalid minus number");
            }
        } else if (ch == '0') {//处理小数
            handlerDecimal(ch, sb);
        } else {//处理正数
            handlerPositive(ch, sb);
        }
        return new Token(TokenType.NUMBER, sb.toString());
    }

    private void handlerDecimal(char ch, StringBuilder sb) throws IOException {
        sb.append(ch);
        readFracAndExp(sb);
    }

    private void handlerPositive(char ch, StringBuilder sb) throws IOException {
        do {
            sb.append(ch);
            ch = charReader.next();
        } while (isDigit(ch));
        if (ch != (char) -1) {
            charReader.back();
            readFracAndExp(sb);
        }
    }

    /**
     * 判断是否是指数
     * @param ch
     * @return
     * @throws IOException
     */
    private boolean isExp(char ch) throws IOException {
        return ch == 'e' || ch == 'E';
    }
    /**
     * 判断范围[0,9]
     * @param ch
     * @return
     */
    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }
    /**
     * 判断范围[1,9]
     * @param ch
     * @return
     */
    private boolean isDigitFromOneToNine(char ch) {
        return ch >= '1' && ch <= '9';
    }
    /**
     * '.'和E,e
     * @return
     * @throws IOException
     */
    private void readFracAndExp(StringBuilder sb) throws IOException {
        char ch = charReader.next();
        if (ch ==  '.') {//处理小数点
            ch = appendStringByDigitAfterFrac(sb, ch);//添加所有的数字
            if (isExp(ch)) {//处理科学计数法,形式为0.xxxE(e)
                sb.append(ch);
                readExp(sb);
            } else {
                if (ch != (char) -1) {//舍弃
                    charReader.back();
                }
            }
        } else if (isExp(ch)) {
            sb.append(ch);
            readExp(sb);
        } else {
            charReader.back();
        }
    }

    private char appendStringByDigitAfterFrac(StringBuilder sb, char ch) throws IOException {
        //bug：0.xxx需要先得到第一个x，后面的才能循环添加
        sb.append(ch);
        ch = charReader.next();
        if (!isDigit(ch)) {
            throw new JsonParseException("Invalid frac");
        }
        do {
            sb.append(ch);
            ch = charReader.next();
        } while (isDigit(ch));
        return ch;
    }

    /**
     * 处理指数形式的数据
     * @return
     * @throws IOException
     */
    private void readExp(StringBuilder sb) throws IOException {
        char ch = charReader.next();
        if (ch == '+' || ch =='-') {
            appendStringByDigitAfterExp(sb, ch);
        } else {
            throw new JsonParseException("e or E,mush + or -");
        }
    }

    private void appendStringByDigitAfterExp(StringBuilder sb, char ch) throws IOException {
        sb.append(ch);
        ch = charReader.next();
        if (isDigit(ch)) {
            do {
                sb.append(ch);
                ch = charReader.next();
            } while (isDigit(ch));
            //读取结束，如果还没到末尾，则回退(不为数字)
            if (ch != (char) -1) {
                charReader.back();
            }
        } else {
            throw new JsonParseException("e or E，mush digit");
        }
    }
    /**
     * 判断是否是true or false
     * @return
     * @throws IOException
     */
    private Token readBoolean() throws IOException {
        if (charReader.curPos() == 't') {
            if (!(charReader.next() == 'r' && charReader.next() == 'u' && charReader.next() == 'e')) {
                throw new JsonParseException("Invalid json string");
            }
            return new Token(TokenType.BOOLEAN, "true");
        } else {
            if (!(charReader.next() == 'a' && charReader.next() == 'l'
                    && charReader.next() == 's' && charReader.next() == 'e')) {
                throw new JsonParseException("Invalid json string");
            }
            return new Token(TokenType.BOOLEAN, "false");
        }
    }
    /**
     * 词法分析器在读取字符n后，期望后面的三个字符分别是u,l,l，与 n 组成词 null。
     * 如果满足期望，则返回类型为 NULL 的 Token，否则报异常。
     */
    private Token readNull() throws IOException {
        if (!(charReader.next() == 'u' && charReader.next() == 'l' && charReader.next() == 'l')) {
            throw new JsonParseException("Invalid json string");
        }
        return new Token(TokenType.NULL, "null");
    }
}
