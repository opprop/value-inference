package inference;

import value.qual.*;

public class Variables {

    @StringVal String stringField = "string";
    @BoolVal boolean boolField = true;
    @IntRange(from=0, to=0) Integer integerField = 0;
    @IntRange(from=0, to=0) int intField = 0;

  	void localVars() {
  		@StringVal String stringVar = "string";
  	    @BoolVal boolean boolVar = true;
  	    @IntRange(from=0, to=0) Integer integerVar = 0;
  	    @IntRange(from=0, to=0) int intVar = 0;
    }
}