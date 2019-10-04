package value;

import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceVisitor;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;
import checkers.inference.model.ArithmeticConstraint.ArithmeticOperationKind;
import checkers.inference.model.ArithmeticVariableSlot;
import value.qual.UnknownVal;
import checkers.inference.model.ConstraintManager;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;

import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.Tree.Kind;

public class ValueVisitor extends InferenceVisitor<ValueChecker, BaseAnnotatedTypeFactory> {
	
	/** The top type for this hierarchy. */
    protected final AnnotationMirror UNKNOWNVAL =
            AnnotationBuilder.fromClass(elements, UnknownVal.class);
	
    public ValueVisitor(
            ValueChecker checker,
            InferenceChecker ichecker,
            BaseAnnotatedTypeFactory factory,
            boolean infer) {
        super(checker, ichecker, factory, infer);
    }
    
    @Override
    public Void visitBinary(BinaryTree binaryTree, Void p) {
    	// infer mode, adds constraints for binary operations
        if (infer) {
            SlotManager slotManager = InferenceMain.getInstance().getSlotManager();
            ConstraintManager constraintManager =
                    InferenceMain.getInstance().getConstraintManager();

            // Candidate Fix 1:
            InferenceAnnotatedTypeFactory iatf = (InferenceAnnotatedTypeFactory) atypeFactory;

            AnnotatedTypeMirror lhsATM = iatf.getAnnotatedType(binaryTree.getLeftOperand());
            AnnotatedTypeMirror rhsATM = iatf.getAnnotatedType(binaryTree.getRightOperand());
            AnnotationMirror lhsAM = lhsATM.getEffectiveAnnotationInHierarchy(iatf.getVarAnnot());
            AnnotationMirror rhsAM = rhsATM.getEffectiveAnnotationInHierarchy(iatf.getVarAnnot());
            Slot lhs = slotManager.getSlot(lhsAM);
            Slot rhs = slotManager.getSlot(rhsAM);

            Kind kind = binaryTree.getKind();
            switch (binaryTree.getKind()) {
                case PLUS:
                    ArithmeticOperationKind opKind = ArithmeticOperationKind.fromTreeKind(kind);
                    ArithmeticVariableSlot avsRes =
                            slotManager.getArithmeticVariableSlot(
                                    VariableAnnotator.treeToLocation(atypeFactory, binaryTree));
                    constraintManager.addArithmeticConstraint(opKind, lhs, rhs, avsRes);
                    break;
                    
                default:
                    VariableSlot lubSlot =
                            slotManager.getVariableSlot(atypeFactory.getAnnotatedType(binaryTree));
                    // Create LUB constraint by default
                    constraintManager.addSubtypeConstraint(lhs, lubSlot);
                    constraintManager.addSubtypeConstraint(rhs, lubSlot);
                    break;
            }
        }

        return super.visitBinary(binaryTree, p);
    }
}
