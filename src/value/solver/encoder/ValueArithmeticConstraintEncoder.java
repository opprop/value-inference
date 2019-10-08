package value.solver.encoder;

import checkers.inference.model.ArithmeticConstraint.ArithmeticOperationKind;
import checkers.inference.model.ArithmeticVariableSlot;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.backend.encoder.ArithmeticConstraintEncoder;
import checkers.inference.solver.frontend.Lattice;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import org.checkerframework.javacutil.BugInCF;
import value.solver.ValueFormatTranslator;
import value.solver.representation.Z3InferenceValue;

public class ValueArithmeticConstraintEncoder extends ValueAbstractConstraintEncoder
        implements ArithmeticConstraintEncoder<BoolExpr> {

    public ValueArithmeticConstraintEncoder(
            Lattice lattice, Context ctx, ValueFormatTranslator valueZ3SmtFormatTranslator) {
        super(lattice, ctx, valueZ3SmtFormatTranslator);
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

        switch (operation) {
            case PLUS:
                return ctx.mkAnd(
                        encoding,
                        ctx.mkImplies(
                                ctx.mkAnd(left.getIntRange(), right.getStringVal()),
                                res.getStringVal()),
                        ctx.mkImplies(
                                ctx.mkAnd(left.getStringVal(), right.getIntRange()),
                                res.getStringVal()),
                        ctx.mkImplies(
                                ctx.mkAnd(left.getStringVal(), right.getStringVal()),
                                res.getStringVal()),
                        ctx.mkOr(
                                ctx.mkAnd(
                                        res.getIntRange(),
                                        ctx.mkOr(
                                                ctx.mkEq(
                                                        res.getIntRangeUpper(),
                                                        ctx.mkAdd(
                                                                left.getIntRangeUpper(),
                                                                right.getIntRangeUpper())),
                                                ctx.mkAnd(
                                                		ctx.mkLt(
                                                                ctx.mkAdd(
                                                                        left.getIntRangeUpper(),
                                                                        right.getIntRangeUpper()),
                                                                ctx.mkInt(Long.MIN_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeUpper(),
                                                                ctx.mkInt(Long.MIN_VALUE))),
                                                ctx.mkAnd(
                                                		ctx.mkGt(
                                                                ctx.mkAdd(
                                                                        left.getIntRangeUpper(),
                                                                        right.getIntRangeUpper()),
                                                                ctx.mkInt(Long.MAX_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeUpper(),
                                                                ctx.mkInt(Long.MAX_VALUE)))),

                                        ctx.mkOr(
                                                ctx.mkEq(
                                                        res.getIntRangeLower(),
                                                        ctx.mkAdd(
                                                                left.getIntRangeLower(),
                                                                right.getIntRangeLower())),
                                                ctx.mkAnd(
                                                		ctx.mkLt(
                                                                ctx.mkAdd(
                                                                        left.getIntRangeLower(),
                                                                        right.getIntRangeLower()),
                                                                ctx.mkInt(Long.MIN_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeLower(),
                                                                ctx.mkInt(Long.MIN_VALUE))),
                                                ctx.mkAnd(
                                                		ctx.mkGt(
                                                                ctx.mkAdd(
                                                                        left.getIntRangeLower(),
                                                                        right.getIntRangeLower()),
                                                                ctx.mkInt(Long.MAX_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeLower(),
                                                                ctx.mkInt(Long.MAX_VALUE))))),
                                ctx.mkNot(res.getIntRange())));
            case MINUS:
                return ctx.mkAnd(
                        encoding,
                        ctx.mkNot(res.getStringVal()),
                        ctx.mkOr(
                                ctx.mkAnd(
                                        left.getIntRange(),
                                        right.getIntRange(),
                                        res.getIntRange(),
                                        ctx.mkOr(
                                                ctx.mkEq(
                                                        res.getIntRangeUpper(),
                                                        ctx.mkSub(
                                                                left.getIntRangeUpper(),
                                                                right.getIntRangeUpper())),
                                                ctx.mkAnd(
                                                		ctx.mkLt(
                                                                ctx.mkSub(
                                                                        left.getIntRangeUpper(),
                                                                        right.getIntRangeUpper()),
                                                                ctx.mkInt(Long.MIN_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeUpper(),
                                                                ctx.mkInt(Long.MIN_VALUE))),
                                                ctx.mkAnd(
                                                		ctx.mkGt(
                                                                ctx.mkSub(
                                                                        left.getIntRangeUpper(),
                                                                        right.getIntRangeUpper()),
                                                                ctx.mkInt(Long.MAX_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeUpper(),
                                                                ctx.mkInt(Long.MAX_VALUE)))),
                                        ctx.mkOr(
                                                ctx.mkEq(
                                                        res.getIntRangeLower(),
                                                        ctx.mkSub(
                                                                left.getIntRangeLower(),
                                                                right.getIntRangeLower())),
                                                ctx.mkAnd(
                                                		ctx.mkLt(
                                                                ctx.mkSub(
                                                                        left.getIntRangeLower(),
                                                                        right.getIntRangeLower()),
                                                                ctx.mkInt(Long.MIN_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeLower(),
                                                                ctx.mkInt(Long.MIN_VALUE))),
                                                ctx.mkAnd(
                                                		ctx.mkGt(
                                                                ctx.mkSub(
                                                                        left.getIntRangeLower(),
                                                                        right.getIntRangeLower()),
                                                                ctx.mkInt(Long.MAX_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeLower(),
                                                                ctx.mkInt(Long.MAX_VALUE))))),
                                ctx.mkAnd(
                                        ctx.mkNot(left.getIntRange()),
                                        ctx.mkNot(right.getIntRange()))));
            case MULTIPLY:
                return ctx.mkAnd(
                        encoding,
                        ctx.mkNot(res.getStringVal()),
                        ctx.mkOr(
                                ctx.mkAnd(
                                        left.getIntRange(),
                                        right.getIntRange(),
                                        res.getIntRange(),
                                        ctx.mkOr(
                                                ctx.mkEq(
                                                        res.getIntRangeUpper(),
                                                        ctx.mkMul(
                                                                left.getIntRangeUpper(),
                                                                right.getIntRangeUpper())),
                                                ctx.mkAnd(
                                                		ctx.mkLt(
                                                                ctx.mkMul(
                                                                        left.getIntRangeUpper(),
                                                                        right.getIntRangeUpper()),
                                                                ctx.mkInt(Long.MIN_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeUpper(),
                                                                ctx.mkInt(Long.MIN_VALUE))),
                                                ctx.mkAnd(
                                                		ctx.mkGt(
                                                                ctx.mkMul(
                                                                        left.getIntRangeUpper(),
                                                                        right.getIntRangeUpper()),
                                                                ctx.mkInt(Long.MAX_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeUpper(),
                                                                ctx.mkInt(Long.MAX_VALUE)))),
                                        ctx.mkOr(
                                                ctx.mkEq(
                                                        res.getIntRangeLower(),
                                                        ctx.mkMul(
                                                                left.getIntRangeLower(),
                                                                right.getIntRangeLower())),
                                                ctx.mkAnd(
                                                		ctx.mkLt(
                                                                ctx.mkMul(
                                                                        left.getIntRangeLower(),
                                                                        right.getIntRangeLower()),
                                                                ctx.mkInt(Long.MIN_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeLower(),
                                                                ctx.mkInt(Long.MIN_VALUE))),
                                                ctx.mkAnd(
                                                		ctx.mkGt(
                                                                ctx.mkMul(
                                                                        left.getIntRangeLower(),
                                                                        right.getIntRangeLower()),
                                                                ctx.mkInt(Long.MAX_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeLower(),
                                                                ctx.mkInt(Long.MAX_VALUE))))),
                                ctx.mkAnd(
                                        ctx.mkNot(left.getIntRange()),
                                        ctx.mkNot(right.getIntRange()))));
            case DIVIDE:
                return ctx.mkAnd(
                        encoding,
                        ctx.mkNot(res.getStringVal()),
                        ctx.mkOr(
                                ctx.mkAnd(
                                        left.getIntRange(),
                                        right.getIntRange(),
                                        res.getIntRange(),
                                        ctx.mkEq(
                                                res.getIntRangeUpper(),
                                                ctx.mkDiv(
                                                        left.getIntRangeUpper(),
                                                        right.getIntRangeUpper())),
                                        ctx.mkEq(
                                                res.getIntRangeLower(),
                                                ctx.mkDiv(
                                                        left.getIntRangeLower(),
                                                        right.getIntRangeLower()))),
                                ctx.mkAnd(
                                        ctx.mkNot(left.getIntRange()),
                                        ctx.mkNot(right.getIntRange()))));
            case REMAINDER:
                return ctx.mkAnd(
                        encoding,
                        ctx.mkNot(res.getStringVal()),
                        ctx.mkOr(
                                ctx.mkAnd(
                                        left.getIntRange(),
                                        right.getIntRange(),
                                        res.getIntRange(),
                                        ctx.mkEq(
                                                res.getIntRangeUpper(),
                                                ctx.mkRem(
                                                        left.getIntRangeUpper(),
                                                        right.getIntRangeUpper())),
                                        ctx.mkEq(
                                                res.getIntRangeLower(),
                                                ctx.mkRem(
                                                        left.getIntRangeLower(),
                                                        right.getIntRangeLower()))),
                                ctx.mkAnd(
                                        ctx.mkNot(left.getIntRange()),
                                        ctx.mkNot(right.getIntRange()))));
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
    }

    @Override
    public BoolExpr encodeVariable_Variable(
            ArithmeticOperationKind operation,
            VariableSlot leftOperand,
            VariableSlot rightOperand,
            ArithmeticVariableSlot result) {
        return encode(operation, leftOperand, rightOperand, result);
    }

    @Override
    public BoolExpr encodeVariable_Constant(
            ArithmeticOperationKind operation,
            VariableSlot leftOperand,
            ConstantSlot rightOperand,
            ArithmeticVariableSlot result) {
        return encode(operation, leftOperand, rightOperand, result);
    }

    @Override
    public BoolExpr encodeConstant_Variable(
            ArithmeticOperationKind operation,
            ConstantSlot leftOperand,
            VariableSlot rightOperand,
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
