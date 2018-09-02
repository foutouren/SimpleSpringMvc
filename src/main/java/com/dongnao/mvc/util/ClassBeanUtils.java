package com.dongnao.mvc.util;

public class ClassBeanUtils {

    /**
     * 把字符串的首字母小写
     * @param name
     * @return
     */
    public static String toLowerFirstWord(String name){
        char[] charArray = name.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }
}
