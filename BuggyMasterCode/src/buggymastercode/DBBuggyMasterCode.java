/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

/**
 *
 * @author jalvarez
 */
public interface DBBuggyMasterCode {
    public boolean updateClass(DBConnection db, ClassObject classObj);
    public boolean updateFunction(DBConnection db, FunctionObject functionObj);
    public boolean updateVariable(DBConnection db, VariableObject variableObj);
}
