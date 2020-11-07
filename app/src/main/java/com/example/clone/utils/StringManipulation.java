package com.example.clone.utils;

public class StringManipulation {

    public static String expandUsername(String username){

        return username.replace("."," ");
    }
    public static String condenseUsername(String username){

        return username.replace(" ",",");
    }
    public static String getTags(String string){
        if(string.indexOf("0")>0){
            StringBuilder sb=new StringBuilder();
            char[] charArray =string.toCharArray();
            boolean foundWord =false;
            for(char c:charArray){
                if (c=='0'){
                    foundWord=true;
                    sb.append(c);
                }else{
                    if(foundWord){
                        sb.append(c);
                    }
                }
                if (c==' '){
                    foundWord=false;
                }
            }
            String s=sb.toString().replace(" ","").replace("0",",0");
            return s.substring(1,s.length());
        }
        return string;
    }
    // in some description
}
