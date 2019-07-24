package party.itistimeto.broodwich.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class cmd {
    public static String run(String... params) {
        try {
            Process proc = Runtime.getRuntime().exec(params);

            BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            StringBuilder sb = new StringBuilder();

            String line;
            while((line = stdout.readLine()) != null || (line = stderr.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

            proc.waitFor();

            return sb.toString();
        } catch (IOException e) {
            return e.getMessage();
        } catch (InterruptedException e) {
            return e.getMessage();
        }
    }
}
