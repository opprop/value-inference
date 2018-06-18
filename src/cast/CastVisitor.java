package cast;

import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.common.value.ValueVisitor;

import org.checkerframework.common.value.qual.IntRange;

public class CastVisitor extends ValueVisitor {

    public CastVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    public Void visitTypeCast(TypeCastTree node, Void p) {
        checkTypecastSafety(node, p);
        return super.visitTypeCast(node, p);
    }

    @Override
    protected void checkTypecastSafety(TypeCastTree node, Void p) {
        super.checkTypecastSafety(node, p);
    }
}
