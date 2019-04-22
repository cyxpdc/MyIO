package com.pdc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * author PDC
 */
public class PropUtil {

    private static Properties prop = null;

    private static String getPropertiesPath(){
        ClassLoader classLoader = PropUtil.class.getClassLoader();
        InputStream fileIs = classLoader.getResourceAsStream("prop.properties");
        URL resource = classLoader.getResource("prop.properties");
        return resource.getPath();
    }

    public static void readProperties(){
        InputStream is = null;
        prop = new Properties();
        try {
            is = new FileInputStream(new File(getPropertiesPath()));
            prop.load(is);
            return ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("注意：Properties返回null了！");
    }
    
    public static String getDirector(){
        return prop.getProperty("Director");
    }

    public static String getOldName(){
        return prop.getProperty("OldName");
    }

    public static String getNewName(){
        return prop.getProperty("NewName");
    }

    public static Boolean getIsRecursionReadDirector(){
        return Boolean.parseBoolean(prop.getProperty("IsRecursionReadDirector"));
    }
}
