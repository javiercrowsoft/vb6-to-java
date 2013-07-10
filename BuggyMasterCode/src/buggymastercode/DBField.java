/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

/**
 *
 * @author jalvarez
 */
public class DBField {
    private String m_name;
    private Object m_value;
    private int m_type;

    private DBField() {
    }

    public DBField(String name, int type, Object value) {
        m_name = name;
        m_type = type;
        m_value = value;
    }

    public String getName() {
        return m_name;
    }

    public int getType() {
        return m_type;
    }

    public Object getValue() {
        return m_value;
    }

    public void setValue(Object value) {
        m_value = value;
    }
}
