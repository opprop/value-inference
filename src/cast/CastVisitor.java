package cast;

import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.value.ValueAnnotatedTypeFactory;
import org.checkerframework.common.value.ValueVisitor;
import org.checkerframework.common.value.qual.UnknownVal;
import org.checkerframework.common.value.util.Range;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationBuilder;

import com.sun.source.tree.AssignmentTree;
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
            AnnotationMirror valueAnno = valueType.getAnnotationInHierarchy(UNKNOWNVAL);
        	Range range = ValueAnnotatedTypeFactory.getRange(valueAnno);
        	if (isUnsignedByte(range)) {
        		checker.report(Result.warning(errorKey, valueType, varType), valueTree);
        	}
        }
        super.commonAssignmentCheck(varType, valueType, valueTree, errorKey);
    }
    
    /** Return true if this range contains unsigned part of {@code byte} value. */
    private boolean isUnsignedByte(Range range) {
        return !range.intersect(new Range(Byte.MAX_VALUE + 1, Byte.MAX_VALUE * 2 + 1)).isNothing()
        		&& !range.contains(Range.BYTE_EVERYTHING);
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
    
    @Override
    public Void visitAssignment(AssignmentTree node, Void p) {
    	System.out.println(node.getExpression());
    	Set<Node> expr_nodes = atypeFactory.getNodesForTree(node.getExpression());
    	for (Node n : expr_nodes) {
    		System.out.println(n);
    	}
    	return super.visitAssignment(node, p);
    }
}
