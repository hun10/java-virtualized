package com.urbanowicz.javac;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.Test;

public class VirtualizedPluginTest {
    @Test
    public void test() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(
                null,
                null,
                null,
                "-printsource",
                "-d", "target/generated-test-sources",
                "-Xplugin:Virtualized",
                getClass().getResource("Testbed.java").getFile()
        );
    }
}
