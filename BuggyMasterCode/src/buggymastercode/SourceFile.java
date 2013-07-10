/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

import java.util.ArrayList;

/**
 *
 * @author jalvarez
 */
public class SourceFile {
    private String m_vbSource = "";
    private String m_javaSource = "";
    private String m_vbName = "";
    private String m_javaName = "";
    private String m_fileName = "";

    private ArrayList<Function> m_publicFunctions = null;
    private ArrayList<Function> m_privateFunctions = null;
    private ArrayList<Variable> m_publicVariables = null;
    private ArrayList<String> m_raiseEventFunctions = null;

    public String getVbSource() {
        return m_vbSource;
    }

    public void setVbSource(String value) {
        m_vbSource = value;
    }

    public String getJavaSource() {
        return m_javaSource;
    }

    public void setJavaSource(String value) {
        m_javaSource = value;
    }

    public String getVbName() {
        return m_vbName;
    }

    public void setVbName(String value) {
        m_vbName = value;
    }

    public String getJavaName() {
        return m_javaName;
    }

    public void setJavaName(String value) {
        m_javaName = value;
    }

    public String getFileName() {
        return m_fileName;
    }

    public void setFileName(String value) {
        m_fileName = value;
    }

    public ArrayList<Function> getPublicFunctions() {
        return m_publicFunctions;
    }

    public void setPublicFunctions(ArrayList<Function> list) {
        m_publicFunctions = list;
    }

    public ArrayList<Function> getPrivateFunctions() {
        return m_privateFunctions;
    }

    public void setPrivateFunctions(ArrayList<Function> list) {
        m_privateFunctions = list;
    }

    public ArrayList<Variable> getPublicVariables() {
        return m_publicVariables;
    }

    public void setPublicVariables(ArrayList<Variable> list) {
        m_publicVariables = list;
    }

    public ArrayList<String> getRaiseEventFunctions() {
        return m_raiseEventFunctions;
    }

    public void setRaiseEventFunctions(ArrayList<String> list) {
        m_raiseEventFunctions = list;
    }

    @Override
    public String toString() {
        return m_javaName;
    }
}
