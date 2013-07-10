/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author jalvarez
 */
public class TranslatorWorker extends SwingWorker<Boolean, Boolean> {

    static private final String newline = "\n";
            
    private String m_path = "";
    private String m_vbpFile = "";
    private String m_packageName = "";
    private ArrayList<SourceFile> m_collFiles = new ArrayList<SourceFile>();
    private Translator m_translator = new Translator();
    private BuggyMasterCodeView m_caller = null;
    private boolean m_translateToJava = false;

    public TranslatorWorker(BuggyMasterCodeView caller, 
            String path, 
            String vbpFile, 
            ArrayList<SourceFile> collFiles,
            boolean translateToJava) {
        m_path = path;
        m_collFiles = collFiles;
        m_vbpFile = vbpFile;
        m_caller = caller;
        m_translateToJava = translateToJava;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        G.setNoChangeMousePointer(true);
        doWork(m_vbpFile);
        G.setNoChangeMousePointer(false);
        return true;
    }

    @Override
    protected void done() {
        try {
            m_caller.workDone();
        }
        catch (Exception ignore) {
        }
    }

    private void doWork(String vbpFile) {
        ByRefString value = new ByRefString();

        m_caller.initProgress();

        m_packageName = "";

        // Get package name
        //
        if (G.getToken(vbpFile, "Name", 1 , value)) {
            if (!value.text.isEmpty()) {
                m_packageName = value.text.replaceAll("\"", "") ;
            }
        }

        if (m_packageName.isEmpty()) {
            G.showInfo("Package name can't be found in: " + vbpFile);
            return;
        }

        m_translator.deletePackage(m_packageName);
        m_translator.setCaller(this);

        // References
        //
        int line = 1;
        int k = 0;
        String[] references = new String[500];
        references[k] = m_packageName;
        k++;
        references[k] = "VBA";
        k++;
        if (G.getToken(vbpFile, "Reference", line, value)) {
            while (!value.text.isEmpty()) {
                references[k] = getReferenceName(value.text);
                if (isAdoDBReference(references[k])) {
                    k++;
                    line++;
                    references[k] = "ADODB";
                }
                k++;
                line++;
                if (!G.getToken(vbpFile, "Reference", line, value)) {
                    break;
                }
                //progressBar.setValue(line);
            }
        }
        references = G.redim(references, k);
        m_translator.setReferences(references);
        
        // Parse
        //
        line = 1;
        if (G.getToken(vbpFile, "Form", line, value)) {
            while (!value.text.isEmpty()) {
                // stop if the user wants to cancel
                //
                if (m_caller.getCancel())
                    return;
                parseFile(value.text);
                line++;
                if (!G.getToken(vbpFile, "Form", line, value)) {
                    break;
                }
                //progressBar.setValue(line);
            }
        }
        line = 1;
        if (G.getToken(vbpFile, "Module", line, value)) {
            while (!value.text.isEmpty()) {
                // stop if the user wants to cancel
                //
                if (m_caller.getCancel())
                    return;
                parseFile(value.text);
                line++;
                if (!G.getToken(vbpFile, "Module", line, value)) {
                    break;
                }
                //progressBar.setValue(line);
            }
        }
        line = 1;
        if (G.getToken(vbpFile, "Class", line, value)) {
            while (!value.text.isEmpty()) {
                // stop if the user wants to cancel
                //
                if (m_caller.getCancel())
                    return;
                parseFile(value.text);
                line++;
                if (!G.getToken(vbpFile, "Class", line, value)) {
                    break;
                }
                //progressBar.setValue(line);
            }
        }

        // Translate
        //
        m_translator.setSourceFiles(m_collFiles);

        int indexFile = 0;
        line = 1;
        if (G.getToken(vbpFile, "Form", line, value)) {
            while (!value.text.isEmpty()) {
                // stop if the user wants to cancel
                //
                if (m_caller.getCancel())
                    return;
                translateFile(value.text, indexFile++);
                line++;
                if (!G.getToken(vbpFile, "Form", line, value)) {
                    break;
                }
            }
        }
        line = 1;
        if (G.getToken(vbpFile, "Module", line, value)) {
            while (!value.text.isEmpty()) {
                // stop if the user wants to cancel
                //
                if (m_caller.getCancel())
                    return;
                translateFile(value.text, indexFile++);
                line++;
                if (!G.getToken(vbpFile, "Module", line, value)) {
                    break;
                }
            }
        }
        line = 1;
        if (G.getToken(vbpFile, "Class", line, value)) {
            while (!value.text.isEmpty()) {
                // stop if the user wants to cancel
                //
                if (m_caller.getCancel())
                    return;
                translateFile(value.text, indexFile++);
                line++;
                if (!G.getToken(vbpFile, "Class", line, value)) {
                    break;
                }
            }
        }

        // G class
        //
        Preference pref = PreferenceObject.getPreference(G.C_AUX_FUN_ID);
        if (pref.getValue().equals(G.C_AUX_FUN_IN_G_CLASS)) {
            addClass("G", m_translator.getGClass());
        }
    }

