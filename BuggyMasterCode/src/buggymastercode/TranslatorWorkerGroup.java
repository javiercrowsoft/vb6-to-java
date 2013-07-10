/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package buggymastercode;

import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author javier
 */
public class TranslatorWorkerGroup extends SwingWorker<Boolean, Boolean> {

    private String m_vbgFile = "";
    private BuggyMasterCodeView m_caller = null;
    ArrayList<Project> m_projects = new ArrayList<Project>();
    
    public TranslatorWorkerGroup(BuggyMasterCodeView caller, 
            String vbgFile) {
        m_vbgFile = vbgFile;
        m_caller = caller;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        G.setHourglass();
        G.setNoChangeMousePointer(true);
        doWork(m_vbgFile);
        G.setNoChangeMousePointer(false);
        G.setDefaultCursor();
        return true;
    }

    @Override
    protected void done() {
        try {
            m_caller.setStatusBarMessage("");
            m_caller.showVBGroupDialog(m_projects);
        }
        catch (Exception ignore) {
        }
    }

    private void doWork(String vbgFile) {
        // Parse
        //
        int line = 1;
        ByRefString value = new ByRefString();

        if (G.getToken(vbgFile, "Project", line, value)) {
            while (!value.text.isEmpty()) {
                m_caller.setStatusBarMessage("Loading references for " + value.text);
                // stop if the user wants to cancel
                //
                if (m_caller.getCancel())
                    return;
                Project project = new Project();
                project.setName(G.getFileName(value.text));
                String path = G.getFilePath(vbgFile);
                String filePath = G.getFilePath(value.text);
                if (!filePath.isEmpty()) {
                    if (filePath.contains(":"))
                        path = filePath;
                    else
                        path += "\\" + filePath;
                }
                project.setPath(path);
                if (!project.save())
                    return;
                if (!project.loadReferences())
                    return;
                m_projects.add(project);
                line++;
                if (!G.getToken(vbgFile, "Project", line, value)) {
                    break;
                }
            }
        }

        if (G.getToken(vbgFile, "StartupProject", 1, value)) {
            if (!value.text.isEmpty()) {
                m_caller.setStatusBarMessage("Loading references for " + value.text);
                Project project = new Project();
                project.setName(G.getFileName(value.text));
                String path = G.getFilePath(vbgFile);
                String filePath = G.getFilePath(value.text);
                if (!filePath.isEmpty())
                    path += "\\" + filePath;
                project.setPath(path);
                if (!project.save())
                    return;
                if (!project.loadReferences())
                    return;
                m_projects.add(project);
            }
        }

        for (int i = 0; i < m_projects.size(); i++) {
            m_caller.setStatusBarMessage("Getting level of refences for " + m_projects.get(i).getName());
            m_projects.get(i).getLevelFromReferences();
        }
    }
    
}
