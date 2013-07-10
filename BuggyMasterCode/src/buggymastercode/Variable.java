/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

/**
 *
 * @author jalvarez
 */
public class Variable {

    private String m_javaName = "";
    private String m_vbName = "";

    public boolean isString = false;
    public boolean isLong = false;
    public boolean isInt = false;
    public boolean isBoolean = false;
    public boolean isParam = false;
    public boolean isPublic = false;
    public boolean isArray = false;
    public boolean isEventGenerator = false;
    public boolean isEnumMember = false;
    public String className = "";
    public String dataType = "";
    public String packageName = "";

    public String getVbName() {
        return m_vbName;
    }

    public String getJavaName() {
        return m_javaName;
    }

    public void setVbName(String name) {
        int i = name.indexOf("(");
        if (i > 1) {
            isArray = true;
            m_vbName = name.substring(0, i);
        }
        else {
            isArray = false;
            m_vbName = name;
        }
    }

    public void setJavaName(String name) {
        int i = name.indexOf("(");
        if (i > 1) {
            m_javaName = name.substring(0, i);
        }
        else {
            m_javaName = name;
        }
    }

    public void setType(String dataType) {
        this.dataType = dataType;
        if (dataType.equals("String"))
            isString = true;
        else if (dataType.equals("long"))
            isLong = true;
        else if (dataType.equals("int"))
            isInt = true;
        else if (dataType.equals("boolean"))
            isBoolean = true;
    }
}
