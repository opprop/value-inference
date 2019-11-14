package value;

import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceVisitor;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;
import checkers.inference.model.ArithmeticConstraint.ArithmeticOperationKind;
import checkers.inference.model.ArithmeticVariableSlot;
import checkers.inference.model.ComparableConstraint.ComparableOperationKind;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.ConstraintManager;
import checkers.inference.model.Slot;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;

import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;
import value.qual.IntRange;
import value.qual.UnknownVal;

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

            InferenceAnnotatedTypeFactory iatf = (InferenceAnnotatedTypeFactory) atypeFactory;

            AnnotatedTypeMirror lhsATM = iatf.getAnnotatedType(binaryTree.getLeftOperand());
            AnnotatedTypeMirror rhsATM = iatf.getAnnotatedType(binaryTree.getRightOperand());
            AnnotationMirror lhsAM = lhsATM.getEffectiveAnnotationInHierarchy(iatf.getVarAnnot());
            AnnotationMirror rhsAM = rhsATM.getEffectiveAnnotationInHierarchy(iatf.getVarAnnot());
            AnnotationMirror lhsAMVal = lhsATM.getEffectiveAnnotationInHierarchy(UNKNOWNVAL);
            AnnotationMirror rhsAMVal = rhsATM.getEffectiveAnnotationInHierarchy(UNKNOWNVAL);
            Slot lhs = slotManager.getSlot(lhsAM);
            Slot rhs = slotManager.getSlot(rhsAM);

            Kind kind = binaryTree.getKind();
            switch (kind) {
                case PLUS:
                    if (TreeUtils.isStringConcatenation(binaryTree)) {
                        break;
                    }
                case MINUS:
                case MULTIPLY:
                case DIVIDE:
                case REMAINDER:
                case LEFT_SHIFT:
                case RIGHT_SHIFT:
                case UNSIGNED_RIGHT_SHIFT:
                case AND:
                case OR:
                case XOR:
                    if (lhsAMVal == null || rhsAMVal == null) {
                        ArithmeticOperationKind opKindplus =
                                ArithmeticOperationKind.fromTreeKind(kind);
                        ArithmeticVariableSlot avsResplus =
                                slotManager.getArithmeticVariableSlot(
                                        VariableAnnotator.treeToLocation(atypeFactory, binaryTree));
                        constraintManager.addArithmeticConstraint(opKindplus, lhs, rhs, avsResplus);
                        break;
                    }
                    if (AnnotationUtils.areSameByClass(lhsAMVal, IntRange.class)
                            && AnnotationUtils.areSameByClass(rhsAMVal, IntRange.class)) {
                    } else {
                        Slot lubSlot =
                                slotManager.getVariableSlot(
                                        atypeFactory.getAnnotatedType(binaryTree));
                        // Create LUB constraint by default
                        constraintManager.addSubtypeConstraint(lhs, lubSlot);
                        constraintManager.addSubtypeConstraint(rhs, lubSlot);
                    }
                    break;
                case EQUAL_TO: // ==
                case NOT_EQUAL_TO: // !=
                case GREATER_THAN: // >
                case GREATER_THAN_EQUAL: // >=
                case LESS_THAN: // <
                case LESS_THAN_EQUAL:
                    if (lhsAMVal == null || rhsAMVal == null) {
                    	ComparableOperationKind opKindplus =
                    			ComparableOperationKind.fromTreeKind(kind);
                        constraintManager.addComparableConstraint(opKindplus, lhs, rhs);
                        break;
                    }
                    if (AnnotationUtils.areSameByClass(lhsAMVal, IntRange.class)
                            && AnnotationUtils.areSameByClass(rhsAMVal, IntRange.class)) {
                    	break;
                    } // Create LUB constraint by default
                default:
                    Slot lubSlot =
                            slotManager.getVariableSlot(atypeFactory.getAnnotatedType(binaryTree));
                    // Create LUB constraint by default
                    constraintManager.addSubtypeConstraint(lhs, lubSlot);
                    constraintManager.addSubtypeConstraint(rhs, lubSlot);
                    break;
            }
        }

        return super.visitBinary(binaryTree, p);
    }
    
    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree tree, Void p) {
        // infer mode, adds constraints for binary operations
        if (infer) {
            SlotManager slotManager = InferenceMain.getInstance().getSlotManager();
            ConstraintManager constraintManager =
                    InferenceMain.getInstance().getConstraintManager();

            InferenceAnnotatedTypeFactory iatf = (InferenceAnnotatedTypeFactory) atypeFactory;

            AnnotatedTypeMirror exprATM = iatf.getAnnotatedType(tree.getExpression());
            AnnotatedTypeMirror varATM = iatf.getAnnotatedType(tree.getVariable());
            AnnotationMirror exprAM = exprATM.getEffectiveAnnotationInHierarchy(iatf.getVarAnnot());
            AnnotationMirror varAM = varATM.getEffectiveAnnotationInHierarchy(iatf.getVarAnnot());
            AnnotationMirror exprAMVal = exprATM.getEffectiveAnnotationInHierarchy(UNKNOWNVAL);
            AnnotationMirror varAMVal = varATM.getEffectiveAnnotationInHierarchy(UNKNOWNVAL);
            Slot lhs = slotManager.getSlot(exprAM);
            Slot rhs = slotManager.getSlot(varAM);

            Kind kind = tree.getKind();
            switch (kind) {
                case PLUS_ASSIGNMENT:
                    if (TreeUtils.isStringConcatenation(tree)) {
                        break;
                    }
                case MINUS_ASSIGNMENT:
                case MULTIPLY_ASSIGNMENT:
                case DIVIDE_ASSIGNMENT:
                case REMAINDER_ASSIGNMENT:
                    if (exprAMVal == null || varAMVal == null) {
                        ArithmeticOperationKind opKindplus =
                                ArithmeticOperationKind.fromTreeKind(kind);
                        ArithmeticVariableSlot avsResplus =
                                slotManager.getArithmeticVariableSlot(
                                        VariableAnnotator.treeToLocation(atypeFactory, tree));
                        constraintManager.addArithmeticConstraint(opKindplus, lhs, rhs, avsResplus);
                        break;
                    }
                    if (AnnotationUtils.areSameByClass(exprAMVal, IntRange.class)
                            && AnnotationUtils.areSameByClass(varAMVal, IntRange.class)) {
                    	break;
                    } // Create LUB constraint by default
                default:
                    Slot lubSlot =
                            slotManager.getVariableSlot(atypeFactory.getAnnotatedType(tree));
                    // Create LUB constraint by default
                    constraintManager.addSubtypeConstraint(lhs, lubSlot);
                    constraintManager.addSubtypeConstraint(rhs, lubSlot);
                    break;
            }
        }

        return super.visitCompoundAssignment(tree, p);
    }
    
    @Override
	public Void visitUnary(UnaryTree tree, Void p) {
    	// infer mode, adds constraints for unary operations
        if (infer) {
            SlotManager slotManager = InferenceMain.getInstance().getSlotManager();
            ConstraintManager constraintManager =
                    InferenceMain.getInstance().getConstraintManager();

            ValueInferenceAnnotatedTypeFactory iatf = (ValueInferenceAnnotatedTypeFactory) atypeFactory;

            AnnotatedTypeMirror exprATM = iatf.getAnnotatedType(tree.getExpression());
            AnnotationMirror exprAM = exprATM.getEffectiveAnnotationInHierarchy(iatf.getVarAnnot());
            AnnotationMirror exprAMVal = exprATM.getEffectiveAnnotationInHierarchy(UNKNOWNVAL);
            Slot expr = slotManager.getSlot(exprAM);

            Kind kind = tree.getKind();
            switch (kind) {
                case UNARY_MINUS:
                    if (exprAMVal == null) {
                        ArithmeticVariableSlot avsResplus =
                                slotManager.getArithmeticVariableSlot(
                                        VariableAnnotator.treeToLocation(atypeFactory, tree));
                        ConstantSlot cs = slotManager.createConstantSlot(iatf.createIntRangeAnnotation(-1,-1));
                        constraintManager.addArithmeticConstraint(ArithmeticOperationKind.MULTIPLY, cs, expr, avsResplus);
                        break;
                    }
                    if (AnnotationUtils.areSameByClass(exprAMVal, IntRange.class)) {
                    } // Create LUB constraint by default
                default:
                    Slot lubSlot =
                            slotManager.getVariableSlot(atypeFactory.getAnnotatedType(tree));
                    // Create LUB constraint by default
                    constraintManager.addSubtypeConstraint(expr, lubSlot);
                    break;
            }
        }
		return super.visitUnary(tree, p);
    }
    
    @Override
    public Void visitTypeCast(TypeCastTree tree, Void p) {
    	// infer mode, adds constraints for cast operations
        if (infer) {
            SlotManager slotManager = InferenceMain.getInstance().getSlotManager();
            ConstraintManager constraintManager =
                    InferenceMain.getInstance().getConstraintManager();

            InferenceAnnotatedTypeFactory iatf = (InferenceAnnotatedTypeFactory) atypeFactory;

            AnnotatedTypeMirror castATM = iatf.getAnnotatedType(tree);
            AnnotatedTypeMirror exprATM = iatf.getAnnotatedType(tree.getExpression());
            AnnotationMirror castAM = castATM.getEffectiveAnnotationInHierarchy(iatf.getVarAnnot());
            AnnotationMirror exprAM = exprATM.getEffectiveAnnotationInHierarchy(iatf.getVarAnnot());
            Slot cast = slotManager.getSlot(castAM);
            Slot expr = slotManager.getSlot(exprAM);
            
            constraintManager.addSubtypeConstraint(expr, cast);
        }
        
        return super.visitTypeCast(tree, p);
    }
}
