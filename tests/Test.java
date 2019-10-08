import value.qual.*;

public class Test {
	int two = 2;
    int zero = 0;
    String one = "one";
    boolean no = false;

    public int testing(int value) {
        if (two + 2 == 4){
        	return 0;
        }
        return 1;
    }

    public String testing2(int value) {
        return one + 2;
    }

    public int testing3(int value) {
        return two + 2;
    }
}
