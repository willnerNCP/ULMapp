package de.fs_cse.ulmapp;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

public class Tools {

    public static String getFormatedHex(long l, int numBytes){
        String result = String.format("%1$" + numBytes*2 + "s", Long.toHexString(l).toUpperCase())
                .replaceAll(" ", "0");
        return result.substring(result.length() - numBytes*2)
                .replaceAll(".{2}", "$0 ").trim();
    }

    public static String readURI(Uri uri, ContentResolver contentResolver){
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(contentResolver.openInputStream(uri)));
            int c;
            while((c = reader.read()) != -1) {
                content.append((char) c);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static byte[] toByteArray(long l, int numBytes){
        byte[] result = new byte[numBytes];
        for (int i = numBytes - 1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= Byte.SIZE;
        }
        return result;
    }

    public static int[] getProgramHelloWorld(){
        return new int[]{
                0x56002001,
                0x1B000102,
                0x39000200,
                0x42000004,
                0x61020000,
                0x38010101,
                0x41FFFFFB,
                0x09000000,
                0x68656C6C,
                0x6F2C2077,
                0x6F726C64,
                0x210A0000,
        };
    }

    public static int[] getProgramIO(){
        return new int[]{
                0x30000000,
                0x60010000,
                0x39000100,
                0x42000003,
                0x61010000,
                0x41FFFFFC,
                0x09000000};
    }

    public static byte[] parseProgramm(String s){
        ArrayList<Byte> program = new ArrayList<>();
        Scanner scanner = new Scanner(s);
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            if(line.charAt(0) == '0' && line.charAt(1) == 'x'){
                int colon_pos = line.indexOf(':');
                int hashtag_pos = line.indexOf('#');
                line = line.substring(colon_pos+1, hashtag_pos);
                Scanner lineScanner = new Scanner(line);
                while(lineScanner.hasNext())
                    program.add(Integer.decode("0x" + lineScanner.next()).byteValue());
            }
        }
        byte[] ret = new byte[program.size()];
        for(int i = 0; i < ret.length; i++){
            ret[i] = program.get(i);
        }
        return ret;
    }



}
