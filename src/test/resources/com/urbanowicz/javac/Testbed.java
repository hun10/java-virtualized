package com.urbanowicz.javac;

import java.math.BigInteger;

public class Testbed {

    @Virtualized
    public static void main(String... args) {
        BigInteger s1 = BigInteger.valueOf(98);
        BigInteger s2 = BigInteger.valueOf(95);
        BigInteger x = s1 + s2;
    }

    static int plus(int x, int y) {
        return x + y;
    }

    static BigInteger plus(BigInteger x, BigInteger y) {
        return x.add(y);
    }
}