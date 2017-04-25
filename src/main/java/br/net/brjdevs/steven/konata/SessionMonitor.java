package br.net.brjdevs.steven.konata;

import br.net.brjdevs.steven.konata.core.TaskManager;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class SessionMonitor {

    private static final int availableProcessors = Runtime.getRuntime().availableProcessors();
    private static final double gb = 1024 * 1024 * 1024;
    private static double cpuUsage = 0;
    private static double freeMemory = 0;
    private static double lastProcessCpuTime = 0;
    private static long lastSystemTime = 0;
    private static double maxMemory = 0;
    private static int threadCount = 0;
    private static double totalMemory = 0;
    private static double vpsCPUUsage = 0;
    private static double vpsFreeMemory = 0;
    private static double vpsMaxMemory = 0;
    private static double vpsUsedMemory = 0;

    static {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        int mb = 0x100000;
        Runtime runtime = Runtime.getRuntime();
        TaskManager.startAsyncTask("Session Monitor", (service) -> {
            threadCount = Thread.activeCount();
            long systemTime = System.nanoTime();
            double processCpuTime = ((com.sun.management.OperatingSystemMXBean) os).getProcessCpuTime();

            cpuUsage = ((processCpuTime - lastProcessCpuTime) / ((double) (systemTime - lastSystemTime))) / availableProcessors;

            lastSystemTime = systemTime;
            lastProcessCpuTime = processCpuTime;

            vpsCPUUsage = ((com.sun.management.OperatingSystemMXBean) os).getSystemCpuLoad() * 100;

            vpsFreeMemory = ((com.sun.management.OperatingSystemMXBean) os).getFreePhysicalMemorySize() / gb;
            vpsMaxMemory = ((com.sun.management.OperatingSystemMXBean) os).getTotalPhysicalMemorySize() / gb;
            vpsUsedMemory = vpsMaxMemory - vpsFreeMemory;

            freeMemory = runtime.freeMemory() / mb;
            maxMemory = runtime.maxMemory() / mb;
            totalMemory = runtime.totalMemory() / mb;
        }, 5);
    }

    public static double getCPUUsage() {
        return cpuUsage;
    }

    public static double getMaxMemory() {
        return maxMemory;
    }

    public static double getVpsCPUUsage() {
        return vpsCPUUsage;
    }

    public static double getVpsFreeMemory() {
        return vpsFreeMemory;
    }

    public static double getVpsMaxMemory() {
        return vpsMaxMemory;
    }

    public static double getTotalMemory() {
        return totalMemory;
    }

    public static double getVpsUsedMemory() {
        return vpsUsedMemory;
    }

    public static int getThreadCount() {
        return threadCount;
    }

    public static double getFreeMemory() {
        return freeMemory;
    }



    public static String getUptime(){
        final long
                duration = ManagementFactory.getRuntimeMXBean().getUptime(),
                years = duration / 31104000000L,
                months = duration / 2592000000L % 12,
                days = duration / 86400000L % 30,
                hours = duration / 3600000L % 24,
                minutes = duration / 60000L % 60,
                seconds = duration / 1000L % 60;
        String uptime = (years == 0 ? "" : years + " years, ") + (months == 0 ? "" : months + " months, ")
                + (days == 0 ? "" : days + " days, ") + (hours == 0 ? "" : hours + " hours, ")
                + (minutes == 0 ? "" : minutes + " minutes, ") + (seconds == 0 ? "" : seconds + " seconds, ");

        uptime = StringUtils.replaceLast(uptime, ", ", "");
        return StringUtils.replaceLast(uptime, ",", " and");
    }
}
