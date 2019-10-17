package value;

import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceVisitor;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;
import checkers.inference.model.ArithmeticConstraint.ArithmeticOperationKind;
import checkers.inference.model.ArithmeticVariableSlot;
import checkers.inference.model.ConstraintManager;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.Tree.Kind;
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

            // Candidate Fix 1:
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
            switch (binaryTree.getKind()) {
                case PLUS:
                    if (TreeUtils.isStringConcatenation(binaryTree)) {
                        break;
                    }
                case MINUS:
                case MULTIPLY:
                case DIVIDE:
                case REMAINDER:
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
                        VariableSlot lubSlot =
                                slotManager.getVariableSlot(
                                        atypeFactory.getAnnotatedType(binaryTree));
                        // Create LUB constraint by default
                        constraintManager.addSubtypeConstraint(lhs, lubSlot);
                        constraintManager.addSubtypeConstraint(rhs, lubSlot);
                    }
                    break;
                case LEFT_SHIFT:
                case RIGHT_SHIFT:
                case UNSIGNED_RIGHT_SHIFT:
                case AND:
                case OR:
                case XOR:
                case EQUAL_TO: // ==
                case NOT_EQUAL_TO: // !=
                case GREATER_THAN: // >
                case GREATER_THAN_EQUAL: // >=
                case LESS_THAN: // <
                case LESS_THAN_EQUAL:
                    if (lhsAMVal == null || rhsAMVal == null) {
                        VariableSlot lubSlot =
                                slotManager.getVariableSlot(
                                        atypeFactory.getAnnotatedType(binaryTree));
                        // Create LUB constraint by default
                        constraintManager.addSubtypeConstraint(lhs, lubSlot);
                        constraintManager.addSubtypeConstraint(rhs, lubSlot);
                        break;
                    }
                    if (AnnotationUtils.areSameByClass(lhsAMVal, IntRange.class)
                            && AnnotationUtils.areSameByClass(rhsAMVal, IntRange.class)) {
                        break;
                    } // else create LUB constraint
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
