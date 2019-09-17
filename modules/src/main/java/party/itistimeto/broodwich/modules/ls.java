package party.itistimeto.broodwich.modules;

import java.io.File;
import java.util.List;

public class ls {
    public static String run(List<String> params) {
        if(params.size() > 0) {
            File[] contents = new File(params.get(0)).listFiles();

            if(contents != null) {
                StringBuilder sb = new StringBuilder();
                for (File f : contents) {
                    sb.append(f.getName());
                    sb.append("\n");
                }
                return sb.toString();
            }
            else {
                return "Directory not found.";
            }
        }
        else {
            return "Please provide a path for a directory to list.";
        }
    }
}
