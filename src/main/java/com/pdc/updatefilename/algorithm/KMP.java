package com.pdc.updatefilename.algorithm;

/**
 * author PDC
 */
public class KMP {
    public static int getIndexOf(String s,String m){
        if(null == s || null == m || m.length() < 1 || s.length() < m.length()){
            return -1;
        }
        char[] str1 = s.toCharArray();
        char[] str2 = m.toCharArray();
        int[] next = getNextArray(str2);
        int i1 = 0,i2 = 0;//两个指针
        while(i1 < str1.length && i2 < str2.length){
            if(str1[i1] == str2[i2]){
                i1++;
                i2++;
            }
            //两个else都是不等的情况
            else if(next[i2] == -1){//为-1，说明i2是第一个位置,即i1到下一位后，跟str2的第一个位置比较
                i1++;
            }
            else{
                i2 = next[i2];
            }
        }
        return i2 == str2.length ? i1 - i2 : -1;
    }

    public static int[] getNextArray(char[] str2) {
        if(str2.length == 1){
            return new int[]{-1};
        }
        int[] next = new int[str2.length];
        next[0] = -1;
        next[1] = 0;
        int i = 2;
        int cn = 0;//跳转的位置
        while(i < next.length){
            if(str2[i-1] == str2[cn]){
                next[i++] = ++cn;
            }
            //两个else，代表：可以就往前跳，不行就等于0
            else if(cn > 0){
                cn = next[cn];
            }
            else{//cn<=0
                next[i++] = 0;
            }
        }
        return next;
    }
}
