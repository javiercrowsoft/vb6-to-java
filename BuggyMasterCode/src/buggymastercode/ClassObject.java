/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

/**
 *
 * @author jalvarez
 */
public class ClassObject {

    private int m_id = 0;
    private String m_packageName = "";
    private String m_vbName = "";
    private String m_javaName = "";
    private int m_isPublicEnum = 0;
    private String m_enumParentClass = "";

    public void setId(int value) {m_id = value;}
    public int getId() {return m_id;}
    public void setPackageName(String value) {m_packageName = value;}
    public void setVbName(String value) {m_vbName = value;}
    public void setJavaName(String value) {m_javaName = value;}
    public void setIsPublicEnum(boolean value) {m_isPublicEnum = value ? 1: 0;}
    public void setEnumParentClass(String value) {m_enumParentClass = value;}

    public boolean saveClass() {
        if (m_id == Db.CS_NO_ID) {

            DataBaseId id = new DataBaseId();

            if (!Db.db.getNewId("tclass", id)) {
                return false;
            }

            String sqlstmt = "insert into tclass (cl_id, cl_packagename, cl_vbname, cl_javaname, cl_ispublicenum, cl_enumparentclass) values ("
                            + id.getId().toString()
                            + ", " + Db.getString(m_packageName)
                            + ", " + Db.getString(m_vbName)
                            + ", " + Db.getString(m_javaName)
                            + ", " + Integer.toString(m_isPublicEnum)
                            + ", " + Db.getString(m_enumParentClass)
                            + ")";

            if (Db.db.execute(sqlstmt)) {
                m_id = id.getId();
            }
            else {
                return false;
            }

        }
        else {

            String sqlstmt = "update tclass set "
                            + "cl_packagename = "  + Db.getString(m_packageName)
                            + ", cl_vbname = "  + Db.getString(m_vbName)
                            + ", cl_javaname = "  + Db.getString(m_javaName)
                            + " where cl_id = " + ((Integer)m_id).toString();

            if (!Db.db.execute(sqlstmt)) {
                return false;
            }
        }
        return true;
    }

    public boolean deleteClass() {
        String sqlstmt = "delete from tclass where cl_id = " + ((Integer)m_id).toString();
        if (Db.db.execute(sqlstmt)) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean getClassIdFromClassName() {
        G.setHourglass();
        setId(Db.CS_NO_ID);
        String sqlstmt = "select cl_id from tclass"
                            + " where lower(cl_vbname) = " + Db.getString(m_vbName.toLowerCase())
                            + " and lower(cl_packagename) = " + Db.getString(m_packageName.toLowerCase());
        DBRecordSet rs = new DBRecordSet();
        if (!Db.db.openRs(sqlstmt, rs)) {
            G.setDefaultCursor();
            return false;
        }

        if (rs.getRows().isEmpty()) {
            G.setDefaultCursor();
            return false;
        }
        else {
            setId(((Number)(rs.getRows().get(0).get("cl_id"))).intValue());
            G.setDefaultCursor();
            return true;
        }
    }
    
    public String getDataTypeOfCollectionItem() {

        String sqlstmt = "select fun_id, fun_vbname, fun_javaname, fun_datatype from tfunction where cl_id <> 0 and cl_id = "
                            + ((Integer)m_id).toString() ;

        DBRecordSet rs = new DBRecordSet();
        if (!Db.db.openRs(sqlstmt, rs)) {return "";}

        // print the results
        for (int i = 0; i < rs.getRows().size(); i++) {
            
            if (rs.getRows().get(i).get("fun_vbname").toString().equals("Item")) {
                return rs.getRows().get(i).get("fun_datatype").toString();
            }
        }
        return "";        
    }    
}
