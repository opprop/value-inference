package value.solver.encoder;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

import value.solver.representation.Z3InferenceValue;

public class ValueEncoderUtils {
	
	public ValueEncoderUtils () {}

	// sub <: super cases:
    // bot <: any
    // any <: top
    // str <: str
	// bool <: bool
	// int(from1, to1) <: int(from2, to2) | from1 >= from2 && to1 <= to2 
	public BoolExpr subtype(Context ctx, Z3InferenceValue subT, Z3InferenceValue superT) {
        BoolExpr subtypeEncoding =
                ctx.mkOr(
	        		// sub = bot
	                subT.getBottomVal(),
	                // super = top
	                superT.getUnknownVal(),
	                // if neither is top or bottom then they must be equal: sub = super
	                ctx.mkAnd(
	            		ctx.mkNot(subT.getUnknownVal()),
	                	ctx.mkNot(subT.getBottomVal()),
	                	ctx.mkNot(superT.getUnknownVal()),
	                	ctx.mkNot(superT.getBottomVal()),
	                	ctx.mkEq(subT.getBoolVal(), superT.getBoolVal()),
	            		ctx.mkEq(subT.getStringVal(), superT.getStringVal()),
	            		ctx.mkEq(subT.getIntRange(), superT.getIntRange()),
	            		// int <: int
	            		ctx.mkOr(
	        				ctx.mkAnd(
        						subT.getIntRange(),
        						ctx.mkGe(subT.getIntRangeLower(), superT.getIntRangeLower()),
        						ctx.mkLe(subT.getIntRangeUpper(), superT.getIntRangeUpper())),
	        				ctx.mkNot(subT.getIntRange())
	        			)
                    )
	            );

        return subtypeEncoding;
	}
	
	// x =: x
	public BoolExpr equality(Context ctx, Z3InferenceValue left, Z3InferenceValue right) {
        BoolExpr equalityEncoding =
                ctx.mkAnd(
                		ctx.mkEq(left.getBottomVal(), right.getBottomVal()),
                		ctx.mkEq(left.getUnknownVal(), right.getUnknownVal()),
                		ctx.mkEq(left.getBoolVal(), right.getBoolVal()),
                		ctx.mkEq(left.getStringVal(), right.getStringVal()),
                		ctx.mkEq(left.getIntRange(), right.getIntRange()),
                		// int = int
                		ctx.mkOr(
	        				ctx.mkAnd(
        						left.getIntRange(),
        						ctx.mkEq(left.getIntRangeLower(), right.getIntRangeLower()),
        						ctx.mkEq(left.getIntRangeUpper(), right.getIntRangeUpper())),
	        				ctx.mkNot(left.getIntRange())
	        				)
                        );
        
        return equalityEncoding;
	}
}
