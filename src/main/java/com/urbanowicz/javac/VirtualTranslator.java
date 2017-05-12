package com.urbanowicz.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

class VirtualTranslator extends TreeTranslator {

    private static final String VIRTUALIZED_FQN = Virtualized.class.getName();
    private static final String VIRTUALIZED_PACKAGE = Virtualized.class.getPackage().getName();
    private static final String VIRTUALIZED = VIRTUALIZED_FQN.substring(VIRTUALIZED_PACKAGE.length() + 1);

    private final Names names;
    private final TreeMaker make;

    private boolean annotationImported;
    private List<Boolean> enabledMethodsStack = List.nil();

    VirtualTranslator(Context context) {
        names = Names.instance(context);
        make = TreeMaker.instance(context);
    }

    @Override
    public void visitBinary(JCTree.JCBinary jcBinary) {
        super.visitBinary(jcBinary);

        JCTree.Tag tag = jcBinary.getTag();
        if (enabledMethodsStack.head && !tag.isAssignop()) {
            Name name = names.fromString(tag.name().toLowerCase());
            result = make.Apply(null, make.Ident(name), List.of(jcBinary.lhs, jcBinary.rhs));
            result.pos = jcBinary.pos;
        }
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
        enabledMethodsStack = enabledMethodsStack.prepend(false);

        super.visitMethodDef(jcMethodDecl);

        enabledMethodsStack = enabledMethodsStack.tail;
    }

    @Override
    public void visitAnnotation(JCTree.JCAnnotation jcAnnotation) {
        super.visitAnnotation(jcAnnotation);

        String annotation = jcAnnotation.annotationType.toString();

        if (annotationImported && VIRTUALIZED.equals(annotation)
                || VIRTUALIZED_FQN.equals(annotation)) {
            enabledMethodsStack = enabledMethodsStack.tail.prepend(true);
        }
    }

    @Override
    public void visitTopLevel(JCTree.JCCompilationUnit jcCompilationUnit) {
        if (VIRTUALIZED_PACKAGE.equals(jcCompilationUnit.getPackageName().toString())) {
            annotationImported = true;
        }

        super.visitTopLevel(jcCompilationUnit);
    }
}