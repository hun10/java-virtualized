package com.urbanowicz.javac;

import java.math.BigInteger;

public class Testbed {

    @Virtualized
    public static void main(String... args) {
        String x;

        BigInteger s1 = 98;
        BigInteger s2 = 95;
        x = (String) (s1 + s2);

        System.out.println(x);
    }

    static BigInteger plus(BigInteger x, BigInteger y) {
        return x.add(y);
    }

    static BigInteger cast(BigInteger x, long y) {
        return BigInteger.valueOf(y);
    }

    static String cast(String x, Number y) {
        return y.toString();
    }

    static String cast(String x, String y) {
        return y;
    }
}