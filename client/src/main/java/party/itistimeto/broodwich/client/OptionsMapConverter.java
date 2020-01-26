package party.itistimeto.broodwich.client;

import com.google.common.base.Splitter;
import picocli.CommandLine;

import java.util.Map;

public class OptionsMapConverter implements CommandLine.ITypeConverter<Map> {
    @Override
    public Map<String, String> convert(String value) throws Exception {
        // https://stackoverflow.com/a/14768279
        return Splitter.on(",").withKeyValueSeparator("=").split(value);
    }
}
