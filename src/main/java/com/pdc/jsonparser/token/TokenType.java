package com.pdc.jsonparser.token;

/**
 * JSON数据符号都保存在枚举类中
 * 为每一种类型定义一个数字(1、2、3、4也可以)
 * 目的：能通过位运算来判断是否是期望出现的类型
 * 注：可以与常量类相比较:https://yq.aliyun.com/articles/379553
 * author PDC
 */
public enum TokenType {
    BEGIN_OBJECT(1),// {
    END_OBJECT(2),//{
    BEGIN_ARRAY(4),// [
    END_ARRAY(8),// ]
    NULL(16),// null
    NUMBER(32),// 数字
    STRING(64),// 字符串
    BOOLEAN(128),// true or false
    SEPARATE_COLON(256),// ：
    SEPARATE_COMMA(512),// ，
    END_DOCUMENT(1024);//表示JSON数据结束了

    private int tokenCode;    // 每个类型的编号

    TokenType(int tokenCode) {
        this.tokenCode = tokenCode;
    }

    public int getTokenCode() {
        return tokenCode;
    }
}
