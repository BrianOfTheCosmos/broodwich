/*
 * Copyright (c) 2019 Brian D. Hysell
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package party.itistimeto.broodwich.modules;

import java.util.List;

public class prop {
    public static String run(List<String> params) {
        StringBuilder sb = new StringBuilder();

        for(String propName : System.getProperties().stringPropertyNames()) {
            sb.append(propName);
            sb.append(":");
            sb.append(System.getProperty(propName));
            sb.append("\n");
        }

        return sb.toString();
    }
}
