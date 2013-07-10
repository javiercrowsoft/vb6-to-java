/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;

/**
 *
 * @author jalvarez
 */
public class G {

    static public final String C_AUX_FUN_ID = "Auxiliary-Functions";
    static public final String C_AUX_FUN_IN_CLASS_SOURCE = "in class source";
    static public final String C_AUX_FUN_IN_G_CLASS = "in G class";
    static public final String C_AUX_FUN_IN_CS_LIBRARY = "in CSUtil library";
    static public final String C_AUX_ADO_REPLACE_ID = "ADO-Replace";

    /**
     * value = " +-/*,;"
     */
    static private final String C_SYMBOLS = " +-*/,;";
    /**
     * value = " +-/*,;()[]{}"
     */
    static private final String C_SYMBOLS2 = " +-*/,;()[]{}";
    /**
     * value = " \t"
     */
    static private final String C_SPACES = " \t";

    static private boolean m_noChangeMousePointer = false;

    /**
     *
     * @param value
     */
    public static void setNoChangeMousePointer(boolean value) {
        m_noChangeMousePointer = value;
    }

    /**
     *
     * @param value
     * @return
     */
    public static boolean isNumeric(Object value) {
        try {
            double d = Double.parseDouble(value.toString());
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param msg
     */
    public static void showInfo(String msg) {
        JFrame mainFrame = BuggyMasterCodeApp.getApplication().getMainFrame();
        JOptionPane.showMessageDialog(mainFrame,msg);
    }

    /**
     *
     * @param table
     * @param id
     * @return
     */
    public static boolean setRowSelectedById(JTable table, int id) {
        int rows = table.getModel().getRowCount();
        if (rows > 0) {
            int indexRow = -1;
            for (int i = 0; i < rows; i++) {
                if (id == Db.getId(table.getValueAt(i, 0))) {
                    indexRow = i;
                    break;
                }
            }
            if (indexRow >= 0) {
                return setRowSelected(table, indexRow);
            }
            else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     *
     * @param table
     * @param indexRow
     * @return
     */
    public static boolean setRowSelected(JTable table, int indexRow) {
        if (indexRow >= 0 && indexRow < table.getModel().getRowCount()) {
            // The following row selection methods work only if these
            // properties are set this way table.setColumnSelectionAllowed(false);
            table.setRowSelectionAllowed(true);
            // Select a row - row indexRow
            table.setRowSelectionInterval(indexRow, indexRow);
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     */
    public static void setHourglass() {
        setHourglass(BuggyMasterCodeApp.getApplication().getMainFrame());
    }
    /**
     *
     * @param frame
     */
    public static void setHourglass(JFrame frame) {
        if (!m_noChangeMousePointer) {
            Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
            if (frame.getCursor() != hourglassCursor) {
                frame.setCursor(hourglassCursor);
            }
        }
    }
    /**
     *
     */
    public static void setDefaultCursor() {
        setDefaultCursor(BuggyMasterCodeApp.getApplication().getMainFrame());
    }
    /**
     *
     * @param frame
     */
    public static void setDefaultCursor(JFrame frame) {
        if (!m_noChangeMousePointer) {
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            frame.setCursor(normalCursor);
        }
    }

    /**
     *
     * @param str
     * @return
     */
    public static String ltrim(String str)
    {
        //If the argument is null then return empty string
        if(str==null) return "";
        if(str.isEmpty()) return "";

        /* The charAt method returns the character at a particular position in a String.
         * We check to see if the character at position 0 (the leading character) is a space.
         * If it is, use substring to make a new String that starts after the space.
         */
        while(str.charAt(0) == ' ')
        {
            str = str.substring(1);
            if(str.isEmpty()) return "";
        }
        return str;
    }

    /**
     *
     * @param str
     * @return
     */
    public static String rtrim(String str) {
        //If the argument is null then return empty string
        if(str==null) return "";
        if(str.isEmpty()) return "";

        /* The logic for Rtrim is, While the last character in the String is a space, remove it.
         * In the code, take the length of the string and use it to determine if the last character is a space.
         */
        int len = str.length();
        while(str.charAt(len-1) == ' ')
        {
            str = str.substring(0,len-1);
            len--;
        }
        return str;
    }

    /**
     *
     * @param str
     * @return
     */
    public static String ltrimTab(String str)
    {
        //If the argument is null then return empty string
        if(str==null) return "";
        if(str.isEmpty()) return "";

        /* The charAt method returns the character at a particular position in a String.
         * We check to see if the character at position 0 (the leading character) is a space.
         * If it is, use substring to make a new String that starts after the space.
         */
        while(str.charAt(0) == ' ')
        {
            str = str.substring(1);
            if(str.isEmpty()) return "";
        }
        while(str.charAt(0) == '\t')
        {
            str = str.substring(1);
            if(str.isEmpty()) return "";
        }
        return str;
    }

    /**
     *
     * @param source
     * @param begin
     * @return
     */
    public static boolean beginLike(String source, String begin) {
        int len = begin.length();
        if (source.length() < len)
            return false;
        return (source.substring(0,len).equalsIgnoreCase(begin));
    }

    /**
     *
     * @param source
     * @param end
     * @return
     */
    public static boolean endLike(String source, String end) {
        int len = end.length();
        if (source.length() < len)
            return false;
        return (source.substring(source.length()-len).equalsIgnoreCase(end));
    }

    /**
     *
     * @param c
     * @param count
     * @return
     */
    public final static String rep(char c, int count) {
        char[] s = new char[count];
        for (int i = 0; i < count; i++) {
            s[i] = c;
        }
        return new String(s).intern();
    } // end rep

    /**
     *
     * @param vbpFile
     * @param token
     * @param line
     * @param value
     * @return
     */
    public static boolean getToken(String vbpFile, String token, int line, ByRefString value) {
        int currLine=0;
        int lenToken = token.length();
        FileInputStream fstream = null;
        value.text = "";

        if (!token.substring(0,lenToken).equals("=")) {
            token += "=";
            lenToken++;
        }

        try {

            fstream = new FileInputStream(vbpFile);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (strLine.length() >= lenToken) {
                    if (strLine.substring(0,lenToken).equals(token)) {
                        currLine++;
                        if (currLine == line) {
                            value.text = strLine.substring(lenToken);
                            break;
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BuggyMasterCodeView.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(BuggyMasterCodeView.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (Exception ex) {
            Logger.getLogger(BuggyMasterCodeView.class.getName()).log(Level.SEVERE, null, ex);
            return false;            
        } finally {
            try {
                fstream.close();
                return true;
            } catch (IOException ex) {
                Logger.getLogger(BuggyMasterCodeView.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } catch (Exception ex) {
                Logger.getLogger(BuggyMasterCodeView.class.getName()).log(Level.SEVERE, null, ex);
                return false; 
            }
        }
    }

    /**
     *
     * @param source
     * @param toFind
     * @return
     */
    public static boolean contains(String source, String toFind) {
        boolean literalFlag = false;
        String expression = "";
        int openParentheses = 0;

        for (int i = 0; i < source.length(); i++) {
            if (source.charAt(i) == '"') {
                literalFlag = !literalFlag;
            }
            if (!literalFlag) {

                if (openParentheses == 0) {
                    if (source.charAt(i) == '(') {
                        openParentheses++;
                    }
                    expression += String.valueOf(source.charAt(i));
                }
                else {
                    if (source.charAt(i) == '(') {
                        openParentheses++;
                    }
                    else if (source.charAt(i) == ')') {
                        openParentheses--;
                        if (openParentheses == 0) {
                            expression += String.valueOf(source.charAt(i));
                        }
                    }
                }
            }
        }
        return expression.contains(toFind);
    }

    /**
     * split a string using {@link buggymastercode.G#C_SYMBOLS C_SYMBOLS} as separators
     *
     * <p>the resulting array contains the characters
     * defined by the C_SYMBOLS constant
     *
     * <p>the text sourronded by parenthesis is not
     * splitted eg:
     *
     *<p>      "var1.var2.var3.var4.var5 (var.var.var) "var.var.var""
     *
     *<p>  is translated as
     *
     *<p>      return[0] = "var1.var2.var3.var4.var5"
     *<br>     return[1] = " "
     *<br>     return[2] = "("
     *<br>     return[3] = "var.var.var"
     *<br>     return[4] = ")"
     *<br>     return[5] = " "
     *<br>     return[6] = ""var.var.var""
     *
     * @param strLine
     * @return
     */
    public static String[] split(String strLine) {
        return split(strLine, C_SYMBOLS);
    }

    /**
     * split a string using {@link buggymastercode.G#C_SYMBOLS2 C_SYMBOLS2} as separators
     *
     * <p>the resulting array contains the characters
     * defined by the C_SYMBOLS constant
     *
     * <p>the text sourronded by parenthesis IS splitted eg:
     *
     *<p>      "var1,var2,var3,var4,var5 (var,var,var) "var,var,var""
     *
     *<p>  is translated as
     *
     *<p>      return[0] = "var1"
     *<br>     return[1] = ","
     *<br>     return[2] = "var2"
     *<br>     return[3] = ","
     *<br>     return[4] = "var3"
     *<br>     return[5] = ","
     *<br>     return[6] = "var4"
     *<br>     return[7] = ","
     *<br>     return[8] = "var5"
     *<br>     return[9] = " "
     *<br>     return[10] = "("
     *<br>     return[11] = "var"
     *<br>     return[12] = ","
     *<br>     return[13] = "var"
     *<br>     return[14] = ","
     *<br>     return[15] = "var"
     *<br>     return[16] = ")"
     *<br>     return[17] = " "
     *<br>     return[18] = ""var,var,var""
     *
     * @param strLine
     * @return
     */
    public static String[] split2(String strLine) {
        return split2(strLine, C_SYMBOLS2);
    }

    /**
     * split a string using the characters given in
     * the symbols parameter as separators
     *
     * <p>the resulting array contains the characters
     * defined by the symbols parameter
     *
     * <p>the text sourronded by parenthesis IS splitted eg:
     *
     *<p>      "var1.var2.var3.var4.var5 (var.var.var) "var.var.var""
     *
     *<p>with a "(.)" as a separator is translated as
     *
     *<p>      return[0] = "var1"
     *<br>     return[1] = "."
     *<br>     return[2] = "var2"
     *<br>     return[3] = "."
     *<br>     return[4] = "var3"
     *<br>     return[5] = "."
     *<br>     return[6] = "var4"
     *<br>     return[7] = "."
     *<br>     return[8] = "var5"
     *<br>     return[9] = "("
     *<br>     return[10] = "var"
     *<br>     return[11] = "."
     *<br>     return[12] = "var"
     *<br>     return[13] = "."
     *<br>     return[14] = "var"
     *<br>     return[15] = ")"
     *<br>     return[16] = " "
     *<br>     return[16] = ""var.var.var""
     *
     * @param strLine
     * @return
     */
    public static String[] split2(String strLine, String symbols) {
        boolean literalFlag = false;
        boolean numberFlag = false;
        String[] words = new String[500];
        String word = "";
        int j = 0;
        boolean wordEnded = false;

        for (int i = 0; i < strLine.length(); i++) {
            if (strLine.charAt(i) == '"') {
                literalFlag = !literalFlag;
            }
            if (!literalFlag) {

                if (strLine.charAt(i) == '#') {
                    numberFlag = !numberFlag;
                }

                if (!numberFlag) {

                    if (symbols.contains(String.valueOf(strLine.charAt(i)))) {
                        wordEnded = true;
                    }
                    if (wordEnded) {
                        j = addWord(word, strLine, words, i, j);
                        wordEnded = false;
                        if (!word.isEmpty()) {
                            word = "";
                        }
                    }
                    else {
                        word += String.valueOf(strLine.charAt(i));
                    }
                }
                else {
                    word += String.valueOf(strLine.charAt(i));
                }
            }
            else {
                word += String.valueOf(strLine.charAt(i));
            }
        }
        if (!word.isEmpty()) {
            words[j] = word;
            j++;
        }
        String[] rtn = new String[j];
        for (int i = 0; i < j; i++) {
            rtn[i] = words[i];
        }
        return rtn;
    }

    /**
     * split a string using the characters given in
     * the symbols parameter as separators
     *
     * <p>the resulting array contains the characters
     * defined by the symbols parameter
     *
     * <p>the text sourronded by parenthesis is not
     * splitted eg:
     *
     *<p>      "var1.var2.var3.var4.var5 (var.var.var) "var.var.var""
     *
     *<p>  with a "." as a separator is translated as
     *
     *<p>      return[0] = "var1"
     *<br>     return[1] = "."
     *<br>     return[2] = "var2"
     *<br>     return[3] = "."
     *<br>     return[4] = "var3"
     *<br>     return[5] = "."
     *<br>     return[6] = "var4"
     *<br>     return[7] = "."
     *<br>     return[8] = "var5"
     *<br>     return[9] = "("
     *<br>     return[10] = "var.var.var"
     *<br>     return[11] = ")"
     *<br>     return[12] = ""var.var.var""
     *
     * @param strLine   is the string to split
     * @param symbols   an string with the character to be used as separators
     * @return
     */
    public static String[] split(String strLine, String symbols) {
        boolean literalFlag = false;
        boolean numberFlag = false;
        String[] words = new String[500];
        String word = "";
        int j = 0;
        int openParentheses = 0;
        boolean wordEnded = false;

        for (int i = 0; i < strLine.length(); i++) {
            if (strLine.charAt(i) == '"') {
                literalFlag = !literalFlag;
            }
            if (!literalFlag) {

                if (strLine.charAt(i) == '#') {
                    numberFlag = !numberFlag;
                }

                if (!numberFlag) {

                    if (openParentheses == 0) {
                        if (strLine.charAt(i) == '(') {
                            openParentheses++;
                            wordEnded = true;
                        }
                        else if (symbols.contains(String.valueOf(strLine.charAt(i)))) {
                            wordEnded = true;
                        }
                        if (wordEnded) {
                            j = addWord(word, strLine, words, i, j);
                            wordEnded = false;
                            if (!word.isEmpty()) {
                                word = "";
                            }
                        }
                        else {
                            word += String.valueOf(strLine.charAt(i));
                        }
                    }
                    else {
                        if (strLine.charAt(i) == '(') {
                            openParentheses++;
                            word += String.valueOf(strLine.charAt(i));
                        }
                        else if (strLine.charAt(i) == ')') {
                            openParentheses--;
                            if (openParentheses == 0) {
                                j = addWord(word, strLine, words, i, j);
                                wordEnded = false;
                                if (!word.isEmpty()) {
                                    word = "";
                                }
                            }
                            else {
                                word += String.valueOf(strLine.charAt(i));
                            }
                        }
                        else {
                            word += String.valueOf(strLine.charAt(i));
                        }
                    }
                }
                else {
                    word += String.valueOf(strLine.charAt(i));
                }
            }
            else {
                word += String.valueOf(strLine.charAt(i));
            }
        }
        if (!word.isEmpty()) {
            words[j] = word;
            j++;
        }
        String[] rtn = new String[j];
        for (int i = 0; i < j; i++) {
            rtn[i] = words[i];
        }
        return rtn;
    }

    /**
     * split a string using the spaces and tabs
     * as separators {@link buggymastercode.G#C_SPACES C_SPACES}
     *
     * <p>the resulting array doesn't contain the spaces
     * nither the tabs
     *
     * <p>This function doesn't recognize parenthesis as separators eg:
     *
     *<p>      "var1 var2 var3     var4 var5( var var ) "var var var""
     *
     *<p>  is translated as
     *
     *<p>      return[0] = "var1"
     *<br>     return[1] = "var2"
     *<br>     return[2] = "var3"
     *<br>     return[3] = "var4"
     *<br>     return[4] = "var5("
     *<br>     return[5] = "var"
     *<br>     return[6] = "var"
     *<br>     return[7] = ")"
     *<br>     return[8] = ""var var var""
     *
     * @param strLine
     * @return
     */
    public static String[] splitSpace(String strLine) {
        return split3(strLine, C_SPACES);
    }

    /**
     * split a string using the characters given in
     * the symbols parameter as separators
     *
     * <p>the resulting array doesn't contain the characters
     * defined by the symbols parameter
     *
     *<p>      "var1.var2.var3.var4.var5 (var.var.var) "var.var.var""
     *
     *<p>with a "(.)" as a separator is translated as
     *
     *<p>      return[0] = "var1"
     *<br>     return[1] = "var2"
     *<br>     return[2] = "var3"
     *<br>     return[3] = "var4"
     *<br>     return[4] = "var5 "
     *<br>     return[5] = "var"
     *<br>     return[6] = "var"
     *<br>     return[7] = "var"
     *<br>     return[8] = " "var var var""
     *
     * @param strLine
     * @return
     */
    public static String[] split3(String strLine, String symbols) {
        boolean literalFlag = false;
        boolean numberFlag = false;
        String[] words = new String[500];
        String word = "";
        int j = 0;
        boolean wordEnded = false;

        for (int i = 0; i < strLine.length(); i++) {
            if (strLine.charAt(i) == '"') {
                literalFlag = !literalFlag;
            }
            if (!literalFlag) {

                if (strLine.charAt(i) == '#') {
                    numberFlag = !numberFlag;
                }

                if (!numberFlag) {

                    if (symbols.contains(String.valueOf(strLine.charAt(i)))) {
                        wordEnded = true;
                    }
                    if (wordEnded) {
                        if (!word.isEmpty()) {
                            words[j] = word;
                            word = "";
                            j++;
                        }
                        wordEnded = false;
                        if (!word.isEmpty()) {
                            word = "";
                        }
                    }
                    else {
                        word += String.valueOf(strLine.charAt(i));
                    }
                }
                else {
                    word += String.valueOf(strLine.charAt(i));
                }
            }
            else {
                word += String.valueOf(strLine.charAt(i));
            }
        }
        if (!word.isEmpty()) {
            words[j] = word;
            j++;
        }
        String[] rtn = new String[j];
        for (int i = 0; i < j; i++) {
            rtn[i] = words[i];
        }
        return rtn;
    }

    private static int addWord(String word, String strLine, String[] words, int i, int j) {
        if (!word.isEmpty()) {
            /*if (j >= words.length) {
                int dummy = 0;
            }*/
            words[j] = word;
            word = "";
            j++;
        }
        /*if (j >= words.length) {
            int dummy = 0;
        }*/
        words[j] = String.valueOf(strLine.charAt(i));
        j++;
        return j;
    }

    /**
     *
     * @param source
     * @param size
     * @return
     */
    public static String[] redim(String[] source, int size) {
        if (size == 0) {
            return null;
        }
        else {
            String[] tmp = new String[size];
            if (source != null) {
                for (int i = 0; i < Math.min(source.length, tmp.length); i++) {
                    tmp[i] = source[i];
                }
            }
            return tmp;
        }
    }

    public static String getFileForOS(String file) {
        String nameOS = "os.name";
        if (System.getProperty(nameOS).toLowerCase().contains("windows")) {
            return file;
        }
        else {
            return file.replace("\\", "/");
        }
    }

    public static String getFileName(String fullPath) {
        String fileName = fullPath;
        for (int i = fullPath.length() - 1; i > 0; i--) {
            if (fullPath.charAt(i) == '\\' || fullPath.charAt(i) == '/') {
                fileName = fullPath.substring(i + 1);
                break;
            }
        }
        return fileName;
    }

    public static String getFilePath(String fullPath) {
        String path = "";
        for (int i = fullPath.length() - 1; i > 0; i--) {
            if (fullPath.charAt(i) == '\\' || fullPath.charAt(i) == '/') {
                path = fullPath.substring(0, i);
                break;
            }
        }
        return path;
    }
    
}
