import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Reader;

import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.common.value.qual.IntVal;

public class FalsePositive {
	/**
	 * commons-bcel/src/main/java/org/apache/bcel/classfile/Signature.java:165
	 */
	private static void matchIdent( final MyByteArrayInputStream in, final StringBuilder buf ) {
        int ch;
        if ((ch = in.read()) == -1) {
            throw new RuntimeException("Illegal signature: " + in.getData()
                    + " no ident, reaching EOF");
        }
        char value = (char) ch;
    }
	
	/**
	 * commons-csv/src/main/java/org/apache/commons/csv/Lexer.java:264
	 */
	private Token parseEncapsulatedToken() throws IOException {
		int c;
		Reader reader = new FileReader("afile");
        c = reader.read();
        if (!isEndOfFile(c)) {
            char val = (char) c;
        }
    }
	
	/**
     * @return true if the given character indicates end of file
     */
    boolean isEndOfFile(final int ch) {
        return ch == -1;
    }

}