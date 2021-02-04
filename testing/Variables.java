import value.qual.IntRange;

public class Variables {

    String stringField = "string";
    boolean boolField = true;
    @IntRange(from=0, to=0) Integer integerField = 0;
    @IntRange(from=0, to=0) int intField = 0;

  	void localVars() {
  	    String stringVar = "string";
  	    boolean boolVar = true;
  	    @IntRange(from=0, to=0) Integer integerVar = 0;
  	    @IntRange(from=0, to=0) int intVar = 0;
    }
}
