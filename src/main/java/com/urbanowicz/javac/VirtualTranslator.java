package com.urbanowicz.javac;

import com.sun.tools.javac.code.TypeTag;
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
        if (isVirtualized() && !tag.isAssignop()) {
            Name name = names.fromString(tag.name().toLowerCase());
            JCTree.JCIdent ident = make.Ident(name);
            ident.pos = jcBinary.pos;
            result = make.Apply(null, ident, List.of(jcBinary.lhs, jcBinary.rhs));
            result.pos = jcBinary.lhs.pos;
        }
    }

    @Override
    public void visitTypeCast(JCTree.JCTypeCast jcTypeCast) {
        super.visitTypeCast(jcTypeCast);

        if (isVirtualized()) {
            result = makeCast(jcTypeCast.getType(), jcTypeCast.getExpression());
        }
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
        super.visitVarDef(jcVariableDecl);

        if (isVirtualized() && jcVariableDecl.init != null) {
            result = make.VarDef(
                    jcVariableDecl.getModifiers(),
                    jcVariableDecl.getName(),
                    jcVariableDecl.vartype,
                    makeCast(jcVariableDecl.getType(), jcVariableDecl.getInitializer())
            );
            result.pos = jcVariableDecl.pos;
        }
    }

    private boolean isVirtualized() {
        return enabledMethodsStack.head != null && enabledMethodsStack.head;
    }

    private JCTree.JCMethodInvocation makeCast(JCTree type, JCTree.JCExpression expression) {
        JCTree.JCTypeCast jcTypeCast = make.TypeCast(type, make.Literal(TypeTag.BOT, null));
        jcTypeCast.pos = expression.pos;

        Name name = names.fromString("cast");
        JCTree.JCIdent ident = make.Ident(name);
        ident.pos = expression.pos;
        JCTree.JCMethodInvocation apply = make.Apply(
                null,
                ident,
                List.of(jcTypeCast, expression)
        );
        apply.pos = expression.pos;

        return apply;
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
    public void visitImport(JCTree.JCImport jcImport) {
        super.visitImport(jcImport);

        String qualifier = jcImport.getQualifiedIdentifier().toString();

        if (qualifier.endsWith("*")) {
            qualifier = qualifier.replace("*", VIRTUALIZED);
        }

        if (VIRTUALIZED_FQN.equals(qualifier)) {
            annotationImported = true;
        }
    }

    @Override
    public void visitTopLevel(JCTree.JCCompilationUnit jcCompilationUnit) {
        annotationImported = VIRTUALIZED_PACKAGE.equals(jcCompilationUnit.getPackageName().toString());

        super.visitTopLevel(jcCompilationUnit);

        annotationImported = false;
    }
}