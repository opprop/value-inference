import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Reader;

import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.common.value.qual.IntVal;
//import org.checkerframework.framework.qual.EnsuresQualifierif;

public class FalsePositive {
	
	/**
	 * commons-csv/src/main/java/org/apache/commons/csv/Lexer.java:264
	 */
	private void parseEncapsulatedToken() throws IOException {
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
    //@EnsuresQualifierif(result=true, expression="#1", qualifier=Intval.class) 
	boolean isEndOfFile(final int ch) {
        return ch == -1;
    }
    
    /**
      * commons-io/src/main/java/org/apache/commons/io/HexDump.java:104
      */
    public static void dump(final byte[] data, final long offset, final OutputStream stream, final int index)
           throws IOException, ArrayIndexOutOfBoundsException, IllegalArgumentException {
    	final StringBuilder buffer = new StringBuilder(74);
    	int chars_read = data.length;
    	for (int j = 0; j < chars_read; j++) {
	    	for (int k = 0; k < chars_read; k++) {
	    		if (data[k+j] >= ' ' && data[k+j] < 127) {
	    			buffer.append((char) data[k+j]);
	    		} else {
	    			buffer.append('.');
	    		}
		   }
    	}
    }
}