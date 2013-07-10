/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

/**
 *
 * @author jalvarez
 */
public class Db {

    public static DBConnection db;
    public static DBBuggyMasterCode dbmc;

    public static final int CS_NO_ID = 0;

    public static int getId(Object value) {
        if (G.isNumeric(value)) {
            return Integer.parseInt(value.toString());
        }
        else {
            return 0;
        }
    }

    public static String getString(Object value) {
        String rtn = (String)value;
        return "'" + rtn.replaceAll("'", "''") + "'";
    }
}
