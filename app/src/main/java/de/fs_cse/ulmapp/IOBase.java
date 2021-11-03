package de.fs_cse.ulmapp;

public class IOBase {
    public static String input = "";
    public static String output = "";

    public static char inputPop(){
        char result = input.charAt(0);
        input = input.substring(1);
        return result;
    }

}
