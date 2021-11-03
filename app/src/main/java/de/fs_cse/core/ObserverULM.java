package de.fs_cse.core;

public interface ObserverULM {
    void nextInstruction(long address, int opfield, String disassembly);
    void onHalt(int exitCode, String errorMessage);
    void onBlock();
    void flagCallback(boolean zf, boolean of, boolean cf, boolean sf);
    void reset();
}
