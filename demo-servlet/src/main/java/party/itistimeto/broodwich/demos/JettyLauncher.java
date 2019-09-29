/*
 * Copyright (c) 2019 Brian D. Hysell
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package party.itistimeto.broodwich.demos;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyLauncher {
    public static void main(String... args) {
        try {
            // from https://stackoverflow.com/a/20789340

            Server server = new Server(8080);

            WebAppContext webapp = new WebAppContext(Thread.currentThread().getContextClassLoader().getResource("webapp").toURI().toString(), "");
            // following jsp setup from http://www.eclipse.org/jetty/documentation/current/embedded-examples.html#embedded-webapp-jsp
            Configuration.ClassList classlist = Configuration.ClassList
                    .setServerDefault(server);
            classlist.addBefore(
                    "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                    "org.eclipse.jetty.annotations.AnnotationConfiguration");
            webapp.setAttribute(
                    "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                    ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$");

            server.setHandler(webapp);
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
