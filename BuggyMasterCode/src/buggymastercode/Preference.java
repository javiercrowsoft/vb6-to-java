/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

/**
 *
 * @author jalvarez
 */
public class Preference {
    private String m_id = "";
    private String m_value = "";

    public String getId() {
        return m_id;
    }

    public void setId(String value) {
        m_id = value;
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(String value) {
        m_value = value;
    }
}
