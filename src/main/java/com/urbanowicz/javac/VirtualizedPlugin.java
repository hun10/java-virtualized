package com.urbanowicz.javac;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.tree.JCTree;

public class VirtualizedPlugin implements Plugin {
    @Override
    public String getName() {
        return "Virtualized";
    }

    @Override
    public void init(JavacTask task, String... args) {
        VirtualTranslator virtualTranslator = new VirtualTranslator(((BasicJavacTask) task).getContext());

        task.addTaskListener(new TaskListener() {
            @Override
            public void started(TaskEvent taskEvent) {
            }

            @Override
            public void finished(TaskEvent taskEvent) {
                switch (taskEvent.getKind()) {
                    case PARSE:
                        ((JCTree.JCCompilationUnit) taskEvent.getCompilationUnit()).accept(virtualTranslator);
                        break;
                }
            }
        });
    }

    public static <T> T virtual(T expression) {
        throw new IllegalStateException();
    }
}