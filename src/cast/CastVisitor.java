package cast;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.value.ValueVisitor;
import org.checkerframework.common.value.qual.UnknownVal;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationBuilder;

import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;

public class CastVisitor extends ValueVisitor {
	
	/** The top type for this hierarchy. */
    protected final AnnotationMirror UNKNOWNVAL;
    
    public CastVisitor(BaseTypeChecker checker) {
        super(checker);
        UNKNOWNVAL = AnnotationBuilder.fromClass(elements, UnknownVal.class);
    }
    
    @Override
    protected CastAnnotatedTypeFactory createTypeFactory() {
        return new CastAnnotatedTypeFactory(checker);
    }

    @Override
    protected void commonAssignmentCheck(
            AnnotatedTypeMirror varType,
            AnnotatedTypeMirror valueType,
            Tree valueTree,
            String errorKey) {
    	
    	if (varType.getUnderlyingType().getKind() != valueType.getUnderlyingType().getKind()
    			&& valueType.getUnderlyingType().getKind() == TypeKind.BYTE) {
            checker.report(Result.warning(errorKey, valueType, varType), valueTree);
        }
        super.commonAssignmentCheck(varType, valueType, valueTree, errorKey);
    }
    
    @Override
    public Void visitTypeCast(TypeCastTree node, Void p) {
    	// type cast safety for byte casting is already being checked
    	AnnotatedTypeMirror castType = atypeFactory.getAnnotatedType(node);
    	if (castType.getUnderlyingType().getKind() != TypeKind.BYTE) {
    		checkTypecastSafety(node, p);
    	}
        
    	return super.visitTypeCast(node, p);
    }
}
