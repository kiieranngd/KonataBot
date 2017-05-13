package br.net.brjdevs.steven.konata.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

public class Utils {
    public static String getStackTrace(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    @SuppressWarnings("unchecked")
    public static  <T, K> Map.Entry<T, K> getEntryByIndex(Map<T, K> map, int index) {
        return (Map.Entry<T, K>) map.entrySet().toArray()[index];
    }
}
