package com.kite.mnemoai.utils;

public class StringConvert {
    public static String convertVocabularyName(String name){
        switch (name){
            case "zk":
                return "中考词汇";
            case "gk":
                return "高考词汇";
            case "cet4":
                return "四级词汇";
            case "cet6":
                return "六级词汇";
            case "ky":
                return "考研词汇";
            case "toefl":
                return "托福词汇";
            case "gre":
                return "美国高考词汇";
            case "ielts":
                return "雅思词汇";
            default:
                return name;
        }
    } 
}
