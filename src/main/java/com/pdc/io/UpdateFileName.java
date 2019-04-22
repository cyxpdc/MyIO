package com.pdc.io;

import com.pdc.algorithm.KMP;
import com.pdc.utils.PropUtil;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

/**
 * author PDC
 */
public class UpdateFileName {

    private static List<File> fileList = new ArrayList<>();

    public static void main(String[] args) {
        PropUtil.readProperties();
        String director = PropUtil.getDirector();
        File file = new File(director);
        readFile(file);
        updateFile(fileList);

    }
    /**
     * 读取某个文件夹下的所有文件
     */
    public static void readFile(File file){
        //如果不是文件夹，加入文件列表中
        if(!file.isDirectory()){
            fileList.add(file);
        }
        //如果是，遍历文件夹，若为文件，则加入，若为文件夹，则递归
        else if(file.isDirectory()){
            String[] files = file.list();
            for(int i = 0;i < files.length;i++){
                File perFile = new File(file.getAbsolutePath()+"\\"+files[i]);
                if(!perFile.isDirectory()){
                    fileList.add(perFile);
                }else if(perFile.isDirectory()){
                    if(PropUtil.getIsRecursionReadDirector()){
                        readFile(perFile);
                    }
                }
            }
        }
    }

    public static void updateFile(List<File> fileList){
        String oldName = PropUtil.getOldName();
        String newName = PropUtil.getNewName();
        for(File file : fileList){
            int index = KMP.getIndexOf(file.getName(),oldName);
            String fileName = file.getName();
            if(index != -1){
                String res = fileName.substring(0,index) + newName + fileName.substring(index+oldName.length());
                file.renameTo(new File(file.getParent()+"\\"+res));
                System.out.println(file.getName());
            }
        }
    }

}
