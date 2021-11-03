package de.fs_cse.core;

public interface IODevice {
    void putc(byte c);
    byte getc();
    boolean hasNextChar();
    void reset();
}
