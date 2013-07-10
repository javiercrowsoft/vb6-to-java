/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package buggymastercode;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;

/**
 *
 * @author jalvarez
 */
public class DBOracle implements DBConnection {

    Connection m_cn = null;
    boolean m_isValid = false;  // esto esta por que la pc se levanto medio boluda
                                // y no anda el metodo isValid de JDBC asi que lo
                                // resolvimos con esta variable local que no es lo
                                // mejor pero almenos funca :P
    String m_server = "";
    String m_user = "";
    String m_password = "";

    @Override
    public boolean connect(String server, String database, String user, String password) {
        return connect(server, user, password);
    }

    @Override
    public boolean connect(String server, String user, String password) {
        G.setHourglass();
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

            String url = "jdbc:oracle:thin:@//" + server;
            m_cn = DriverManager.getConnection(url, user, password);
            m_cn.setAutoCommit(true);

            m_server = server;
            m_user = user;
            m_password = password;

            m_isValid = true;
            return true;
        } catch (ClassNotFoundException ex) {
            BuggyMasterCodeApp.getLogger().log(Level.WARNING, "the driver for connect to Oracle is not instaled", ex);
            return false;
        } catch (SQLException ex) {
            BuggyMasterCodeApp.getLogger().log(Level.WARNING, "the connection to the Oracle server could not be established", ex);
            return false;
        } finally {
            G.setDefaultCursor();
        }
    }

    @Override
    public boolean openRs(String sqlstmt, DBRecordSet rs) {
        G.setHourglass();
        if (!connect()) {
            return false;
        } else {

            try {
                Statement stmt = m_cn.createStatement();
                ResultSet rset = stmt.executeQuery(sqlstmt);

                RowSetDynaClass rsdc = new RowSetDynaClass(rset);
                rs.setRows((List<DynaBean>) rsdc.getRows());

                rset.close();
                stmt.close();
                return true;
            } catch (SQLException ex) {
                String msg = "the execution of the query [" + sqlstmt + "] has failed";
                BuggyMasterCodeApp.getLogger().log(Level.WARNING, msg, ex);
                G.showInfo(msg);
                return false;
            } finally {
                G.setDefaultCursor();
            }
        }
    }

    @Override
    public boolean openRs(CallableStatement sqlstmt, DBRecordSet rs) {
        G.setHourglass();
        if (!connect()) {
            return false;
        } else {

            try {
                sqlstmt.execute();
                ResultSet rset = (ResultSet) sqlstmt.getObject(1);

                RowSetDynaClass rsdc = new RowSetDynaClass(rset);
                rs.setRows((List<DynaBean>) rsdc.getRows());
                
                rset.close();
                sqlstmt.close();
                return true;
            } catch (SQLException ex) {
                String msg = "the execution of the query [" + sqlstmt.toString() + "] has failed";
                BuggyMasterCodeApp.getLogger().log(Level.WARNING, msg, ex);
                G.showInfo(msg);
                return false;
            } finally {
                G.setDefaultCursor();
            }
        }
    }

    @Override
    public boolean execute(String sqlstmt) {
        G.setHourglass();
        if (!connect()) {
            return false;
        } else {

            try {
                Statement stmt = m_cn.createStatement();
                stmt.executeQuery(sqlstmt);
                stmt.close();
                return true;
            } catch (SQLException ex) {
                String msg = "the execution of the query [" + sqlstmt + "] has failed";
                BuggyMasterCodeApp.getLogger().log(Level.WARNING, msg, ex);
                G.showInfo(msg);
                return false;
            } finally {
                G.setDefaultCursor();
            }
        }
    }

    @Override
    public boolean execute(CallableStatement sqlstmt) {
        G.setHourglass();
        if (!connect()) {
            return false;
        } else {

            try {
                sqlstmt.execute();
                sqlstmt.close();
                return true;
            } catch (SQLException ex) {
                String msg = "the execution of the query [" + sqlstmt.toString() + "] has failed";
                BuggyMasterCodeApp.getLogger().log(Level.WARNING, msg, ex);
                G.showInfo(msg);
                return false;
            } finally {
                G.setDefaultCursor();
            }
        }
    }

    private boolean connect() {
        G.setHourglass();
        if (m_server.isEmpty() || m_user.isEmpty() || m_password.isEmpty()) {
            BuggyMasterCodeApp.getLogger().log(Level.WARNING, "the connection to the Oracle server can't be achieve because the variabes server, user or password aren't set");
            G.setDefaultCursor();
            return false;
        }
        if (m_isValid) {
            G.setDefaultCursor();
            return true;
        }
        int i = 0;
        while (i < 5) {
            if (connect(m_server, m_user, m_password)) {
                G.setDefaultCursor();
                return true;
            }
            i++;
            BuggyMasterCodeApp.getLogger().log(Level.WARNING, "the connection with the Oracle server could not be established, try " + i);
        }
        BuggyMasterCodeApp.getLogger().log(Level.WARNING, "the connection with the Oracle server could not be established");
        G.setDefaultCursor();
        return false;
    }

    @Override
    public Connection getConnection() {
        return m_cn;
    }

    @Override
    public boolean getNewId(String table, DataBaseId id) {
        G.setHourglass();
        String sqlstmt = "select seq_" + table + "_id.nextval from dual";
        DBRecordSet rs = new DBRecordSet();
        if (!openRs(sqlstmt, rs)) {
            G.setDefaultCursor();
            return false;
        }

        if (rs.getRows().isEmpty()) {
            G.setDefaultCursor();
            return false;
        }
        else {
            id.setId(((Number)(rs.getRows().get(0).get("nextval"))).intValue());
            G.setDefaultCursor();
            return true;
        }
    }
}
