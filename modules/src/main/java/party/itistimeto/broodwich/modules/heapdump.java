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
        try {
            // todo: randomize name
            String heapDumpPath;
            String heapDumpRelPath = File.separator + "broodwich" + Math.random() + ".hprof";
            // perform test write, then delete
            // todo: figure out alternative to this / why File.canWrite doesn't work
            try {
                heapDumpPath = System.getProperty("java.io.tmpdir") + heapDumpRelPath;
                FileOutputStream fos = new FileOutputStream(heapDumpPath);
                fos.write(41);
                fos.close();
                new File(heapDumpPath).delete();
            } catch (IOException e) {
                // todo: this pattern is really bad?
                try {
                    heapDumpPath = System.getProperty("user.home") + heapDumpRelPath;
                    FileOutputStream fos = new FileOutputStream(heapDumpPath);
                    fos.write(41);
                    fos.close();
                    new File(heapDumpPath).delete();
                } catch (IOException ex) {
                    return e.getMessage() + ":" + ex.getMessage();
                }
            }

            // todo: handle error case
            MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            String mxBeanName = "com.sun.management:type=HotSpotDiagnostic";
            HotSpotDiagnosticMXBean diagBean = ManagementFactory.newPlatformMXBeanProxy(platformMBeanServer, mxBeanName, HotSpotDiagnosticMXBean.class);
            // todo: check usage & free space before writing dump
            // by default we want to include dead objects (live = false) but we will accept true as an option
            if (params.size() > 0 && params.get(0).equals("true")) {
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
        } catch (IOException e) {
            return e.getMessage();
        }
    }
}
