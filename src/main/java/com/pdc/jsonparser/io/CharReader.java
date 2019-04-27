package com.pdc.jsonparser.io;

import java.io.IOException;
import java.io.Reader;

/**
 * 读取JSON串的字符
 * 需要不断地读取下一位字符，将此方法(curPos、next)封装起来更方便
 * author PDC
 */
public class CharReader {
    private static final int BUFFER_SIZE = 1024;
    private Reader reader;
    private char[] buffer;
    private int index;//json串当前位置
    private int size;

    public CharReader(Reader reader) throws IOException {
        this.reader = reader;
        buffer = new char[BUFFER_SIZE];
        fillBuffer();
    }

    /**
     * 返回当前位置的字符
     * @return
     */
    public char curPos() {
        if (index - 1 >= size) {
            return (char) -1;
        }
        return buffer[Math.max(0, index - 1)];
    }

    /**
     * 返回当前位置的字符，并将当前位置往后移
     * 注：这里使用了
     * @return
     * @throws IOException
     */
    public char next() throws IOException {
        if (!hasMore()) {
            return (char) -1;
        }
        return buffer[index++];
    }

    /**
     * 位置回退
     */
    public void back() {
        index = Math.max(0, --index);
    }

    /**
     * 判断流是否结束
     */
    public boolean hasMore() throws IOException {
        if (index < size) {
            return true;
        }
        return false;
    }

    /**
     * 填充buffer数组
     * 将字符读入buffer数组，返回读取的字符数
     * 如果buffer为空，直接返回，不修改index和size
     * @throws IOException
     */
    public void fillBuffer() throws IOException {
        int n = reader.read(buffer);
        if (n == -1) {
            return;
        }
        index = 0;
        size = n;
    }
}
