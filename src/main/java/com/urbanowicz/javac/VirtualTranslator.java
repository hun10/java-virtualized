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
        if (enabledMethodsStack.head && !tag.isAssignop()) {
            Name name = names.fromString(tag.name().toLowerCase());
            JCTree.JCIdent ident = make.Ident(name);
            ident.pos = jcBinary.pos;
            result = make.Apply(null, ident, List.of(jcBinary.lhs, jcBinary.rhs));
            result.pos = jcBinary.lhs.pos;
        }
    }

    @Override
    public void visitAssign(JCTree.JCAssign jcAssign) {
        super.visitAssign(jcAssign);

        if (enabledMethodsStack.head) {
            Name name = names.fromString("cast");
            JCTree.JCIdent ident = make.Ident(name);
            ident.pos = jcAssign.pos;
            JCTree.JCMethodInvocation apply = make.Apply(
                    null,
                    ident,
                    List.of(jcAssign.getVariable(), jcAssign.getExpression())
            );
            apply.pos = jcAssign.pos;

            result = make.Assign(jcAssign.getVariable(), apply);
            result.pos = jcAssign.pos;
        }
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
        super.visitVarDef(jcVariableDecl);

        if (enabledMethodsStack.head && jcVariableDecl.init != null) {
            JCTree.JCTypeCast jcTypeCast = make.TypeCast(jcVariableDecl.getType(), make.Literal(TypeTag.BOT, null));
            jcTypeCast.pos = jcVariableDecl.pos;

            Name name = names.fromString("cast");
            JCTree.JCIdent ident = make.Ident(name);
            ident.pos = jcVariableDecl.init.pos;
            JCTree.JCMethodInvocation apply = make.Apply(
                    null,
                    ident,
                    List.of(jcTypeCast, jcVariableDecl.getInitializer())
            );
            apply.pos = jcVariableDecl.init.pos;

            result = make.VarDef(
                    jcVariableDecl.getModifiers(),
                    jcVariableDecl.getName(),
                    jcVariableDecl.vartype,
                    apply
            );
            result.pos = jcVariableDecl.pos;
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