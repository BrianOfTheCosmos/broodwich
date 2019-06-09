package party.itistimeto.broodwich;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.XMLDecoder;
import java.io.*;
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
