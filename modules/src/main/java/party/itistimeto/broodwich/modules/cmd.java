/*
 * Copyright (c) 2019 Brian D. Hysell
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package party.itistimeto.broodwich.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class cmd {
    public static String run(List<String> params) {
        try {
            // todo: p1: figure out reason for hanging!!! (e.g., with cmd /k)
            Process proc = Runtime.getRuntime().exec(params.toArray(new String[0]));

            BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            StringBuilder sb = new StringBuilder();

            String line;
            while((line = stdout.readLine()) != null || (line = stderr.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

            proc.waitFor();

            return sb.toString();
        } catch (IOException e) {
            return e.getMessage();
        } catch (InterruptedException e) {
            return e.getMessage();
        }
    }
}
