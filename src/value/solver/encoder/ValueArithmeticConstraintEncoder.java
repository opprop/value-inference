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
                        ctx.mkNot(res.getBoolVal()),
                        ctx.mkNot(res.getBottomVal()),
                        ctx.mkImplies(
                                ctx.mkAnd(left.getIntRange(), right.getStringVal()),
                                res.getStringVal()),
                        ctx.mkImplies(
                                ctx.mkAnd(left.getStringVal(), right.getIntRange()),
                                res.getStringVal()),
                        ctx.mkImplies(
                                ctx.mkAnd(left.getStringVal(), right.getStringVal()),
                                res.getStringVal()),
                        ctx.mkImplies(
                                ctx.mkAnd(left.getUnknownVal(), right.getUnknownVal()),
                                res.getUnknownVal()),
                        ctx.mkImplies(
                                ctx.mkAnd(left.getUnknownVal(), right.getIntRange()),
                                res.getUnknownVal()),
                        ctx.mkImplies(
                                ctx.mkAnd(left.getIntRange(), right.getUnknownVal()),
                                res.getUnknownVal()),
                        ctx.mkEq(
                                ctx.mkAnd(left.getIntRange(), right.getIntRange()),
                                res.getIntRange()));

        IntNum maxRange = ctx.mkInt(Long.MAX_VALUE);
        IntNum minRange = ctx.mkInt(Long.MIN_VALUE);
        ArithExpr upperArith;
        ArithExpr lowerArith;

        if (leftOperand instanceof VariableSlot) {
            VariableSlot vslot = (VariableSlot) leftOperand;
            TypeMirror type = vslot.getUnderlyingType();
            if (type.toString().equals("byte") || type.toString().equals("java.lang.Byte")) {
                maxRange = ctx.mkInt(Integer.MAX_VALUE);
                minRange = ctx.mkInt(Integer.MIN_VALUE);
            }
            if (type.toString().equals("short") || type.toString().equals("java.lang.Short")) {
                maxRange = ctx.mkInt(Integer.MAX_VALUE);
                minRange = ctx.mkInt(Integer.MIN_VALUE);
            }
            if (type.toString().equals("char") || type.toString().equals("java.lang.Character")) {
                maxRange = ctx.mkInt(Character.MAX_VALUE);
                minRange = ctx.mkInt(Character.MIN_VALUE);
            }
            if (type.toString().equals("int") || type.toString().equals("java.lang.Integer")) {
                maxRange = ctx.mkInt(Integer.MAX_VALUE);
                minRange = ctx.mkInt(Integer.MIN_VALUE);
            }
        }

        switch (operation) {
            case PLUS:
                upperArith = ctx.mkAdd(left.getIntRangeUpper(), right.getIntRangeUpper());
                lowerArith = ctx.mkAdd(left.getIntRangeUpper(), right.getIntRangeUpper());
                break;
            case MINUS:
                upperArith = ctx.mkSub(left.getIntRangeUpper(), right.getIntRangeUpper());
                lowerArith = ctx.mkSub(left.getIntRangeUpper(), right.getIntRangeUpper());
                break;
            case MULTIPLY:
                upperArith = ctx.mkMul(left.getIntRangeUpper(), right.getIntRangeUpper());
                lowerArith = ctx.mkMul(left.getIntRangeUpper(), right.getIntRangeUpper());
                break;
            case DIVIDE:
                upperArith = ctx.mkDiv(left.getIntRangeUpper(), right.getIntRangeUpper());
                lowerArith = ctx.mkDiv(left.getIntRangeUpper(), right.getIntRangeUpper());
                break;
            case REMAINDER:
                upperArith = ctx.mkRem(left.getIntRangeUpper(), right.getIntRangeUpper());
                lowerArith = ctx.mkRem(left.getIntRangeUpper(), right.getIntRangeUpper());
                break;
            case LEFT_SHIFT:
                // The value of n << s is n left-shifted s bit positions. Equivalent to
                // multiplication by 2 to the power s.
                upperArith =
                        ctx.mkMul(
                                left.getIntRangeUpper(),
                                ctx.mkPower(ctx.mkInt(2), right.getIntRangeUpper()));
                lowerArith =
                        ctx.mkMul(
                                left.getIntRangeLower(),
                                ctx.mkPower(ctx.mkInt(2), right.getIntRangeLower()));
                break;
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
                upperArith =
                        ctx.mkBV2Int(
                                ctx.mkBVXOR(
                                        ctx.mkInt2BV(64, left.getIntRangeUpper()),
                                        ctx.mkInt2BV(64, right.getIntRangeUpper())),
                                true);
                lowerArith =
                        ctx.mkBV2Int(
                                ctx.mkBVXOR(
                                        ctx.mkInt2BV(64, left.getIntRangeLower()),
                                        ctx.mkInt2BV(64, right.getIntRangeLower())),
                                true);
                break;
            case AND:
                upperArith =
                        ctx.mkBV2Int(
                                ctx.mkBVAND(
                                        ctx.mkInt2BV(64, left.getIntRangeUpper()),
                                        ctx.mkInt2BV(64, right.getIntRangeUpper())),
                                true);
                lowerArith =
                        ctx.mkBV2Int(
                                ctx.mkBVAND(
                                        ctx.mkInt2BV(64, left.getIntRangeLower()),
                                        ctx.mkInt2BV(64, right.getIntRangeLower())),
                                true);
                break;
            case OR:
                upperArith =
                        ctx.mkBV2Int(
                                ctx.mkBVOR(
                                        ctx.mkInt2BV(64, left.getIntRangeUpper()),
                                        ctx.mkInt2BV(64, right.getIntRangeUpper())),
                                true);
                lowerArith =
                        ctx.mkBV2Int(
                                ctx.mkBVOR(
                                        ctx.mkInt2BV(64, left.getIntRangeLower()),
                                        ctx.mkInt2BV(64, right.getIntRangeLower())),
                                true);
                break;
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
