package com.pdc;

import com.pdc.jsonparser.JSONParser;
import com.pdc.jsonparser.style.JsonObject;
import com.pdc.updatefilename.io.UpdateFileName;
import com.pdc.updatefilename.utils.PropUtil;

import java.io.File;
import java.io.IOException;

/**
 * author PDC
 */
public class Start {

    public static void main(String[] args) throws IOException{
        String json = "{\"姓名\":\"彭德崇\",\"年龄\":\"20\"}";
        JsonObject object = (JsonObject) Start.parseJson(json);
        System.out.println(object.get("姓名"));
    }

    public static void updateFileName(){
        PropUtil.readProperties();
        String director = PropUtil.getDirector();
        File file = new File(director);
        UpdateFileName.readFile(file);
        UpdateFileName.updateFile(UpdateFileName.fileList);
    }

    public static Object parseJson(String json) throws IOException {
        JSONParser parser = new JSONParser();
        Object object = parser.fromJSON(json);
        System.out.println(object);
        return object;
    }
}
