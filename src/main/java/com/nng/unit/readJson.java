package com.nng.unit;

import java.util.LinkedList;
import java.util.List;

/**
 * 把Json格式转化成List<String>
 */
public final class readJson {

    /**
     * 把["","",...]一个个取出来,记得最后转化成自己想要的格式
     * @param c
     * @return
     */
    public static List<String> change_format(String c) {
        List<String> result = new LinkedList<>();
        String temp = "";
        if (c != null) {
            for (int i = 0; i < c.length(); i++) {
                if (c.charAt(i) == '[' || c.charAt(i) == '\"' || c.charAt(i) == '\'')//字符是[ 直接退出这一轮循环
                {
                    continue;
                }
                if (c.charAt(i) == ',' || c.charAt(i) == ']')//字符是 ] 或者 , 直接进行判断
                {
                    if(temp.equals("null")&&c.charAt(i-1)!='\"')
                    {
                        temp=null;
                    }
                    result.add(temp);
                    temp = "";
                    continue;
                }
                temp = temp + c.charAt(i);
            }
        }
            return result;


    }


}
