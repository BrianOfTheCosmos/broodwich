package party.itistimeto.broodwich.modules;

import com.sun.management.HotSpotDiagnosticMXBean;
import party.itistimeto.broodwich.droppers.BroodwichFilter;

import javax.management.MBeanServer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.zip.GZIPOutputStream;

public class heapdump {
    public static String run(String... params) {
        //https://blogs.oracle.com/sundararajan/programmatically-dumping-heap-from-java-applications
        try {
            // todo: randomize prefix
            File tempFile = File.createTempFile("broodwich", ".hprof");
            String heapDumpPath = tempFile.getAbsolutePath();
            if (tempFile.delete()) {
                // todo: handle error case
                MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
                String mxBeanName = "com.sun.management:type=HotSpotDiagnostic";
                HotSpotDiagnosticMXBean diagBean = ManagementFactory.newPlatformMXBeanProxy(platformMBeanServer, mxBeanName, HotSpotDiagnosticMXBean.class);
                // todo: check usage & free space before writing dump
                // by default we want to include dead objects (live = false) but we will accept true as an option
                if (params.length > 0 && params[0].equals("true")) {
                    // todo: dumpHeap() in JDK11?
                    // todo: fix build process
                    diagBean.dumpHeap(heapDumpPath, true);
                } else {
                    diagBean.dumpHeap(heapDumpPath, false);
                }

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                GZIPOutputStream gos = new GZIPOutputStream(bos);
                FileInputStream fis = new FileInputStream(heapDumpPath);
                byte[] buf = new byte[4096];
                int n;
                while ((n = fis.read(buf)) > 0) {
                    gos.write(buf, 0, n);
                }

                fis.close();
                new File(heapDumpPath).delete();

                return BroodwichFilter.encodeBase64(bos.toByteArray());
            }
            else {
                return "Could not delete initial temp file";
            }
        } catch (IOException e) {
            return e.getMessage();
        }
    }
}
