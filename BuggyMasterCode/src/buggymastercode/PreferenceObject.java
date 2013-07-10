/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

/**
 *
 * @author jalvarez
 */
public class PreferenceObject {

    public static boolean savePreference(Preference preference) {
        String sqlstmt = "";
        if (getPreference(preference.getId()) == null) {

            sqlstmt = "insert into tpreference (pr_id, pr_value) values ("
                            + Db.getString(preference.getId())
                            + ", " + Db.getString(preference.getValue())
                            + ")";

        }
        else {

            sqlstmt = "update tpreference set "
                            + "pr_value = "  + Db.getString(preference.getValue())
                            + " where pr_id = " + Db.getString(preference.getId());

        }
        return Db.db.execute(sqlstmt);
    }

    public static Preference getPreference(String id) {
        G.setHourglass();
        String sqlstmt = "select pr_value"
                            + " from tpreference"
                            + " where"
                            + " pr_id = " + Db.getString(id);
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
            Preference pref = new Preference();
            pref.setId(id);
            pref.setValue(rs.getRows().get(0).get("pr_value").toString());
            G.setDefaultCursor();
            return pref;
        }
    }
}