/*
 * Copyright (c) 2019 Brian D. Hysell
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package party.itistimeto.broodwich.modules;

import java.io.File;
import java.util.List;

public class ls {
    public static String run(List<String> params) {
        if(params.size() > 0) {
            File[] contents = new File(params.get(0)).listFiles();

            if(contents != null) {
                StringBuilder sb = new StringBuilder();
                for (File f : contents) {
                    sb.append(f.getName());
                    sb.append("\n");
                }
                return sb.toString();
            }
            else {
                return "Directory not found.";
            }
        }
        else {
            return "Please provide a path for a directory to list.";
        }
    }
}
