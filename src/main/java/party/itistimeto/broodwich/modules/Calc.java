package party.itistimeto.broodwich.modules;

public class Calc {
    public static void runModule(byte[] params) {
        try {
            Runtime.getRuntime().exec("Calc.exe");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
