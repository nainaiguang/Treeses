package com.nng.unit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * 统计指定目录下java文件的行数
 */
public class CountFileRow implements Util{
    private static int total = 0;
    private static int fileNum = 0;
    /*
     * 统计单个文件的行数
     */
    public static void singleFile(File file){
        FileReader reader = null;
        BufferedReader buffer = null;
        try {
            reader = new FileReader(file);
            buffer = new BufferedReader(reader);

            String line = null;
            while((line=buffer.readLine())!= null){
                //去除空格
                String trimStr = line.trim();
                //如果以/,*开头，就跳过该次循环
                if(trimStr.startsWith("/") || trimStr.startsWith("*") || trimStr.length()<=0){
                    continue;
                }else{
                    total++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
     * 遍历指定目录下所有的文件
     */
    public static void fileTraversal(File directory){
        File[] file = directory.listFiles();
        for(int i=0; i<file.length; i++){
            if(file[i].isDirectory()){  //此对象如果是目录，就递归
                fileTraversal(file[i]);
            }else{
                if(file[i].getName().endsWith(".java")||file[i].getName().endsWith(".xml")||file[i].getName().endsWith(".xsd")){//判断是否以.java结尾
                    System.out.println("第"+(++fileNum)+"个文件:\t"+file[i]);
                    singleFile(file[i]);//统计单个文件的行数
                }
            }
        }
    }
    //
    public static void main(String[] args) {
        File dir = new File("E:\\LearningProject\\idea_project\\maven_workplace");
        fileTraversal(dir);
        System.out.println("共"+total+"行代码");
    }

}
