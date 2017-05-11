package com.urbanowicz.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

class VirtualTranslator extends TreeTranslator {

    private final Names names;
    private final TreeMaker make;

    VirtualTranslator(Context context) {
        names = Names.instance(context);
        make = TreeMaker.instance(context);
    }

    @Override
    public void visitBinary(JCTree.JCBinary jcBinary) {
        super.visitBinary(jcBinary);

        JCTree.Tag tag = jcBinary.getTag();
        if (!tag.isAssignop()) {
            Name name = names.fromString(tag.name().toLowerCase());
            result = make.Apply(null, make.Ident(name), List.of(jcBinary.lhs, jcBinary.rhs));
        }
    }
}