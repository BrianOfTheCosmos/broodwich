package party.itistimeto.broodwich.deserialization;

public interface ScriptEngineEvaluator {
    public byte[] generateScriptPayload(String scriptText, String scriptType);
    public byte[] generateJavaScriptPayload(String scriptText);
}
