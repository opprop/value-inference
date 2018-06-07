package cast;

import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

public class CastChecker extends ValueChecker {

    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new CastVisitor(this);
    }
}
