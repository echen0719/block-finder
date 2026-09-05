package echen0719.blockfinder.client;

public final class ClientHooks {
    private static Runnable showHUD;

    private ClientHooks() {}

    public static void setShowHUD(Runnable callback) {
        showHUD = callback;
    }

    public static void showHUD() {
        if (showHUD != null) {
            showHUD.run();
        }
    }
} // is populated by the client classes and then called here