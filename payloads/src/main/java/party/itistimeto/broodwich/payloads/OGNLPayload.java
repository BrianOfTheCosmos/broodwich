/*
 * Copyright (c) 2019 Brian D. Hysell
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package party.itistimeto.broodwich.payloads;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class OGNLPayload extends JavascriptPayload {
    public OGNLPayload(String urlPattern, Class dropperClass, String password, Map<String, String> options) throws NoSuchAlgorithmException, IOException, URISyntaxException {
        super(urlPattern, dropperClass, password, options);
    }

    @Override
    public String toString() {
        try {
            return AbstractPayload.getResourceText("OGNLPayloadTemplate.ognl");
        } catch (IOException e) {
            return e.getMessage(); // don't @ me
        }
    }
}
