package br.net.brjdevs.steven.konata.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {
    public static String getStackTrace(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