    public void parseFile(String vbFile) {

        m_caller.addMessage("Parsing " + vbFile);

        String vbFullFile = vbFile;

        SourceFile sourceFile = new SourceFile();
        m_collFiles.add(sourceFile);

        //txSourceCode.setText("");
        //txSourceCodeJava.setText("");

        FileInputStream fstream = null;

        try {

            if (vbFullFile.contains(";")) {
                vbFullFile = vbFullFile.substring(vbFullFile.indexOf(";")+2);
            }

            vbFullFile = m_path + vbFullFile;

            m_translator.initDbObjects();
            m_translator.initTranslator(vbFullFile);
            m_translator.setPackage(m_packageName);

            if (m_translator.isVbSource()) {
                fstream = new FileInputStream(G.getFileForOS(vbFullFile));
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    // stop if the user wants to cancel
                    //
                    if (m_caller.getCancel())
                        return;
                    m_translator.parse(strLine);
                }
                sourceFile.setVbName(m_translator.getVbClassName());
                sourceFile.setJavaName(m_translator.getJavaClassName());
                sourceFile.setPublicFunctions(m_translator.getPublicFunctions());
                sourceFile.setPrivateFunctions(m_translator.getPrivateFunctions());
                sourceFile.setPublicVariables(m_translator.getPublicVariables());
                sourceFile.setRaiseEventFunctions(m_translator.getRaiseEventFunctions());
                sourceFile.setFileName(vbFile);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BuggyMasterCodeView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BuggyMasterCodeView.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fstream.close();
            } catch (IOException ex) {
                Logger.getLogger(BuggyMasterCodeView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void addPublicType(Type type) {
        SourceFile sourceFile = new SourceFile();
        m_collFiles.add(sourceFile);
        sourceFile.setVbName(type.vbName);
        sourceFile.setJavaName(type.javaName);
        sourceFile.setPublicFunctions(new ArrayList<Function>());
        sourceFile.setPrivateFunctions(new ArrayList<Function>());
        sourceFile.setPublicVariables(type.getMembersVariables());
        sourceFile.setFileName("PUBLIC TYPE: " + type.javaName);
        sourceFile.setVbSource(type.getVbCode().toString());
        sourceFile.setJavaSource(type.getJavaCode().toString());
    }

    public void addClass(String className, String classCode) {
        SourceFile sourceFile = new SourceFile();
        m_collFiles.add(sourceFile);
        sourceFile.setVbName(className);
        sourceFile.setJavaName(className);
        sourceFile.setPublicFunctions(new ArrayList<Function>());
        sourceFile.setPrivateFunctions(new ArrayList<Function>());
        sourceFile.setPublicVariables(new ArrayList<Variable>());
        sourceFile.setFileName(className);
        sourceFile.setVbSource("' This class was generated by the tool");
        sourceFile.setJavaSource(classCode);
    }

    public void translateFile(String vbFile, int indexFile) {

        m_caller.addMessage("Translating " + vbFile);

        SourceFile sourceFile = m_collFiles.get(indexFile);

        StringBuilder sourceCode = new StringBuilder();
        StringBuilder sourceCodeJava = new StringBuilder();

        FileInputStream fstream = null;

        try {

            if (vbFile.contains(";")) {
                vbFile = vbFile.substring(vbFile.indexOf(";")+2);
            }

            vbFile = m_path + vbFile;

            m_translator.initTranslator(vbFile);
            m_translator.setTranslateToJava(m_translateToJava);
            m_translator.setRaiseEventFunctions(sourceFile.getRaiseEventFunctions());

            if (m_translator.isVbSource()) {
                fstream = new FileInputStream(G.getFileForOS(vbFile));
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    // stop if the user wants to cancel
                    //
                    if (m_caller.getCancel())
                        return;
                    m_caller.addVbLine(strLine);
                    sourceCode.append(strLine);
                    sourceCode.append(newline);
                    strLine = m_translator.translate(strLine);
                    m_caller.addJavaLine(strLine);
                    sourceCodeJava.append(strLine);
                }
                sourceCodeJava.append(m_translator.getEventListenerCollection());
                sourceCodeJava.append(m_translator.getAuxFunctions());
                sourceCodeJava.append("}" + newline);
                m_translator.implementListeners(sourceCodeJava);
                sourceCodeJava.insert(0, m_translator.getImportSection());
                sourceFile.setVbSource(sourceCode.toString());
                sourceFile.setJavaSource(sourceCodeJava.toString() + newline + m_translator.getSubClasses());
                m_translator.addEventListenerInterface();
                m_translator.addEventListenerAdapter();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BuggyMasterCodeView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(BuggyMasterCodeView.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fstream.close();
            } catch (Exception ex) {
                Logger.getLogger(BuggyMasterCodeView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private String getReferenceName(String reference) {
        boolean sharpFound = false;
        String rtn = "";
        for (int i = reference.length()-1; i >-1; i--) {
            if (sharpFound) {
                if (reference.charAt(i) == '\\')
                    break;
                rtn = reference.charAt(i) + rtn;
            }
            else if (reference.charAt(i) == '#') {
                sharpFound = true;
            }
        }
        if (rtn.contains(".")) {
            for (int i = rtn.length() - 1; i > 0; i--) {
                if (rtn.charAt(i) == '.') {
                    rtn = rtn.substring(0, i);
                    break;
                }
            }
        }
        return rtn;
    }

    private boolean isAdoDBReference(String reference) {
        if (G.beginLike(reference, "msado21")) {
            return true;
        }
        else {
            return false;
        }
    }
}
