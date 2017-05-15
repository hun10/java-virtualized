package com.urbanowicz.javac;

import java.math.BigInteger;

public class Testbed {

    public static void main(String... args) {
        System.out.println(pow(3, new Rep("b")));
    }

    @Virtualized
    static <T> Rep<T> pow(int n, Rep<T> b) {
        if (n > 0) {
            return b * pow(n - 1, b);
        } else {
            return (Rep<T>) 1;
        }
    }

    static boolean gt(int x, int y) {
        return x > y;
    }

    static int minus(int x, int y) {
        return x - y;
    }

    static <T> Rep<T> mul(Rep<T> x, Rep<T> y) {
        return new Rep(x.rep + " * " + y.rep);
    }

    static BigInteger plus(BigInteger x, BigInteger y) {
        return x.add(y);
    }

    static BigInteger cast(BigInteger x, long y) {
        return BigInteger.valueOf(y);
    }

    static <T> Rep<T> cast(Rep<T> x, long y) {
        return new Rep(y + "");
    }

    static String cast(String x, Number y) {
        return y.toString();
    }

    static String cast(String x, String y) {
        return y;
    }

    static class Rep<T> {
        private final String rep;

        public Rep(String rep) {
            this.rep = rep;
        }

        @Override
        public String toString() {
            return "Rep{" +
                    "rep='" + rep + '\'' +
                    '}';
        }
    }
}