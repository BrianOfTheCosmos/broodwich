/*
 * Copyright (c) 2019 Brian D. Hysell
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package party.itistimeto.broodwich.demos;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DemoServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            final String PARAM = "xml";
            if(req.getParameterMap().containsKey(PARAM)) {
                String xmlVal = req.getParameter(PARAM);
                //resp.getWriter().println(xmlVal);
                ByteArrayInputStream xmlBytes = new ByteArrayInputStream(xmlVal.getBytes(StandardCharsets.UTF_8));
                XMLDecoder xmlDec = new XMLDecoder(xmlBytes);
                Object result = xmlDec.readObject();
                xmlDec.close();
            }
            else if(req.getParameterMap().containsKey("calc")) {
                Runtime.getRuntime().exec("calc.exe");
            }
        } catch (Exception e) {
            e.printStackTrace(resp.getWriter());
        }
    }
}
