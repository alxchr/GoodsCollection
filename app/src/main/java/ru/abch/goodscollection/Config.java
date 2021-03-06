package ru.abch.goodscollection;

import com.bosphere.filelogger.FL;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Config {
    public static String scheme;
    public static String host;
    public static int port;
    public static boolean debug = true;
    public static final boolean tts = true;
//    public static final String scheme = "https";
//    public static final String scheme = "http";
//    public static final String host = "tomcat.comtt.ru";
//    public static final String host = "192.168.21.244";
//    public static final int port = 8443;
//    public static final int port = 8080;
    public static final String goodsPath = "/goodscollection/goods/";
    public static final String barcodesPath = "/goodscollection/barcodes/";
    public static final String cellsPath = "/goodscollection/cells/";
    public static final String labelsPath = "/goodscollection/labeltest/ZD410-1";
    public static final String photoPath = "http://cdn.comtt.ru/";
    public static long timeShift;
//    public static final boolean tts = true;
    public static long toComttTime(long t) {
        return (t - timeShift)/1000;
    }
    public static long toJavaTime(long t) {
        return t*1000 + timeShift;
    }
    static final double pi = 3.14159265358979, rad = 6372795;
    public static final int offlineTimeout = 10 * 60 * 1000;    // 10 min
    public static final String dumpPath = "/goodscollection/dump/";
    /*
    public static final long weekInMillis = 7*24*3600*1000;
    public static final int maxDataCount = 100;
     */
    public static double delta(double lat0, double lon0, double lat1, double lon1) {
//        return 2 * rad * Math.asin((Math.sqrt(Math.pow(Math.sin((lat0-lat1)*pi/360),2)) + Math.cos(lat0*pi/180) * Math.cos(lat1*pi/180) * Math.pow(Math.sin((lon0-lon1)*pi/360),2)));
        return rad * Math.acos(Math.sin(lat0*pi/180) * Math.sin(lat1*pi/180) + (Math.cos(lat0*pi/180) * Math.cos(lat1*pi/180) * Math.cos((lon0-lon1)*pi/180)));
    }
    public Config() {
        if (debug) {
            scheme = "http";
            host = "192.168.21.244";
            port = 8080;
        } else {
            scheme = "https";
            host = "tomcat.comtt.ru";
            port = 8443;
        }
    }
    public static String getCellName(String code) {
        String result;
        int left, right, separator;
        if (code.startsWith("19000")) {
            result = code.substring(5, 12);
            while (result.length() > 2 && result.startsWith("0")) result = result.substring(1);
            if(result.length() > 2) {
                separator = result.indexOf("0");
                if(separator < result.length() - 1 && separator > 0) {
                    left = Integer.parseInt(result.substring(0,separator));
                    right = Integer.parseInt(result.substring(separator + 1));
                    result = String.format("%02d",left) + String.format("%03d",right);
                } else {
                    result = null;
                }
            } else {
                result = null;
            }
        } else {
            result = code.substring(4, 9);
        }
        return result;
    }
    public static String transliterate(String srcstring) {
        ArrayList<String> copyTo = new ArrayList<String>();

        String cyrcodes = "";
        for (int i=1040;i<=1067;i++) {
            cyrcodes = cyrcodes + (char)i;
        }
        for (int j=1072;j<=1099;j++) {
            cyrcodes = cyrcodes + (char)j;
        }
        // Uppercase
        copyTo.add("A");
        copyTo.add("B");
        copyTo.add("V");
        copyTo.add("G");
        copyTo.add("D");
        copyTo.add("E");
        copyTo.add("Zh");
        copyTo.add("Z");
        copyTo.add("I");
        copyTo.add("I");
        copyTo.add("K");
        copyTo.add("L");
        copyTo.add("M");
        copyTo.add("N");
        copyTo.add("O");
        copyTo.add("P");
        copyTo.add("R");
        copyTo.add("S");
        copyTo.add("T");
        copyTo.add("U");
        copyTo.add("F");
        copyTo.add("Kh");
        copyTo.add("TS");
        copyTo.add("Ch");
        copyTo.add("Sh");
        copyTo.add("Shch");
        copyTo.add("");
        copyTo.add("Y");

        // lowercase
        copyTo.add("a");
        copyTo.add("b");
        copyTo.add("v");
        copyTo.add("g");
        copyTo.add("d");
        copyTo.add("e");
        copyTo.add("zh");
        copyTo.add("z");
        copyTo.add("i");
        copyTo.add("i");
        copyTo.add("k");
        copyTo.add("l");
        copyTo.add("m");
        copyTo.add("n");
        copyTo.add("o");
        copyTo.add("p");
        copyTo.add("r");
        copyTo.add("s");
        copyTo.add("t");
        copyTo.add("u");
        copyTo.add("f");
        copyTo.add("kh");
        copyTo.add("ts");
        copyTo.add("ch");
        copyTo.add("sh");
        copyTo.add("shch");
        copyTo.add("");
        copyTo.add("y");

        String newstring = "";
        char onechar;
        int replacewith;
        for (int j=0; j<srcstring.length();j++) {
            onechar = srcstring.charAt(j);
            replacewith = cyrcodes.indexOf((int)onechar);
            if (replacewith > -1) {
                newstring = newstring + copyTo.get(replacewith);
            } else {
                // keep the original character, not in replace list
                newstring = newstring + String.valueOf(onechar);
            }
        }

        return newstring;
    }
    public static String comttTime(long cTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy HH:mm");
        return sdf.format(new java.util.Date(toJavaTime(cTime)));
    }
}
