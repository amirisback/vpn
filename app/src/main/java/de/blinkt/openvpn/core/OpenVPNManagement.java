package de.blinkt.openvpn.core;

public interface OpenVPNManagement {
    int mBytecountInterval = 2;

    void reconnect();

    void pause(pauseReason reason);

    void resume();

    boolean stopVPN(boolean replaceConnection);

    void networkChange(boolean sameNetwork);

    void setPauseCallback(PausedStateCallback callback);

    enum pauseReason {
        noNetwork,
        userPause,
        screenOff,
    }

    interface PausedStateCallback {
        boolean shouldBeRunning();
    }
}
