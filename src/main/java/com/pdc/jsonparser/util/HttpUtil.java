package com.pdc.jsonparser.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * author PDC
 */
public class HttpUtil {
    public static byte[] get(String urlString) {
        HttpURLConnection urlConnection = null;
        InputStream in = null;
        ByteArrayOutputStream bos = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            //设置请求方法
            urlConnection.setRequestMethod("GET");
            //设置超时时间
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(3000);
            //获取响应的状态码
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) {
                bos = new ByteArrayOutputStream();
                in = urlConnection.getInputStream();
                byte[] buffer = new byte[4 * 1024];
                int len = -1;
                while((len = in.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
                byte[] result = bos.toByteArray();
                return result;
            } else {
                System.out.println("responseCode is not 200");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            close(in);
            close(bos);
        }
        return null;
    }

    private static void close(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
