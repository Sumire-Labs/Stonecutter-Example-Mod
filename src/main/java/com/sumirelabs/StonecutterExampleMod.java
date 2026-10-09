package com.sumirelabs;

/** Loader-independent code, compiled into every target with that target's toolchain. */
public final class StonecutterExampleMod {
    public static final String MOD_ID = "@{MOD_ID}";
    public static final String MOD_NAME = "@{MOD_NAME}";
    public static final String VERSION = "@{MOD_VERSION}";

    private StonecutterExampleMod() {}

    public static void initialize() {
        System.out.println("[" + MOD_NAME + "] " + VERSION);
    }
}

