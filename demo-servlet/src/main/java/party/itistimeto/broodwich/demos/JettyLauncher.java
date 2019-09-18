/*
 * Copyright (c) 2019 Brian D. Hysell
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package party.itistimeto.broodwich.demos;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyLauncher {
    public static void main(String... args) {
        try {
            // from https://stackoverflow.com/a/20789340

            Server server = new Server(8080);

            String rootPath = JettyLauncher.class.getClassLoader().getResource(".").toString();
            WebAppContext webapp = new WebAppContext(rootPath + "../../../src/main/webapp", "");
            server.setHandler(webapp);

            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
