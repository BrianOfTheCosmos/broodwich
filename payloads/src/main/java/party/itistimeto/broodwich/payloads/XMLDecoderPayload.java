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
import java.text.MessageFormat;

// todo: incorrect semantics since no "is-a" relationship?
public class XMLDecoderPayload extends JavascriptPayload {
    public XMLDecoderPayload(String urlPattern, Class dropperClass, String password) throws NoSuchAlgorithmException, IOException, URISyntaxException {
        super(urlPattern, dropperClass, password);
    }

    @Override
    public String toString() {
        try {
            return MessageFormat.format(AbstractPayload.getResourceText("XMLDecoderPayloadTemplate.xml"), super.toString());
        } catch (URISyntaxException | IOException e) {
            return e.getMessage(); // don't @ me
        }
    }
}
