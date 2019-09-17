package party.itistimeto.broodwich.modules;

import com.sun.management.HotSpotDiagnosticMXBean;
import party.itistimeto.broodwich.droppers.BroodwichFilter;

import javax.management.MBeanServer;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class heapdump {
    public static String run(List<String> params) {
        //https://blogs.oracle.com/sundararajan/programmatically-dumping-heap-from-java-applications
        if (params.size() > 0) {
            try {
                String heapDumpPath = params.get(0);

                MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
                String mxBeanName = "com.sun.management:type=HotSpotDiagnostic";
                HotSpotDiagnosticMXBean diagBean = ManagementFactory.newPlatformMXBeanProxy(platformMBeanServer, mxBeanName, HotSpotDiagnosticMXBean.class);
                // todo: check usage & free space before writing dump
                // by default we want to include dead objects (live = false) but we will accept true as an option
                if (params.size() > 1 && params.get(1).equals("true")) {
                    // todo: dumpHeap() in JDK11?
                    // todo: fix build process
                    diagBean.dumpHeap(heapDumpPath, true);
                } else {
                    diagBean.dumpHeap(heapDumpPath, false);
                }

                return "Heap dump written to: " + heapDumpPath;
            }
            catch (IOException e) {
                return e.getMessage();
            }
        }
        else {
            return "Please provide a path to which to save the heap dump.";
        }
    }
}
