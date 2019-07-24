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
