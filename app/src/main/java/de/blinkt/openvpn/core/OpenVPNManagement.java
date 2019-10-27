

package de.blinkt.openvpn.core;

public interface OpenVPNManagement {
    interface PausedStateCallback {
        boolean shouldBeRunning();
    }

    enum pauseReason {
        noNetwork,
        userPause,
        screenOff,
    }

    int mBytecountInterval = 2;

    void reconnect();

    void pause(pauseReason reason);

    void resume();

    
    boolean stopVPN(boolean replaceConnection);

    
    void networkChange(boolean sameNetwork);

    void setPauseCallback(PausedStateCallback callback);
}
