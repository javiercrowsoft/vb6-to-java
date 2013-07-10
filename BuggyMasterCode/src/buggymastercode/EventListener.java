/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

/**
 *
 * @author jalvarez
 */
public class EventListener {

    static private final String newline = "\n";

    private String m_generatorVb = "";
    private String m_generatorJava = "";
    private String m_adapter = "";
    private String m_eventMacro = "";
    private StringBuilder m_sourceCode = new StringBuilder();

    public String getGeneratorVb() {
        return m_generatorVb;
    }

    public void setGeneratorVb(String value) {
        m_generatorVb = value;
    }

    public String getGeneratorJava() {
        return m_generatorJava;
    }

    public void setGeneratorJava(String value) {
        m_generatorJava = value;
    }

    public void setAdapter(String value) {
        m_adapter = value;
    }

    public String getEventMacro() {
        return m_eventMacro;
    }

    public void setEventMacro(String value) {
        m_eventMacro = value;
    }

    public StringBuilder getSourceCode() {
        return m_sourceCode;
    }

    public String getAnonymousInnerClass() {
        return "new " + m_adapter + "() {"
                + newline
                + m_sourceCode.toString()
                + newline
                + "              };";
    }
}
