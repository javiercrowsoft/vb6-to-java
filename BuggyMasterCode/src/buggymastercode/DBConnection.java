/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

import java.sql.CallableStatement;
import java.sql.Connection;

/**
 *
 * @author jalvarez
 */
public interface DBConnection {
	public boolean connect(String server, String user, String password);
	public boolean connect(String server, String database, String user, String password);
	public boolean openRs(String sqlstmt, DBRecordSet rs);
	public boolean openRs(CallableStatement sqlstmt, DBRecordSet rs);
	public Connection getConnection();
        public boolean execute(String sqlstmt);
        public boolean execute(CallableStatement sqlstmt);
        public boolean getNewId(String table, DataBaseId id);
}
