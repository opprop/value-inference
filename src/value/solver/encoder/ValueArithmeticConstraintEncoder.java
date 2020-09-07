package value.solver.encoder;

import checkers.inference.model.ArithmeticConstraint.ArithmeticOperationKind;
import checkers.inference.model.ArithmeticVariableSlot;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.backend.encoder.ArithmeticConstraintEncoder;
import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.frontend.Lattice;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntNum;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.javacutil.BugInCF;
import value.representation.TypeCheckValue;
import value.solver.representation.Z3InferenceValue;

public class ValueArithmeticConstraintEncoder extends ValueAbstractConstraintEncoder
        implements ArithmeticConstraintEncoder<BoolExpr> {

    public ValueArithmeticConstraintEncoder(
            Lattice lattice,
            Context ctx,
            Z3SmtFormatTranslator<Z3InferenceValue, TypeCheckValue> formatTranslator) {
        super(lattice, ctx, formatTranslator);
    }

    protected BoolExpr encode(
            ArithmeticOperationKind operation,
            Slot leftOperand,
            Slot rightOperand,
            ArithmeticVariableSlot result) {
        Z3InferenceValue left = leftOperand.serialize(z3SmtFormatTranslator);
        Z3InferenceValue right = rightOperand.serialize(z3SmtFormatTranslator);
        Z3InferenceValue res = result.serialize(z3SmtFormatTranslator);

        BoolExpr encoding =
                ctx.mkAnd(
//                        ctx.mkNot(res.getBoolVal()),
//                        ctx.mkImplies(
//                                ctx.mkAnd(left.getIntRange(), right.getStringVal()),
//                                res.getStringVal()),
//                        ctx.mkImplies(
//                                ctx.mkAnd(left.getStringVal(), right.getIntRange()),
//                                res.getStringVal()),
//                        ctx.mkImplies(
//                                ctx.mkAnd(left.getStringVal(), right.getStringVal()),
//                                res.getStringVal()),
                        ctx.mkImplies(
                                ctx.mkAnd(left.getBottomVal(), right.getBottomVal()),
                                res.getBottomVal()),
                        ctx.mkImplies(left.getUnknownVal(), res.getUnknownVal()),
                        ctx.mkImplies(right.getUnknownVal(), res.getUnknownVal())
//                        ctx.mkImplies(
//                                ctx.mkAnd(left.getIntRange(), right.getIntRange()),
//                                ctx.mkNot(res.getBottomVal()))
                        );

        // Unless variable type is long, all arithmetic operations in Java are widened to int first, 
        IntNum maxRange = ctx.mkInt(Integer.MAX_VALUE);
        IntNum minRange = ctx.mkInt(Integer.MIN_VALUE);
        if (leftOperand instanceof VariableSlot) {
        	VariableSlot vslot = (VariableSlot) leftOperand;
            TypeMirror type = vslot.getUnderlyingType();
            if (type.getKind() == TypeKind.LONG) {
            	maxRange = ctx.mkInt(Long.MAX_VALUE);
            	minRange = ctx.mkInt(Long.MIN_VALUE);
            }
        } else if (rightOperand instanceof VariableSlot) {
        	VariableSlot vslot = (VariableSlot) rightOperand;
            TypeMirror type = vslot.getUnderlyingType();
            if (type.getKind() == TypeKind.LONG) {
            	maxRange = ctx.mkInt(Long.MAX_VALUE);
            	minRange = ctx.mkInt(Long.MIN_VALUE);
            }
        }
        	
        ArithExpr upperArith;
        ArithExpr lowerArith;
        
        switch (operation) {
            case PLUS:
                upperArith = ctx.mkAdd(left.getIntRangeUpper(), right.getIntRangeUpper());
                lowerArith = ctx.mkAdd(left.getIntRangeLower(), right.getIntRangeLower());
                break;
            case MINUS:
                upperArith = ctx.mkSub(left.getIntRangeUpper(), right.getIntRangeUpper());
                lowerArith = ctx.mkSub(left.getIntRangeLower(), right.getIntRangeLower());
                break;
            case MULTIPLY:
            	ArithExpr mul1 = ctx.mkMul(left.getIntRangeUpper(), right.getIntRangeUpper());
            	ArithExpr mul2 = ctx.mkMul(left.getIntRangeLower(), right.getIntRangeLower());
            	ArithExpr mul3 = ctx.mkMul(left.getIntRangeUpper(), right.getIntRangeLower());
            	ArithExpr mul4 = ctx.mkMul(left.getIntRangeLower(), right.getIntRangeUpper());
            	
            	return ctx.mkAnd(
                        encoding,
                        ctx.mkOr(
                                ctx.mkAnd(
                                        left.getIntRange(),
                                        right.getIntRange(),
                                        res.getIntRange(),
                                        ctx.mkOr(
                                                ctx.mkAnd(
                                                        ctx.mkImplies(ctx.mkAnd(ctx.mkGe(mul1, mul2), ctx.mkGe(mul1, mul3), ctx.mkGe(mul1, mul4)), ctx.mkEq(res.getIntRangeUpper(), mul1)),
                                                        ctx.mkImplies(ctx.mkAnd(ctx.mkGe(mul2, mul1), ctx.mkGe(mul2, mul3), ctx.mkGe(mul2, mul4)), ctx.mkEq(res.getIntRangeUpper(), mul2)),
                                                        ctx.mkImplies(ctx.mkAnd(ctx.mkGe(mul3, mul1), ctx.mkGe(mul3, mul2), ctx.mkGe(mul3, mul4)), ctx.mkEq(res.getIntRangeUpper(), mul3)),
                                                        ctx.mkImplies(ctx.mkAnd(ctx.mkGe(mul4, mul1), ctx.mkGe(mul4, mul2), ctx.mkGe(mul4, mul3)), ctx.mkEq(res.getIntRangeUpper(), mul4)),
			                                            ctx.mkImplies(ctx.mkAnd(ctx.mkLe(mul1, mul2), ctx.mkLe(mul1, mul3), ctx.mkLe(mul1, mul4)), ctx.mkEq(res.getIntRangeLower(), mul1)),
			                                            ctx.mkImplies(ctx.mkAnd(ctx.mkLe(mul2, mul1), ctx.mkLe(mul2, mul3), ctx.mkLe(mul2, mul4)), ctx.mkEq(res.getIntRangeLower(), mul2)),
			                                            ctx.mkImplies(ctx.mkAnd(ctx.mkLe(mul3, mul1), ctx.mkLe(mul3, mul2), ctx.mkLe(mul3, mul4)), ctx.mkEq(res.getIntRangeLower(), mul3)),
			                                            ctx.mkImplies(ctx.mkAnd(ctx.mkLe(mul4, mul1), ctx.mkLe(mul4, mul2), ctx.mkLe(mul4, mul3)), ctx.mkEq(res.getIntRangeLower(), mul4)),
			                                            ctx.mkLt(mul1, maxRange),
                                                        ctx.mkLt(mul2, maxRange),
                                                        ctx.mkLt(mul3, maxRange),
                                                        ctx.mkLt(mul4, maxRange),
                                                        ctx.mkGt(mul1, minRange),
                                                        ctx.mkGt(mul2, minRange),
                                                        ctx.mkGt(mul3, minRange),
                                                        ctx.mkGt(mul4, minRange)),
                                                ctx.mkAnd(
                                                        ctx.mkEq(res.getIntRangeLower(), minRange),
                                                        ctx.mkEq(res.getIntRangeUpper(), maxRange),
                                                        ctx.mkOr(
                                                                ctx.mkGt(mul1, maxRange),
                                                                ctx.mkGt(mul2, maxRange),
                                                                ctx.mkGt(mul3, maxRange),
                                                                ctx.mkGt(mul4, maxRange),
                                                                ctx.mkLt(mul1, minRange),
                                                                ctx.mkLt(mul2, minRange),
                                                                ctx.mkLt(mul3, minRange),
                                                                ctx.mkLt(mul4, minRange))))),
                                ctx.mkNot(res.getIntRange())));
            case DIVIDE:
                upperArith = ctx.mkDiv(left.getIntRangeUpper(), right.getIntRangeUpper());
                lowerArith = ctx.mkDiv(left.getIntRangeLower(), right.getIntRangeLower());
                break;
            case REMAINDER:
            	return ctx.mkAnd(
                        encoding,
                        ctx.mkOr(
                                ctx.mkAnd(
                                        left.getIntRange(),
                                        right.getIntRange(),
                                        res.getIntRange(),
                                        ctx.mkOr(
                                                ctx.mkAnd(
                                                        ctx.mkImplies(ctx.mkGe(right.getIntRangeLower(), ctx.mkInt(0)), 
                                                        		ctx.mkAnd(ctx.mkEq(res.getIntRangeLower(), ctx.mkInt(0)), ctx.mkEq(res.getIntRangeUpper(), right.getIntRangeUpper()))),
                                                        ctx.mkImplies(ctx.mkLe(right.getIntRangeUpper(), ctx.mkInt(0)), 
                                                        		ctx.mkAnd(ctx.mkEq(res.getIntRangeUpper(), ctx.mkInt(0)), ctx.mkEq(res.getIntRangeLower(), right.getIntRangeLower()))),
                                                        ctx.mkImplies(ctx.mkAnd(ctx.mkLe(right.getIntRangeLower(), ctx.mkInt(0)), ctx.mkGe(right.getIntRangeUpper(), ctx.mkInt(0))), 
                                                        		ctx.mkAnd(ctx.mkEq(res.getIntRangeLower(), right.getIntRangeLower()), ctx.mkEq(res.getIntRangeUpper(), right.getIntRangeUpper())))),
                                ctx.mkNot(res.getIntRange())))));
            case LEFT_SHIFT:
                // The value of n << s is n left-shifted s bit positions. Equivalent to
                // multiplication by 2 to the power s.
            	
                mul1 = ctx.mkMul(left.getIntRangeUpper(), ctx.mkPower(ctx.mkInt(2), right.getIntRangeUpper()));
            	mul2 = ctx.mkMul(left.getIntRangeLower(), ctx.mkPower(ctx.mkInt(2), right.getIntRangeLower()));
            	mul3 = ctx.mkMul(left.getIntRangeUpper(), ctx.mkPower(ctx.mkInt(2), right.getIntRangeLower()));
            	mul4 = ctx.mkMul(left.getIntRangeLower(), ctx.mkPower(ctx.mkInt(2), right.getIntRangeUpper()));
            	
            	return ctx.mkAnd(
                        encoding,
                        ctx.mkOr(
                        		ctx.mkAnd(
                                        left.getIntRange(),
                                        right.getIntRange(),
                                        res.getIntRange(),
                                		ctx.mkOr(ctx.mkLe(right.getIntRangeLower(), ctx.mkInt(0)), ctx.mkGe(right.getIntRangeUpper(), ctx.mkInt(31))),
	                                    ctx.mkEq(res.getIntRangeLower(), minRange),
	                                    ctx.mkEq(res.getIntRangeUpper(), maxRange)),
                                ctx.mkAnd(
                                        left.getIntRange(),
                                        right.getIntRange(),
                                        res.getIntRange(),
                                		ctx.mkGe(right.getIntRangeLower(), ctx.mkInt(0)), 
                                		ctx.mkLe(right.getIntRangeUpper(), ctx.mkInt(31)),
                                        ctx.mkOr(
                                                ctx.mkAnd(
                                                        ctx.mkImplies(ctx.mkAnd(ctx.mkGe(mul1, mul2), ctx.mkGe(mul1, mul3), ctx.mkGe(mul1, mul4)), ctx.mkEq(res.getIntRangeUpper(), mul1)),
                                                        ctx.mkImplies(ctx.mkAnd(ctx.mkGe(mul2, mul1), ctx.mkGe(mul2, mul3), ctx.mkGe(mul2, mul4)), ctx.mkEq(res.getIntRangeUpper(), mul2)),
                                                        ctx.mkImplies(ctx.mkAnd(ctx.mkGe(mul3, mul1), ctx.mkGe(mul3, mul2), ctx.mkGe(mul3, mul4)), ctx.mkEq(res.getIntRangeUpper(), mul3)),
                                                        ctx.mkImplies(ctx.mkAnd(ctx.mkGe(mul4, mul1), ctx.mkGe(mul4, mul2), ctx.mkGe(mul4, mul3)), ctx.mkEq(res.getIntRangeUpper(), mul4)),
			                                            ctx.mkImplies(ctx.mkAnd(ctx.mkLe(mul1, mul2), ctx.mkLe(mul1, mul3), ctx.mkLe(mul1, mul4)), ctx.mkEq(res.getIntRangeLower(), mul1)),
			                                            ctx.mkImplies(ctx.mkAnd(ctx.mkLe(mul2, mul1), ctx.mkLe(mul2, mul3), ctx.mkLe(mul2, mul4)), ctx.mkEq(res.getIntRangeLower(), mul2)),
			                                            ctx.mkImplies(ctx.mkAnd(ctx.mkLe(mul3, mul1), ctx.mkLe(mul3, mul2), ctx.mkLe(mul3, mul4)), ctx.mkEq(res.getIntRangeLower(), mul3)),
			                                            ctx.mkImplies(ctx.mkAnd(ctx.mkLe(mul4, mul1), ctx.mkLe(mul4, mul2), ctx.mkLe(mul4, mul3)), ctx.mkEq(res.getIntRangeLower(), mul4)),
			                                            ctx.mkLt(mul1, maxRange),
                                                        ctx.mkLt(mul2, maxRange),
                                                        ctx.mkLt(mul3, maxRange),
                                                        ctx.mkLt(mul4, maxRange),
                                                        ctx.mkGt(mul1, minRange),
                                                        ctx.mkGt(mul2, minRange),
                                                        ctx.mkGt(mul3, minRange),
                                                        ctx.mkGt(mul4, minRange)),
                                                ctx.mkAnd(
                                                        ctx.mkEq(res.getIntRangeLower(), minRange),
                                                        ctx.mkEq(res.getIntRangeUpper(), maxRange),
                                                        ctx.mkOr(
                                                                ctx.mkGt(mul1, maxRange),
                                                                ctx.mkGt(mul2, maxRange),
                                                                ctx.mkGt(mul3, maxRange),
                                                                ctx.mkGt(mul4, maxRange),
                                                                ctx.mkLt(mul1, minRange),
                                                                ctx.mkLt(mul2, minRange),
                                                                ctx.mkLt(mul3, minRange),
                                                                ctx.mkLt(mul4, minRange))))),
                                ctx.mkNot(res.getIntRange())));
            case RIGHT_SHIFT:
                // The value of n >> s is n right-shifted s bit positions with sign-extension.
                // Resulting value is ⌊ n / 2s ⌋.
                upperArith =
                        ctx.mkDiv(
                                left.getIntRangeUpper(),
                                ctx.mkPower(ctx.mkInt(2), right.getIntRangeUpper()));
                lowerArith =
                        ctx.mkDiv(
                                left.getIntRangeLower(),
                                ctx.mkPower(ctx.mkInt(2), right.getIntRangeLower()));
                break;
            case UNSIGNED_RIGHT_SHIFT:
                /* The value of n >>> s is n right-shifted s bit positions with zero-extension:
                If n >= 0, then the result is n >> s.
                If n < 0, then the result is (n >> s) + (2L << ~s). where ~s == (-s)-1. */
                ArithExpr posUpperArith =
                        ctx.mkDiv(
                                left.getIntRangeUpper(),
                                ctx.mkPower(ctx.mkInt(2), right.getIntRangeUpper()));
                ArithExpr posLowerArith =
                        ctx.mkDiv(
                                left.getIntRangeLower(),
                                ctx.mkPower(ctx.mkInt(2), right.getIntRangeLower()));
                ArithExpr negUpperArith =
                        ctx.mkAdd(
                                ctx.mkDiv(
                                        left.getIntRangeUpper(),
                                        ctx.mkPower(ctx.mkInt(2), right.getIntRangeUpper())),
                                ctx.mkMul(
                                        ctx.mkInt(2),
                                        ctx.mkPower(
                                                ctx.mkInt(2),
                                                ctx.mkSub(
                                                        ctx.mkMul(
                                                                ctx.mkInt(-1),
                                                                right.getIntRangeUpper()),
                                                        ctx.mkInt(1)))));
                ArithExpr negLowerArith =
                        ctx.mkAdd(
                                ctx.mkDiv(
                                        left.getIntRangeLower(),
                                        ctx.mkPower(ctx.mkInt(2), right.getIntRangeLower())),
                                ctx.mkMul(
                                        ctx.mkInt(2),
                                        ctx.mkPower(
                                                ctx.mkInt(2),
                                                ctx.mkSub(
                                                        ctx.mkMul(
                                                                ctx.mkInt(-1),
                                                                right.getIntRangeLower()),
                                                        ctx.mkInt(1)))));
                return ctx.mkAnd(
                        encoding,
                        ctx.mkOr(
                                ctx.mkAnd(
                                        left.getIntRange(),
                                        right.getIntRange(),
                                        res.getIntRange(),
                                        ctx.mkOr(
                                                // 0 <= left.lower <= left.upper <= max
                                                ctx.mkAnd(
                                                        ctx.mkGe(
                                                                left.getIntRangeLower(),
                                                                ctx.mkInt(0)),
                                                        ctx.mkEq(
                                                                res.getIntRangeUpper(),
                                                                posUpperArith),
                                                        ctx.mkEq(
                                                                res.getIntRangeLower(),
                                                                posLowerArith)),
                                                // min <= left.lower <= left.upper < 0
                                                ctx.mkAnd(
                                                        ctx.mkLt(
                                                                left.getIntRangeUpper(),
                                                                ctx.mkInt(0)),
                                                        ctx.mkEq(
                                                                res.getIntRangeUpper(),
                                                                negUpperArith),
                                                        ctx.mkEq(
                                                                res.getIntRangeLower(),
                                                                negLowerArith),
                                                        ctx.mkGe(negLowerArith, minRange),
                                                        ctx.mkLe(negUpperArith, maxRange)),
                                                ctx.mkAnd(
                                                        ctx.mkLt(
                                                                left.getIntRangeUpper(),
                                                                ctx.mkInt(0)),
                                                        ctx.mkEq(res.getIntRangeLower(), minRange),
                                                        ctx.mkEq(res.getIntRangeUpper(), maxRange),
                                                        ctx.mkOr(
                                                                ctx.mkGt(negUpperArith, maxRange),
                                                                ctx.mkLt(negLowerArith, minRange))),
                                                //  min <= left.lower < 0 <= left.upper <= max
                                                ctx.mkAnd(
                                                        ctx.mkGe(
                                                                left.getIntRangeUpper(),
                                                                ctx.mkInt(0)),
                                                        ctx.mkLt(
                                                                left.getIntRangeLower(),
                                                                ctx.mkInt(0)),
                                                        ctx.mkEq(
                                                                res.getIntRangeUpper(),
                                                                posLowerArith),
                                                        ctx.mkEq(
                                                                res.getIntRangeLower(),
                                                                negLowerArith),
                                                        ctx.mkGe(negLowerArith, minRange)),
                                                ctx.mkAnd(
                                                        ctx.mkGe(
                                                                left.getIntRangeUpper(),
                                                                ctx.mkInt(0)),
                                                        ctx.mkLt(
                                                                left.getIntRangeLower(),
                                                                ctx.mkInt(0)),
                                                        ctx.mkEq(res.getIntRangeLower(), minRange),
                                                        ctx.mkEq(res.getIntRangeUpper(), maxRange),
                                                        ctx.mkLt(negLowerArith, minRange)))),
                                ctx.mkNot(res.getIntRange())));
            case XOR:
            	return ctx.mkAnd(
                        encoding,
                        ctx.mkOr(
                                ctx.mkAnd(
                                        left.getIntRange(),
                                        right.getIntRange(),
                                        res.getIntRange(),
                                        ctx.mkEq(res.getIntRangeLower(), minRange),
                                        ctx.mkEq(res.getIntRangeUpper(), maxRange)),
                                ctx.mkNot(res.getIntRange())));
            case AND:
            	return ctx.mkAnd(
                        encoding,
                        ctx.mkOr(
                                ctx.mkAnd(
                                        left.getIntRange(),
                                        right.getIntRange(),
                                        res.getIntRange(),
                                        ctx.mkOr(ctx.mkAnd(
                                                		ctx.mkEq(left.getIntRangeLower(), left.getIntRangeUpper()),
                                                		ctx.mkGe(left.getIntRangeLower(), ctx.mkInt(0))),
                                        		ctx.mkAnd(
                                                        ctx.mkEq(right.getIntRangeLower(), right.getIntRangeUpper()),
                                                        ctx.mkGe(right.getIntRangeLower(), ctx.mkInt(0)))),
                                        ctx.mkImplies(
                                        		ctx.mkAnd(
                                                        ctx.mkEq(left.getIntRangeLower(), left.getIntRangeUpper()),
                                                        ctx.mkGe(left.getIntRangeLower(), ctx.mkInt(0))),
                                                ctx.mkAnd(
                                                        ctx.mkEq(res.getIntRangeUpper(), left.getIntRangeLower()),
                                                        ctx.mkEq(res.getIntRangeLower(), ctx.mkInt(0)))),
                                        ctx.mkImplies(
                                        		ctx.mkAnd(
                                                        ctx.mkEq(right.getIntRangeLower(), right.getIntRangeUpper()),
                                                        ctx.mkGe(right.getIntRangeLower(), ctx.mkInt(0))),
                                                ctx.mkAnd(
                                                        ctx.mkEq(res.getIntRangeUpper(), right.getIntRangeLower()),
                                                        ctx.mkEq(res.getIntRangeLower(), ctx.mkInt(0))))),
                                ctx.mkAnd(
                                        left.getIntRange(),
                                        right.getIntRange(),
                                        res.getIntRange(),
                                        ctx.mkNot(ctx.mkAnd(
                                        		ctx.mkEq(left.getIntRangeLower(), left.getIntRangeUpper()),
                                        		ctx.mkGe(left.getIntRangeLower(), ctx.mkInt(0)))),
                                        ctx.mkNot(ctx.mkAnd(
                                                ctx.mkEq(right.getIntRangeLower(), right.getIntRangeUpper()),
                                                ctx.mkGe(right.getIntRangeLower(), ctx.mkInt(0)))),
                                        ctx.mkEq(res.getIntRangeLower(), minRange),
                                        ctx.mkEq(res.getIntRangeUpper(), maxRange)),
                                ctx.mkNot(res.getIntRange())));
            case OR:
            	return ctx.mkAnd(
                        encoding,
                        ctx.mkOr(
                                ctx.mkAnd(
                                        left.getIntRange(),
                                        right.getIntRange(),
                                        res.getIntRange(),
                                        ctx.mkEq(res.getIntRangeLower(), minRange),
                                        ctx.mkEq(res.getIntRangeUpper(), maxRange)),
                                ctx.mkNot(res.getIntRange())));
            default:
                throw new BugInCF(
                        "Attempting to encode an unsupported arithmetic operation: "
                                + operation
                                + " leftOperand: "
                                + leftOperand
                                + " rightOperand: "
                                + rightOperand
                                + " result: "
                                + result);
        }

        return ctx.mkAnd(
                encoding,
                ctx.mkOr(
                        ctx.mkAnd(
                                left.getIntRange(),
                                right.getIntRange(),
                                res.getIntRange(),
                                ctx.mkOr(
                                        ctx.mkAnd(
                                                ctx.mkEq(res.getIntRangeUpper(), upperArith),
                                                ctx.mkEq(res.getIntRangeLower(), lowerArith),
                                                ctx.mkLe(upperArith, maxRange),
                                                ctx.mkGe(lowerArith, minRange)),
                                        ctx.mkAnd(
                                                ctx.mkEq(res.getIntRangeLower(), minRange),
                                                ctx.mkEq(res.getIntRangeUpper(), maxRange),
                                                ctx.mkOr(
                                                        ctx.mkGt(upperArith, maxRange),
                                                        ctx.mkLt(lowerArith, minRange))))),
                        ctx.mkNot(res.getIntRange())));
    }

    @Override
    public BoolExpr encodeVariable_Variable(
            ArithmeticOperationKind operation,
            Slot leftOperand,
            Slot rightOperand,
            ArithmeticVariableSlot result) {
        return encode(operation, leftOperand, rightOperand, result);
    }

    @Override
    public BoolExpr encodeVariable_Constant(
            ArithmeticOperationKind operation,
            Slot leftOperand,
            ConstantSlot rightOperand,
            ArithmeticVariableSlot result) {
        return encode(operation, leftOperand, rightOperand, result);
    }

    @Override
    public BoolExpr encodeConstant_Variable(
            ArithmeticOperationKind operation,
            ConstantSlot leftOperand,
            Slot rightOperand,
            ArithmeticVariableSlot result) {
        return encode(operation, leftOperand, rightOperand, result);
    }

    @Override
    public BoolExpr encodeConstant_Constant(
            ArithmeticOperationKind operation,
            ConstantSlot leftOperand,
            ConstantSlot rightOperand,
            ArithmeticVariableSlot result) {
        return encode(operation, leftOperand, rightOperand, result);
    }
}
