package backend.z3smt;

import checkers.inference.model.CombVariableSlot;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.ExistentialVariableSlot;
import checkers.inference.model.LubVariableSlot;
import checkers.inference.model.RefinementVariableSlot;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.backend.AbstractFormatTranslator;
import checkers.inference.solver.frontend.Lattice;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

// AbstractFormatTranslator<SlotEncodingT, ConstraintEncodingT, SlotSolutionT>
public abstract class Z3SmtFormatTranslator<SlotEncodingT, SlotSolutionT>
        extends AbstractFormatTranslator<SlotEncodingT, BoolExpr, SlotSolutionT> {

    protected Context ctx;

    /** Cache of all serialized slots, keyed on slot ID. */
    protected final Map<Integer, SlotEncodingT> serializedSlots;

    public Z3SmtFormatTranslator(Lattice lattice) {
        super(lattice);
        serializedSlots = new HashMap<>();
    }

    public final void init(Context ctx) {
        this.ctx = ctx;
        finishInitializingEncoders();
    }

    protected abstract SlotEncodingT serializeVarSlot(VariableSlot slot);

    protected abstract SlotEncodingT serializeConstantSlot(ConstantSlot slot);

    @Override
    public SlotEncodingT serialize(VariableSlot slot) {
        return serializeVarSlot(slot);
    }

    @Override
    public SlotEncodingT serialize(ConstantSlot slot) {
        return serializeConstantSlot(slot);
    }

    @Override
    public SlotEncodingT serialize(ExistentialVariableSlot slot) {
        return serializeVarSlot(slot);
    }

    @Override
    public SlotEncodingT serialize(RefinementVariableSlot slot) {
        return serializeVarSlot(slot);
    }

    @Override
    public SlotEncodingT serialize(CombVariableSlot slot) {
        return serializeVarSlot(slot);
    }

    @Override
    public SlotEncodingT serialize(LubVariableSlot slot) {
        return serializeVarSlot(slot);
    }

    /**
     * Subclasses can override this method to perform pre-analysis of slots for encoding
     * optimization
     */
    public void preAnalyzeSlots(Collection<Slot> slots) {}

    public abstract String generateZ3SlotDeclaration(VariableSlot slot);

    public abstract BoolExpr encodeSlotWellformnessConstraint(VariableSlot slot);

    public abstract BoolExpr encodeSlotPreferenceConstraint(VariableSlot slot);

    public abstract Map<Integer, AnnotationMirror> decodeSolution(
            List<String> model, ProcessingEnvironment processingEnv);
}
