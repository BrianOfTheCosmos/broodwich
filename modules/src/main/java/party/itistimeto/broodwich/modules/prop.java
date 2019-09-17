package party.itistimeto.broodwich.modules;

import java.util.List;

public class prop {
    public static String run(List<String> params) {
        StringBuilder sb = new StringBuilder();

        for(String propName : System.getProperties().stringPropertyNames()) {
            sb.append(propName);
            sb.append(":");
            sb.append(System.getProperty(propName));
            sb.append("\n");
        }

        return sb.toString();
    }
}
