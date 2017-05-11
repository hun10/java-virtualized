package com.urbanowicz.javac;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VirtualizedPluginTest {
    @Test
    public void test() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        int exitCode = compiler.run(
                null,
                null,
                null,
                "-printsource",
                "-d", "target/generated-test-sources",
                "-Xplugin:Virtualized",
                getClass().getResource("Testbed.java").getFile()
        );

        assertEquals(0, exitCode);
    }
}
