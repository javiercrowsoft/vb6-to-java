/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

import java.util.Iterator;
import java.util.Map;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;

/**
 *
 * @author jalvarez
 */
public class FunctionObject {

    private int m_id = 0;
    private int m_cl_id = 0;
    private String m_vbName = "";
    private String m_javaName = "";
    private String m_dataType = "";

    public void setId(int value) {m_id = value;}
    public int getId() {return m_id;}
    public void setClId(int value) {m_cl_id = value;}
    public void setVbName(String value) {m_vbName = value;}
    public void setJavaName(String value) {m_javaName = value;}
    public void setDataType(String value) {m_dataType = value;}

    public boolean saveFunction() {
        if (m_id == Db.CS_NO_ID) {

            DataBaseId id = new DataBaseId();

            if (!Db.db.getNewId("tfunction", id)) {
                return false;
            }

            String sqlstmt = "insert into tfunction (cl_id, fun_id, fun_vbname, fun_javaname, fun_datatype) values ("
                            + ((Integer)m_cl_id).toString()
                            + ", " + id.getId().toString()
                            + ", " + Db.getString(m_vbName)
                            + ", " + Db.getString(m_javaName)
                            + ", " + Db.getString(m_dataType)
                            + ")";

            if (Db.db.execute(sqlstmt)) {
                m_id = id.getId();
            }
            else {
                return false;
            }
        }
        else {

            String sqlstmt = "update tfunction set "
                            + "fun_vbname = "  + Db.getString(m_vbName)
                            + ", fun_javaname = "  + Db.getString(m_javaName)
                            + ", fun_datatype = "  + Db.getString(m_dataType)
                            + " where fun_id = " + ((Integer)m_id).toString();

            if (!Db.db.execute(sqlstmt)) {
                return false;
            }
        }
        return true;
    }

    public boolean deleteFunction() {
        String sqlstmt = "delete from tfunction where fun_id = " + ((Integer)m_id).toString();
        if (Db.db.execute(sqlstmt)) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean getFunctionIdFromFunctionName() {
        G.setHourglass();
        setId(Db.CS_NO_ID);
        String sqlstmt = "select fun_id from tfunction"
                            + " where lower(fun_vbname) = " + Db.getString(m_vbName.toLowerCase())
                            + " and fun_javaname = " + Db.getString(m_javaName)
                            + " and cl_id = " + Integer.toString(m_cl_id);
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
            setId(((Number)(rs.getRows().get(0).get("fun_id"))).intValue());
            G.setDefaultCursor();
            return true;
        }
    }

    public static Function getFunctionFromName(
            String functionName,
            String className,
            String[] references) {

        if (functionName.trim().isEmpty())
            return null;
        if (className.trim().isEmpty())
            return null;

        if ("=;/+-:.(){}[]*\\".contains(functionName))
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
        String sqlstmt = "select f.*, cl_javaname, cl_packagename"
                            + " from tfunction f inner join tclass c"
                            + " on f.cl_id = c.cl_id"
                            + " where"
                            + " (lower(fun_vbname) = " + Db.getString(functionName.toLowerCase())
                            + " or fun_javaname = " + Db.getString(functionName)
                            + ") and (lower(cl_vbname) = " + Db.getString(className.toLowerCase())
                            + " or cl_javaname = " + Db.getString(className)
                            + ")";
        if (!packageName.isEmpty()) {
            sqlstmt += " and (lower(cl_packagename) = " + Db.getString(packageName.toLowerCase()) + ")";
        }
        sqlstmt += " order by fun_javaname;";
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
            Function fun = null;
            for (int i = 0; i < references.length; i++) {
                if (!(rs.isBOF() && rs.isEOF()))
                    rs.moveFirst();

                while (!rs.isEOF()) {
                    if (rs.getFields("cl_packagename").getValue().toString().equalsIgnoreCase(references[i])) {
                        fun = new Function();
                        fun.setJavaClassName(rs.getFields("cl_javaname").getValue().toString());
                        fun.getReturnType().packageName = rs.getFields("cl_packagename").getValue().toString();
                        fun.getReturnType().setJavaName(rs.getFields("fun_javaname").getValue().toString());
                        fun.getReturnType().setVbName(rs.getFields("fun_vbname").getValue().toString());
                        fun.getReturnType().setType(rs.getFields("fun_datatype").getValue().toString());
                        break;
                    }
                    rs.moveNext();
                }
                if (fun != null)
                    break;
            }
            G.setDefaultCursor();
            return fun;
        }
    }
}
