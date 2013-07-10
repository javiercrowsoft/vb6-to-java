/*
 * BuggyMasterCodeApp.java
 */

package buggymastercode;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class BuggyMasterCodeApp extends SingleFrameApplication {

    // constants
    public static final String C_DB_TYPE_ORACLE = "oracle";
    public static final String C_DB_TYPE_H2 = "h2";

    static final Logger m_logger = Logger.getLogger("ar.com.crowsoft.cvxi");
    FileHandler fh;

    private static DBConnection m_db;
    private static DBBuggyMasterCode m_dbBuggyMasterCode;

    public static Logger getLogger() {
    	return m_logger;
    }

    public static void setDb(DBConnection db) {
        m_db = db;
        Db.db = db;
    }

    /*public static DBConnection getDb() {
        return m_db;
    }*/

    public static void setDbBuggyMasterCode(DBBuggyMasterCode db) {
        m_dbBuggyMasterCode = db;
        Db.dbmc = db;
    }

    /**
     * At startup create and show the main frame of the application.
     */
    @Override 
    protected void startup() {
        BuggyMasterCodeView view = new BuggyMasterCodeView(this);
        show(view);

        // Login to database
        Login login;
        JFrame mainFrame = BuggyMasterCodeApp.getApplication().getMainFrame();
        login = new Login(mainFrame);
        if (login.initDialog()) {
            login.setLocationRelativeTo(mainFrame);

            BuggyMasterCodeApp.getApplication().show(login);

            if (startLog()) {
                if (login.getOk() == false) {
                    mainFrame.dispose();
                }
                else {
                    view.fillOpenRecentList();
                }
            }
        }
        else {
            G.showInfo("Initializing of MasterBuggyCode has failed");
            mainFrame.dispose();
        }
    }

    private boolean startLog() {
        try {
    		fh = new FileHandler("BuggyMasterCode.log");

                m_logger.addHandler(fh);
                m_logger.setLevel(Level.ALL);
                m_logger.info("initializing BuggyMasterCode");

                File currentDir = new File("");
                m_logger.info("running in: " + currentDir.getAbsolutePath());

                return true;
    	}
    	catch(Exception ex) {
    		m_logger.log(Level.WARNING, "failed to open the log file", ex);
    		return false;
    	}
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of BuggyMasterCodeApp
     */
    public static BuggyMasterCodeApp getApplication() {
        return Application.getInstance(BuggyMasterCodeApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(BuggyMasterCodeApp.class, args);
    }
}
