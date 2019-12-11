package value.solver.encoder;

import checkers.inference.model.ArithmeticConstraint.ArithmeticOperationKind;
import checkers.inference.model.ArithmeticVariableSlot;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.encoder.ArithmeticConstraintEncoder;
import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.frontend.Lattice;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
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
            case LEFT_SHIFT:
                // The value of n << s is n left-shifted s bit positions. Equivalent to
                // multiplication by 2 to the power s.
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
                                                                ctx.mkPower(
                                                                        ctx.mkInt(2),
                                                                        right.getIntRangeUpper()))),
                                                ctx.mkAnd(
                                                        ctx.mkLt(
                                                                ctx.mkMul(
                                                                        left.getIntRangeUpper(),
                                                                        ctx.mkPower(
                                                                                ctx.mkInt(2),
                                                                                right
                                                                                        .getIntRangeUpper())),
                                                                ctx.mkInt(Long.MIN_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeUpper(),
                                                                ctx.mkInt(Long.MIN_VALUE))),
                                                ctx.mkAnd(
                                                        ctx.mkGt(
                                                                ctx.mkMul(
                                                                        left.getIntRangeUpper(),
                                                                        ctx.mkPower(
                                                                                ctx.mkInt(2),
                                                                                right
                                                                                        .getIntRangeUpper())),
                                                                ctx.mkInt(Long.MAX_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeUpper(),
                                                                ctx.mkInt(Long.MAX_VALUE)))),
                                        ctx.mkOr(
                                                ctx.mkEq(
                                                        res.getIntRangeLower(),
                                                        ctx.mkMul(
                                                                left.getIntRangeLower(),
                                                                ctx.mkPower(
                                                                        ctx.mkInt(2),
                                                                        right.getIntRangeLower()))),
                                                ctx.mkAnd(
                                                        ctx.mkLt(
                                                                ctx.mkMul(
                                                                        left.getIntRangeLower(),
                                                                        ctx.mkPower(
                                                                                ctx.mkInt(2),
                                                                                right
                                                                                        .getIntRangeLower())),
                                                                ctx.mkInt(Long.MIN_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeLower(),
                                                                ctx.mkInt(Long.MIN_VALUE))),
                                                ctx.mkAnd(
                                                        ctx.mkGt(
                                                                ctx.mkMul(
                                                                        left.getIntRangeLower(),
                                                                        ctx.mkPower(
                                                                                ctx.mkInt(2),
                                                                                right
                                                                                        .getIntRangeLower())),
                                                                ctx.mkInt(Long.MAX_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeLower(),
                                                                ctx.mkInt(Long.MAX_VALUE))))),
                                ctx.mkAnd(
                                        ctx.mkNot(left.getIntRange()),
                                        ctx.mkNot(right.getIntRange()))));
            case RIGHT_SHIFT:
                // The value of n >> s is n right-shifted s bit positions with sign-extension.
                // Resulting value is ⌊ n / 2s ⌋.
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
                                                        ctx.mkPower(
                                                                ctx.mkInt(2),
                                                                right.getIntRangeUpper()))),
                                        ctx.mkEq(
                                                res.getIntRangeLower(),
                                                ctx.mkDiv(
                                                        left.getIntRangeLower(),
                                                        ctx.mkPower(
                                                                ctx.mkInt(2),
                                                                right.getIntRangeLower())))),
                                ctx.mkAnd(
                                        ctx.mkNot(left.getIntRange()),
                                        ctx.mkNot(right.getIntRange()))));
            case UNSIGNED_RIGHT_SHIFT:
                /* The value of n >>> s is n right-shifted s bit positions with zero-extension:
                If n >= 0, then the result is n >> s.
                If n < 0, then the result is (n >> s) + (2L << ~s). where ~s == (-s)-1. */
                return ctx.mkAnd(
                        encoding,
                        ctx.mkNot(res.getStringVal()),
                        ctx.mkOr(
                                ctx.mkAnd(
                                        left.getIntRange(),
                                        right.getIntRange(),
                                        res.getIntRange(),
                                        ctx.mkOr(
                                                ctx.mkAnd(
                                                        ctx.mkGe(
                                                                left.getIntRangeUpper(),
                                                                ctx.mkInt(0)),
                                                        ctx.mkEq(
                                                                res.getIntRangeUpper(),
                                                                ctx.mkDiv(
                                                                        left.getIntRangeUpper(),
                                                                        ctx.mkPower(
                                                                                ctx.mkInt(2),
                                                                                right
                                                                                        .getIntRangeUpper())))),
                                                ctx.mkAnd(
                                                        ctx.mkLt(
                                                                left.getIntRangeUpper(),
                                                                ctx.mkInt(0)),
                                                        ctx.mkEq(
                                                                res.getIntRangeUpper(),
                                                                ctx.mkAdd(
                                                                        ctx.mkDiv(
                                                                                left
                                                                                        .getIntRangeUpper(),
                                                                                ctx.mkPower(
                                                                                        ctx.mkInt(
                                                                                                2),
                                                                                        right
                                                                                                .getIntRangeUpper())),
                                                                        ctx.mkMul(
                                                                                ctx.mkInt(2),
                                                                                ctx.mkPower(
                                                                                        ctx.mkInt(
                                                                                                2),
                                                                                        ctx.mkSub(
                                                                                                ctx
                                                                                                        .mkMul(
                                                                                                                ctx
                                                                                                                        .mkInt(
                                                                                                                                -1),
                                                                                                                right
                                                                                                                        .getIntRangeUpper()),
                                                                                                ctx
                                                                                                        .mkInt(
                                                                                                                1))))))),
                                                ctx.mkAnd(
                                                        ctx.mkLt(
                                                                left.getIntRangeUpper(),
                                                                ctx.mkInt(0)),
                                                        ctx.mkLt(
                                                                ctx.mkAdd(
                                                                        ctx.mkDiv(
                                                                                left
                                                                                        .getIntRangeUpper(),
                                                                                ctx.mkPower(
                                                                                        ctx.mkInt(
                                                                                                2),
                                                                                        right
                                                                                                .getIntRangeUpper())),
                                                                        ctx.mkMul(
                                                                                ctx.mkInt(2),
                                                                                ctx.mkPower(
                                                                                        ctx.mkInt(
                                                                                                2),
                                                                                        ctx.mkSub(
                                                                                                ctx
                                                                                                        .mkMul(
                                                                                                                ctx
                                                                                                                        .mkInt(
                                                                                                                                -1),
                                                                                                                right
                                                                                                                        .getIntRangeUpper()),
                                                                                                ctx
                                                                                                        .mkInt(
                                                                                                                1))))),
                                                                ctx.mkInt(Long.MIN_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeUpper(),
                                                                ctx.mkInt(Long.MIN_VALUE))),
                                                ctx.mkAnd(
                                                        ctx.mkLt(
                                                                left.getIntRangeUpper(),
                                                                ctx.mkInt(0)),
                                                        ctx.mkGt(
                                                                ctx.mkAdd(
                                                                        ctx.mkDiv(
                                                                                left
                                                                                        .getIntRangeUpper(),
                                                                                ctx.mkPower(
                                                                                        ctx.mkInt(
                                                                                                2),
                                                                                        right
                                                                                                .getIntRangeUpper())),
                                                                        ctx.mkMul(
                                                                                ctx.mkInt(2),
                                                                                ctx.mkPower(
                                                                                        ctx.mkInt(
                                                                                                2),
                                                                                        ctx.mkSub(
                                                                                                ctx
                                                                                                        .mkMul(
                                                                                                                ctx
                                                                                                                        .mkInt(
                                                                                                                                -1),
                                                                                                                right
                                                                                                                        .getIntRangeUpper()),
                                                                                                ctx
                                                                                                        .mkInt(
                                                                                                                1))))),
                                                                ctx.mkInt(Long.MAX_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeUpper(),
                                                                ctx.mkInt(Long.MAX_VALUE)))),
                                        ctx.mkOr(
                                                ctx.mkAnd(
                                                        ctx.mkGe(
                                                                left.getIntRangeLower(),
                                                                ctx.mkInt(0)),
                                                        ctx.mkEq(
                                                                res.getIntRangeLower(),
                                                                ctx.mkDiv(
                                                                        left.getIntRangeLower(),
                                                                        ctx.mkPower(
                                                                                ctx.mkInt(2),
                                                                                right
                                                                                        .getIntRangeLower())))),
                                                ctx.mkAnd(
                                                        ctx.mkLt(
                                                                left.getIntRangeLower(),
                                                                ctx.mkInt(0)),
                                                        ctx.mkEq(
                                                                res.getIntRangeLower(),
                                                                ctx.mkAdd(
                                                                        ctx.mkDiv(
                                                                                left
                                                                                        .getIntRangeLower(),
                                                                                ctx.mkPower(
                                                                                        ctx.mkInt(
                                                                                                2),
                                                                                        right
                                                                                                .getIntRangeLower())),
                                                                        ctx.mkMul(
                                                                                ctx.mkInt(2),
                                                                                ctx.mkPower(
                                                                                        ctx.mkInt(
                                                                                                2),
                                                                                        ctx.mkSub(
                                                                                                ctx
                                                                                                        .mkMul(
                                                                                                                ctx
                                                                                                                        .mkInt(
                                                                                                                                -1),
                                                                                                                right
                                                                                                                        .getIntRangeLower()),
                                                                                                ctx
                                                                                                        .mkInt(
                                                                                                                1))))))),
                                                ctx.mkAnd(
                                                        ctx.mkLt(
                                                                left.getIntRangeLower(),
                                                                ctx.mkInt(0)),
                                                        ctx.mkLt(
                                                                ctx.mkAdd(
                                                                        ctx.mkDiv(
                                                                                left
                                                                                        .getIntRangeLower(),
                                                                                ctx.mkPower(
                                                                                        ctx.mkInt(
                                                                                                2),
                                                                                        right
                                                                                                .getIntRangeLower())),
                                                                        ctx.mkMul(
                                                                                ctx.mkInt(2),
                                                                                ctx.mkPower(
                                                                                        ctx.mkInt(
                                                                                                2),
                                                                                        ctx.mkSub(
                                                                                                ctx
                                                                                                        .mkMul(
                                                                                                                ctx
                                                                                                                        .mkInt(
                                                                                                                                -1),
                                                                                                                right
                                                                                                                        .getIntRangeLower()),
                                                                                                ctx
                                                                                                        .mkInt(
                                                                                                                1))))),
                                                                ctx.mkInt(Long.MIN_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeLower(),
                                                                ctx.mkInt(Long.MIN_VALUE))),
                                                ctx.mkAnd(
                                                        ctx.mkLt(
                                                                left.getIntRangeLower(),
                                                                ctx.mkInt(0)),
                                                        ctx.mkGt(
                                                                ctx.mkAdd(
                                                                        ctx.mkDiv(
                                                                                left
                                                                                        .getIntRangeLower(),
                                                                                ctx.mkPower(
                                                                                        ctx.mkInt(
                                                                                                2),
                                                                                        right
                                                                                                .getIntRangeLower())),
                                                                        ctx.mkMul(
                                                                                ctx.mkInt(2),
                                                                                ctx.mkPower(
                                                                                        ctx.mkInt(
                                                                                                2),
                                                                                        ctx.mkSub(
                                                                                                ctx
                                                                                                        .mkMul(
                                                                                                                ctx
                                                                                                                        .mkInt(
                                                                                                                                -1),
                                                                                                                right
                                                                                                                        .getIntRangeLower()),
                                                                                                ctx
                                                                                                        .mkInt(
                                                                                                                1))))),
                                                                ctx.mkInt(Long.MAX_VALUE)),
                                                        ctx.mkEq(
                                                                res.getIntRangeLower(),
                                                                ctx.mkInt(Long.MAX_VALUE))))),
                                ctx.mkAnd(
                                        ctx.mkNot(left.getIntRange()),
                                        ctx.mkNot(right.getIntRange()))));
            case XOR:
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
                                                ctx.mkBV2Int(
                                                        ctx.mkBVXOR(
                                                                ctx.mkInt2BV(
                                                                        64,
                                                                        left.getIntRangeUpper()),
                                                                ctx.mkInt2BV(
                                                                        64,
                                                                        right.getIntRangeUpper())),
                                                        true)),
                                        ctx.mkEq(
                                                res.getIntRangeLower(),
                                                ctx.mkBV2Int(
                                                        ctx.mkBVXOR(
                                                                ctx.mkInt2BV(
                                                                        64,
                                                                        left.getIntRangeUpper()),
                                                                ctx.mkInt2BV(
                                                                        64,
                                                                        right.getIntRangeUpper())),
                                                        true))),
                                ctx.mkAnd(
                                        ctx.mkNot(left.getIntRange()),
                                        ctx.mkNot(right.getIntRange()))));
            case AND:
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
                                                ctx.mkBV2Int(
                                                        ctx.mkBVAND(
                                                                ctx.mkInt2BV(
                                                                        64,
                                                                        left.getIntRangeUpper()),
                                                                ctx.mkInt2BV(
                                                                        64,
                                                                        right.getIntRangeUpper())),
                                                        true)),
                                        ctx.mkEq(
                                                res.getIntRangeLower(),
                                                ctx.mkBV2Int(
                                                        ctx.mkBVAND(
                                                                ctx.mkInt2BV(
                                                                        64,
                                                                        left.getIntRangeUpper()),
                                                                ctx.mkInt2BV(
                                                                        64,
                                                                        right.getIntRangeUpper())),
                                                        true))),
                                ctx.mkAnd(
                                        ctx.mkNot(left.getIntRange()),
                                        ctx.mkNot(right.getIntRange()))));
            case OR:
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
                                                ctx.mkBV2Int(
                                                        ctx.mkBVOR(
                                                                ctx.mkInt2BV(
                                                                        64,
                                                                        left.getIntRangeUpper()),
                                                                ctx.mkInt2BV(
                                                                        64,
                                                                        right.getIntRangeUpper())),
                                                        true)),
                                        ctx.mkEq(
                                                res.getIntRangeLower(),
                                                ctx.mkBV2Int(
                                                        ctx.mkBVOR(
                                                                ctx.mkInt2BV(
                                                                        64,
                                                                        left.getIntRangeUpper()),
                                                                ctx.mkInt2BV(
                                                                        64,
                                                                        right.getIntRangeUpper())),
                                                        true))),
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
