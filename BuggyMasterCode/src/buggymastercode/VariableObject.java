/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

import java.util.Iterator;
import org.apache.commons.beanutils.DynaBean;

/**
 *
 * @author jalvarez
 */
public class VariableObject {

    private int m_id = 0;
    private int m_cl_id = 0;
    private int m_fun_id = 0;
    private String m_vbName = "";
    private String m_javaName = "";
    private String m_dataType = "";
    private int m_isParameter = 0;
    private int m_isPublic = 0;

    public void setId(int value) {m_id = value;}
    public int getId() {return m_id;}
    public void setClId(int value) {m_cl_id = value;}
    public void setFunId(int value) {m_fun_id = value;}
    public void setVbName(String value) {m_vbName = value;}
    public void setJavaName(String value) {m_javaName = value;}
    public void setDataType(String value) {m_dataType = value;}
    public void setIsParameter(boolean value) {m_isParameter = value ? 1: 0;}
    public void setIsPublic(boolean value) {m_isPublic = value ? 1: 0;}

    public boolean saveVariable() {
        if (m_id == Db.CS_NO_ID) {

            DataBaseId id = new DataBaseId();

            if (!Db.db.getNewId("tvariable", id)) {
                return false;
            }

            String sqlstmt = "insert into tvariable (cl_id, fun_id, var_id, " 
                            + "var_vbname, var_javaname, var_datatype, " 
                            + "var_isparameter, var_ispublic) values ("
                            + Integer.toString(m_cl_id)
                            + ", " + Integer.toString(m_fun_id)
                            + ", " + id.getId().toString()
                            + ", " + Db.getString(m_vbName)
                            + ", " + Db.getString(m_javaName)
                            + ", " + Db.getString(m_dataType)
                            + ", " + Integer.toString(m_isParameter)
                            + ", " + Integer.toString(m_isPublic)
                            + ")";

            if (Db.db.execute(sqlstmt)) {
                m_id = id.getId();
            }
            else {
                return false;
            }
        }
        else {

            String sqlstmt = "update tvariable set "
                            + "var_vbname = "  + Db.getString(m_vbName)
                            + ", var_javaname = "  + Db.getString(m_javaName)
                            + ", var_datatype = "  + Db.getString(m_dataType)
                            + ", var_isparameter = "  + Integer.toString(m_isParameter)
                            + ", var_ispublic = "  + Integer.toString(m_isPublic)
                            + " where var_id = " + Integer.toString(m_id);

            if (!Db.db.execute(sqlstmt)) {
                return false;
            }
        }
        return true;
    }

    public boolean deleteVariable() {
        String sqlstmt = "delete from tvariable where var_id = " + ((Integer)m_id).toString();
        if (Db.db.execute(sqlstmt)) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean getVariableIdFromVariableName() {
        G.setHourglass();
        setId(Db.CS_NO_ID);
        String sqlstmt = "select var_id from tvariable"
                            + " where lower(var_vbname) = " + Db.getString(m_vbName.toLowerCase())
                            + " and cl_id = " + Integer.toString(m_cl_id)
                            + " and fun_id = " + Integer.toString(m_fun_id);
        DBRecordSet rs = new DBRecordSet();
        if (!Db.db.openRs(sqlstmt, rs)) {
            G.setDefaultCursor();
            return false;
        }

        if (rs.getRows().isEmpty()) {
            G.setDefaultCursor();
            return true;
        }
        else {
            setId(((Number)(rs.getRows().get(0).get("var_id"))).intValue());
            G.setDefaultCursor();
            return true;
        }
    }

    public static Variable getVariableFromName(
            String variableName,
            String className,
            String[] references) {
        Variable var = null;
        // member variables
        var = getVariableFromName(variableName, className, references, false, false, false);
        if (var == null) {
            // constants in private enums
            var = getVariableFromName(variableName, className, references, true, false, false);
            if (var == null) {
                // constants in public enums
                var = getVariableFromName(variableName, className, references, false, true, false);
                if (var != null)
                    var.isEnumMember = true;
                else
                    // global variables
                    var = getVariableFromName(variableName, className, references, false, false, true);
            }
            else
                var.isEnumMember = true;
        }
        return var;
    }

    private static Variable getVariableFromName(
            String variableName,
            String className,
            String[] references,
            boolean searchForPrivateEnums,
            boolean searchForPublicEnums,
            boolean searchForPublicMembers) {

        // this is to translate some global object of vb
        // using variables for example err is translate to ex
        //
        if (searchForPublicMembers && className.trim().isEmpty())
            className = "VBA";
        
        if (variableName.trim().isEmpty())
            return null;
        if (className.trim().isEmpty() && !searchForPublicEnums)
            return null;

        if ("=;/+-:.(){}[]*\\".contains(variableName))
            return null;

        // if classname contains a package name
        // we need split in package and class
        //
        String packageName = "";
        if (className.contains(".")) {
            int n = className.indexOf(".");
            packageName = className.substring(0, n);
            className = className.substring(n + 1, className.length());
        }

        G.setHourglass();
        String sqlstmt = "select v.*, cl_packagename, cl_javaname"
                            + " from tvariable v inner join tclass c"
                            + " on v.cl_id = c.cl_id and v.fun_id = 0"
                            + " where"
                            + " (lower(var_vbname) = " + Db.getString(variableName.toLowerCase())
                            + " or var_javaname = " + Db.getString(variableName) + ")";

        if (searchForPrivateEnums) {
            sqlstmt += " and (cl_enumparentclass = " + Db.getString(className)
                            + ")";
        }
        else if(searchForPublicEnums) {
            sqlstmt += " and (cl_ispublicenum <> 0)";
        }
        //else {
        if (!className.isEmpty()) {
            sqlstmt += " and (lower(cl_vbname) = " + Db.getString(className.toLowerCase())
                            + " or cl_javaname = " + Db.getString(className)
                            + ")";
        }
        if (!packageName.isEmpty()) {
            sqlstmt += " and (lower(cl_packagename) = " + Db.getString(packageName.toLowerCase()) + ")";
        }
        DBRecordSet rs = new DBRecordSet();
        if (!Db.db.openRs(sqlstmt, rs)) {
            G.setDefaultCursor();
            return null;
        }

        if (rs.getRows().isEmpty()) {
            G.setDefaultCursor();
            return null;
        }
        else {
            Variable var = null;
            for (int i = 0; i < references.length; i++) {
                for (Iterator<DynaBean> j = rs.getRows().iterator(); j.hasNext();) {
                    DynaBean row = j.next();
                    if (row.get("cl_packagename").toString().equals(references[i])) {
                        var = new Variable();
                        var.packageName = row.get("cl_packagename").toString();
                        var.setJavaName(row.get("var_javaname").toString());
                        var.setVbName(row.get("var_vbname").toString());
                        var.isParam = (Byte)row.get("var_isparameter") != 0 ? true : false;
                        var.isPublic = (Byte)row.get("var_ispublic") != 0 ? true : false;
                        var.className = row.get("cl_javaname").toString();
                        var.setType(row.get("var_datatype").toString());
                        break;
                    }
                }
                if (var != null)
                    break;
            }
            G.setDefaultCursor();
            return var;
        }
    }
}
