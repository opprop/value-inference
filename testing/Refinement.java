import value.qual.*;

public class Refinement {
	
	void refineParam(@IntRange(from=0, to=1) int x, @IntRange(from=0, to=1) int y) {
        x = 0;
        x = 1;
        y = x;
    }

	@IntRange(from=0, to=1) int refineReturn(int x) {
        x = 0;
        x = 1;
        return x;
    }
}
