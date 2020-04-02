/*
 * Copyright (c) 2019 Brian D. Hysell
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package party.itistimeto.broodwich.payloads;

import party.itistimeto.broodwich.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

// todo: gzip support

public abstract class AbstractPayload {
    final String urlPattern;
    final String password;
    final Map<String, String> options;
    final Class filterClass = party.itistimeto.broodwich.droppers.BroodwichFilter.class;
    final byte[] filterBytecode;
    final byte[] compressedFilterBytecode;
    final Class dropperClass;
    final byte[] dropperBytecode;
    final byte[] compressedDropperBytecode;

    AbstractPayload(String urlPattern, Class dropperClass, String password, Map<String, String> options) throws IOException {
        this.urlPattern = urlPattern;
        this.dropperClass = dropperClass;

        ClassLoader cl = this.getClass().getClassLoader();
        this.filterBytecode = Util.getResourceBytes(Util.classToResource(filterClass));
        this.dropperBytecode = Util.getResourceBytes(Util.classToResource(dropperClass));

        var bos = new ByteArrayOutputStream();
        var gis = new GZIPOutputStream(bos);
        gis.write(this.filterBytecode);
        gis.finish();
        this.compressedFilterBytecode = bos.toByteArray();
        bos.reset();
        gis = new GZIPOutputStream(bos);
        gis.write(this.dropperBytecode);
        gis.finish();
        this.compressedDropperBytecode = bos.toByteArray();

        this.password = password;
        this.options = options;
    }

    @Override
    public abstract String toString();

    public abstract byte[] toBytes();

}
