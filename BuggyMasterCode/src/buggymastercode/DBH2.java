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
public class DBH2 implements DBConnection {

    private Connection m_cn;

    boolean m_isValid = false;  // esto esta por que la pc se levanto medio boluda
                                // y no anda el metodo isValid de JDBC asi que lo
                                // resolvimos con esta variable local que no es lo
                                // mejor pero almenos funca :P
    String m_server = "";
    String m_user = "";
    String m_password = "";
    
    public String test = "";

    @Override
    public boolean connect(String server, String database, String user, String password) {
        return connect(server, user, password);
    }

    @Override
    public boolean connect(String server, String user, String password) {
        G.setHourglass();
        try {
            org.h2.Driver.load();

            m_cn = DriverManager.getConnection(server, user, password);
            initDataBase();

            m_server = server;
            m_user = user;
            m_password = password;

            m_isValid = true;
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            G.setDefaultCursor();
        }
    }

    private void initDataBase() throws Exception {
        Statement stat = m_cn.createStatement();
        stat.execute("SET SCHEMA PUBLIC");
        //stat.execute("drop table tclass");
        stat.execute("CREATE TABLE IF NOT EXISTS tclass (" +
                "cl_id int PRIMARY KEY, " +
                "cl_javaname varchar(255) DEFAULT '' NOT NULL," +
                "cl_vbname varchar(255) DEFAULT '' NOT NULL," +
                "cl_packagename varchar(255) DEFAULT '' NOT NULL," +
                "cl_ispublicenum tinyint DEFAULT 0 NOT NULL," +
                "cl_enumparentclass varchar(255) DEFAULT '' NOT NULL)");
        //stat.execute("drop table tfunction");
        stat.execute("CREATE TABLE IF NOT EXISTS tfunction (" +
                "cl_id int DEFAULT 0 NOT NULL," +
                "fun_id int PRIMARY KEY," +
                "fun_javaname varchar(255) DEFAULT '' NOT NULL," +
                "fun_vbname varchar(255) DEFAULT '' NOT NULL," +
                "fun_datatype varchar(255) DEFAULT '' NOT NULL);");
        //stat.execute("drop table tvariable");
        stat.execute("CREATE TABLE IF NOT EXISTS tvariable (" +
                "cl_id int DEFAULT 0 NOT NULL," +
                "fun_id int DEFAULT 0 NOT NULL," +
                "var_id int PRIMARY KEY," +
                "var_javaname varchar(255) DEFAULT '' NOT NULL," +
                "var_vbname varchar(255) DEFAULT '' NOT NULL," +
                "var_datatype varchar(255) DEFAULT '' NOT NULL," +
                "var_isparameter tinyint DEFAULT 0 NOT NULL," +
                "var_ispublic tinyint DEFAULT 0 NOT NULL);");
        //stat.execute("drop table topenrecent");
        stat.execute("CREATE TABLE IF NOT EXISTS topenrecent (" +
                "or_id int PRIMARY KEY," +
                "or_file varchar(500) DEFAULT '' NOT NULL);");
        //stat.execute("drop table tproject");
        stat.execute("CREATE TABLE IF NOT EXISTS tproject (" +
                "prj_id int PRIMARY KEY," +
                "prj_name varchar(500) DEFAULT '' NOT NULL," +
                "prj_dllname varchar(500) DEFAULT '' NOT NULL);");
        //stat.execute("drop table treference");
        stat.execute("CREATE TABLE IF NOT EXISTS treference (" +
                "prj_id int," +
                "ref_id int PRIMARY KEY," +
                "ref_name varchar(500) DEFAULT '' NOT NULL);");
        //stat.execute("drop table tpreference");
        stat.execute("CREATE TABLE IF NOT EXISTS tpreference (" +
                "pr_id varchar(255) PRIMARY KEY," +
                "pr_value varchar(500) DEFAULT '' NOT NULL);");
        stat.execute("CREATE SEQUENCE IF NOT EXISTS seq_tclass_id");
        stat.execute("CREATE SEQUENCE IF NOT EXISTS seq_tfunction_id");
        stat.execute("CREATE SEQUENCE IF NOT EXISTS seq_tvariable_id");
        stat.execute("CREATE SEQUENCE IF NOT EXISTS seq_topenrecent_id");
        stat.execute("CREATE SEQUENCE IF NOT EXISTS seq_tproject_id");
        stat.execute("CREATE SEQUENCE IF NOT EXISTS seq_treference_id");
        stat.execute("CREATE INDEX classJavaName ON tclass(cl_javaname)");
        stat.execute("CREATE INDEX classVbName ON tclass(cl_vbname)");
        stat.execute("CREATE INDEX packageName ON tclass(cl_packagename)");
        stat.execute("CREATE INDEX funJavaName ON tfunction(fun_javaname)");
        stat.execute("CREATE INDEX funVbName ON tfunction(fun_vbname)");
        stat.execute("CREATE INDEX funDataType ON tfunction(fun_datatype)");
        stat.execute("CREATE INDEX funClId ON tfunction(cl_id)");
        stat.execute("CREATE INDEX varJavaName ON tvariable(var_javaname)");
        stat.execute("CREATE INDEX varVbName ON tvariable(var_vbname)");
        stat.execute("CREATE INDEX varDataType ON tvariable(var_datatype)");
        stat.execute("CREATE INDEX varClId ON tvariable(cl_id)");
        stat.execute("CREATE INDEX varFunId ON tvariable(fun_id)");
        stat.execute("CREATE INDEX prjName ON tproject(prj_name)");
        stat.execute("CREATE INDEX prjDllName ON tproject(prj_dllname)");
        stat.execute("CREATE INDEX refName ON treference(ref_name)");
        stat.execute("CREATE INDEX refPrjId ON treference(prj_id)");
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
                rs.setMetaData(rset.getMetaData());

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
                rs.setMetaData(rset.getMetaData());

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
                stmt.execute(sqlstmt);
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
            BuggyMasterCodeApp.getLogger().log(Level.WARNING, "the connection to the H2 server can't be achieve because the variabes server, user or password aren't set");
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
            BuggyMasterCodeApp.getLogger().log(Level.WARNING, "the connection with the H2 server could not be established, try " + i);
        }
        BuggyMasterCodeApp.getLogger().log(Level.WARNING, "the connection with the H2 server could not be established");
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
        String sqlstmt = "select seq_" + table + "_id.nextval as id";
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
            id.setId(((Number)(rs.getRows().get(0).get("id"))).intValue());
            G.setDefaultCursor();
            return true;
        }
    }
}
