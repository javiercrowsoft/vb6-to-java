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
public class OpenRecent {

    private int m_id = 0;
    private String m_file = "";

    public void setId(int value) {m_id = value;}
    public int getId() {return m_id;}
    public void setFile(String value) {m_file = value;}

    public boolean saveOpenRecent() {
        if (m_id == Db.CS_NO_ID) {

            DataBaseId id = new DataBaseId();

            if (!Db.db.getNewId("topenrecent", id)) {
                return false;
            }

            String sqlstmt = "insert into topenrecent (or_id, or_file)"
                            + " values ("
                            + id.getId().toString()
                            + ", " + Db.getString(m_file)
                            + ")";

            if (Db.db.execute(sqlstmt)) {
                m_id = id.getId();
            }
            else {
                return false;
            }
        }
        else {

            String sqlstmt = "update topenrecent set "
                            + "or_file = "  + Db.getString(m_file)
                            + " where or_id = " + Integer.toString(m_id);

            if (!Db.db.execute(sqlstmt)) {
                return false;
            }
        }
        return true;
    }

    public boolean addOpenRecent(String file) {
        m_file = file;
        if (!getOpenRecentIdFromName())
            return false;
        if (m_id != Db.CS_NO_ID)
            return true;
        return saveOpenRecent();
    }

    public boolean deleteOpenRecent() {
        String sqlstmt = "delete from topenrecent where or_id = " + ((Integer)m_id).toString();
        if (Db.db.execute(sqlstmt)) {
            return true;
        }
        else {
            return false;
        }
    }

    public static String[] getOpenRecentList() {

        G.setHourglass();
        String sqlstmt = "select * from topenrecent";
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
            String[] list = new String[rs.getRows().size()];
            int j = 0;
            for (Iterator<DynaBean> i = rs.getRows().iterator(); i.hasNext(); j++) {
                DynaBean row = i.next();
                list[j] = row.get("or_file").toString();
            }
            G.setDefaultCursor();
            return list;
        }
    }

    public boolean getOpenRecentIdFromName() {

        G.setHourglass();
        setId(Db.CS_NO_ID);
        String sqlstmt = "select or_id from topenrecent where or_file = "
                            + Db.getString(m_file);
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
            setId(((Number)(rs.getRows().get(0).get("or_id"))).intValue());
            G.setDefaultCursor();
            return true;
        }
    }
}
