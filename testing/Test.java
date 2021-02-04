import value.qual.IntRange;

public class Test {
    int zero = 0;
    String one = "one";
    boolean no = false;
    int two = 2;

    public int add() {
        int sum = 2;
        sum = -6 * sum;
        return sum;
    }

    public int testing(int x) {
        if (x == 4) {
            return x;
        } else {
            x = 1;
            return x;
        }
    }

    public String testing2(int value) {
        return one + 2;
    }

    public int testing4(int value) {
    	two = two + 2;
    	return two;
    }

//  Not work
//    public int setx() {
//    	int x = 2;
//        x = x + x;
//        x++;
//        x += x;
//        x++;
//        x = x + x;
//    	return x;
//    }

//  Not work
//    public byte casting(@IntRange(from = 0, to = 255)int value) {
//    	  // :: warning: (cast.unsafe)
//        return (byte)(value);
//    }
//
    void refineParam(int x, int y) {
        x = 0;
        x = 1;
        y = x;
    }

//  Not work
//    int forloop() {
//        int x;
//        int sum = 0;
//        int max = 5;
//        for (x = 0; sum < max; x++) {
//            sum++;
//        }
//        return sum;
//    }

//  Not work
//    int whileloop(int max) {
//        int x = 0;
//        int sum = 0;
//        while ( x < max ) {
//            sum++;
//            x++;
//        }
//        return sum;
//    }
}
