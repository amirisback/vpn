

package de.blinkt.openvpn.core;

import java.security.InvalidKeyException;

public class NativeUtils {
    public static native byte[] rsasign(byte[] input, int pkey) throws InvalidKeyException;

    public static native String[] getIfconfig() throws IllegalArgumentException;

    static native void jniclose(int fdint);

    public static native String getNativeAPI();

    static {
        System.loadLibrary("opvpnutil");

    }
}
