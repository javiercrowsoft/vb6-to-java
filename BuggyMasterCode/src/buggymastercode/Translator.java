/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author jalvarez
 */
public class Translator {

    static private final String newline = "\n";
    static private final String C_TAB = "    ";
    static private final String outerTabHandler = "                  ";
    static private final String innerTabHandler = outerTabHandler + "  ";
    static private final String C_NUMBERS = "-+0123456789";
    static private final String C_SEPARARTORS = "_._=_&&_||_+_-_*_/_==_!=_<_>_<=_>=_";
    static private final String C_SYMBOLS = " +-()*/,";
    static private final String C_RESERVED_WORDS =
        "_and_as_byval_byref_case_class_dim_elseif_else_end_each_for_friend_"
     + "_function_global_goto_if_in_is_next_not_of_or_on error_on resume_print_"
     + "_private_public_raise_select_sub_type_while_wend_char_date_double_integer_"
     + "_long_object_short_string_variant_#if_#end_exit_redim_on_me_";

    static private final String C_INTERFACE_POSTIFX = "EventI";
    static private final String C_ADAPTER_POSTIFX = "EventA";

    static private final String C_NUMERIC_DATA_TYPES = "||int||integer||double||single||currency||short||long||";

    private boolean m_isVbSource = false;
    private boolean m_codeHasStarted = false;
    private boolean m_attributeBlockHasStarted = false;
    private boolean m_inFunction = false;
    private boolean m_inEnum = false;
    private boolean m_inWith = false;
    private boolean m_inType = false;
    private boolean m_withDeclaration = false;
    private boolean m_endWithDeclaration = false;
    private boolean m_emptyLine = false;
    private String m_returnValue = ""; // default value for function return

    private String[] m_iterators = {"","_i","_j","_k","_t","_w","_z"};
    private int m_iteratorIndex = 0;
    private String[] m_imports = null;
    private int m_importCount = 0;
    
    private boolean m_translateToJava = true;

    // member variables of the class which we are translating
    //
    private ArrayList<Variable> m_memberVariables = new ArrayList<Variable>();
    // parameters and local variables of the function which we are translating
    //
    private ArrayList<Variable> m_functionVariables = new ArrayList<Variable>();
    // public functions, subs and properties of the class which we are 
    // translating
    //
    private ArrayList<Function> m_publicFunctions = null;
    // private functions, subs and properties of the class which we are
    // translating
    //
    private ArrayList<Function> m_privateFunctions = null;
    // this is used to build the dictionary of public variables of every
    // class in this project. this collection is used to found identifiers
    // in the code which references to public member of objects of other
    // classes.
    // public variables are accessed by the dot operator and assigned using the
    // equal sign (=) eg: "m_objmember.publicVariable = 1;" by the other
    // hand public properties are translated as setters and getters and the
    // assignment doesn't use the equals sign but the setter method.
    //
    private ArrayList<Variable> m_publicVariables = null;
    // files (frm, bas, cls) in this vbp
    //
    private ArrayList<SourceFile> m_collFiles = new ArrayList<SourceFile>();
    private ArrayList<Variable> m_collWiths = new ArrayList<Variable>();
    private ArrayList<Type> m_types = new ArrayList<Type>();
    // classes in java (String, Date, etc.)
    //
    private ArrayList<SourceFile> m_collJavaClassess = new ArrayList<SourceFile>();

    // the current type which we are translaing
    //
    private String m_type = "";
    // the collection of every type public and private declared
    // in the class which we are translating
    //
    private ArrayList<String> m_collTypes = new ArrayList<String>();
    // the current enum which we are translating
    //
    private String m_enum = "";
    // the collection of every enum public and private declared
    // in the class which we are translating
    //
    private ArrayList<String> m_collEnums = new ArrayList<String>();

    // member variables which can raise events
    //
    private ArrayList<EventListener> m_eventListeners = new ArrayList<EventListener>();
    // the resulting interface declaration of add every public event declaration
    // in the class which we are translating
    //
    private String m_listenerInterface = "";
    // the resulting class declaration of add every public event declaration
    // in the class which we are translating with a null implementation
    // of every method
    //
    private String m_adapterClass = "";
    // this flag tell us if we need add a collection variable to
    // hold the listeners and two methods to add and remove objets
    // in the collection
    //
    private boolean m_raiseEvents = false;
    // this collection is filled when we parse the class and
    // used by translate function declaration to determine if
    // the function has to be syncrhonized
    //
    private ArrayList<String> m_raiseEventFunctions = new ArrayList<String>();

    private boolean m_wasSingleLineIf = false;
    private String m_strBuffer = "";
    private int m_tabCount = 0;
    // the vb name of the function we are parsing or translating
    //
    private String m_vbFunctionName = "";
    private String m_vbClassName = "";
    private String m_javaClassName = "";
    private boolean m_isFirstCase = false;
    private boolean m_previousWasReturn = false;
    // flag to add auxiliary function to support vb date expecific
    // functionality
    //
    private boolean m_addDateAuxFunction = false;
    private boolean m_addDateAuxFunctionToG = false;
    // flag to add auxiliary function to support vb CDate function
    //
    private boolean m_addParseDateAuxFunction = false;
    private boolean m_addParseDateAuxFunctionToG = false;
    // flag to add auxiliary function to support vb IsNumeric function
    //
    private boolean m_addIsNumericAuxFunction = false;
    private boolean m_addIsNumericAuxFunctionToG = false;
    // flag to add auxiliary function to support vb redim and redim preserve
    //
    private boolean m_addRedimAuxFunction = false;
    private boolean m_addRedimAuxFunctionToG = false;
    private boolean m_addRedimPreserveAuxFunction = false;
    private boolean m_addRedimPreserveAuxFunctionToG = false;
    //
    private String m_packageName = "";
    // packages refence by this visual basic project in the order it appears in
    // vbp file
    //
    private String[] m_references = null;

    private ClassObject m_classObject;
    private FunctionObject m_functionObject;
    private VariableObject m_variableObject;

    private TranslatorWorker m_caller = null;

    private ClassObject m_typeClassObject;
    private ClassObject m_enumClassObject;

    private boolean m_AddAuxFunctionsToClass = false;
    private boolean m_UseGAuxFunctions = false;
    private boolean m_UseCSUtils = false;

    // used to define if the function need a variable rtn to
    // hold the return value
    //
    private boolean m_setReturnValueFound = false;
    private boolean m_needReturnVariable = false;
    private Function m_function = null;

    private boolean m_isBasFile = false;

    // to translate On Error
    //
    private boolean m_onErrorFound = false;
    private boolean m_onErrorResumeNext = false;
    private String m_onErrorLabel = "";
    private boolean m_onCatchBlock = false;

    public Translator() {
        
        m_collJavaClassess = new ArrayList<SourceFile>();
        SourceFile source = null;
        Function fun = null;

        // String
        //
        source = new SourceFile();
        source.setJavaName("String");
        source.setPublicFunctions(new ArrayList<Function>());

            // substring
            //
            fun = new Function();
            if (m_translateToJava)
                fun.getReturnType().setJavaName("substring");
            else
                fun.getReturnType().setJavaName("Substring");
            fun.getReturnType().setType("String");
            source.getPublicFunctions().add(fun);

            // toLowerCase
            //
            fun = new Function();
            if (m_translateToJava)
                fun.getReturnType().setJavaName("toLowerCase");
            else
                fun.getReturnType().setJavaName("ToLower");
            fun.getReturnType().setType("String");
            source.getPublicFunctions().add(fun);

            // toUpperCase
            //
            fun = new Function();
            if (m_translateToJava)
                fun.getReturnType().setJavaName("toUpperCase");
            else
                fun.getReturnType().setJavaName("ToUpper");
            fun.getReturnType().setType("String");
            source.getPublicFunctions().add(fun);

            // trim
            //
            fun = new Function();
            if (m_translateToJava)
                fun.getReturnType().setJavaName("trim");
            else
                fun.getReturnType().setJavaName("Trim");
            fun.getReturnType().setType("String");
            source.getPublicFunctions().add(fun);
        
        m_collJavaClassess.add(source);

        Preference pref = PreferenceObject.getPreference(G.C_AUX_FUN_ID);
        if (pref != null) {
            if (pref.getValue().equals(G.C_AUX_FUN_IN_CLASS_SOURCE))
                m_AddAuxFunctionsToClass = true;
            else if (pref.getValue().equals(G.C_AUX_FUN_IN_G_CLASS))
                m_UseGAuxFunctions = true;
            else if (pref.getValue().equals(G.C_AUX_FUN_IN_CS_LIBRARY))
                m_UseCSUtils = true;
        }
    }
    
    public void setCaller(TranslatorWorker caller) {
        m_caller = caller;
    }

    public void setPackage(String packageName) {
        m_packageName = packageName;
    }

    public void setReferences(String[] references) {
        m_references = references;
    }

    public void setSourceFiles(ArrayList<SourceFile> sourceFiles) {
        m_collFiles = sourceFiles;
    }
    
    public void setTranslateToJava(boolean value) {
        m_translateToJava = value;
    }

    public boolean isVbSource() {
        return m_isVbSource;
    }

    public String getVbClassName() {
        return m_vbClassName;
    }

    public String getJavaClassName() {
        return m_javaClassName;
    }

    public ArrayList<Function> getPublicFunctions() {
        return m_publicFunctions;
    }

    public ArrayList<Function> getPrivateFunctions() {
        return m_privateFunctions;
    }

    public ArrayList<Variable> getPublicVariables() {
        return m_publicVariables;
    }

    public boolean deletePackage(String packageName) {
        String sqlstmt = "delete from tvariable where cl_id in "
                            + "(select cl_id from tclass where cl_packagename = "
                            + Db.getString(packageName) + ")";
        if (Db.db.execute(sqlstmt)) {

            sqlstmt = "delete from tfunction where cl_id in "
                                + "(select cl_id from tclass where cl_packagename = "
                                + Db.getString(packageName) + ")";
            if (Db.db.execute(sqlstmt)) {

                sqlstmt = "delete from tclass where cl_packagename = "
                            + Db.getString(packageName);
                if (Db.db.execute(sqlstmt)) {
                    return true;
                }
                else {
                    return false;
                }
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    public ArrayList<String> getRaiseEventFunctions() {
        return m_raiseEventFunctions;
    }

    public void setRaiseEventFunctions(ArrayList<String> functions) {
        m_raiseEventFunctions = functions;
    }

    public void parse(String strLine) {
        if (m_isVbSource) {
            if (m_codeHasStarted) {
                parseLine(strLine);
            }
            else {
                if (strLine.contains("Attribute VB_Name = \"")) {
                    m_attributeBlockHasStarted = true;
                    m_vbClassName = strLine.substring(21, strLine.length()-1);
                    m_javaClassName = m_vbClassName;
                    
                    // debug
                    /*
                    if (m_vbClassName.equalsIgnoreCase("mGlobal")) {
                        int i = 9999;
                    }
                     /* 
                     */
                    // debug
                }
                else {
                    if (m_attributeBlockHasStarted) {
                        if (strLine.length() < 9) {
                            m_codeHasStarted = true;
                            parseLine(strLine);
                        }
                        else {
                            if (!strLine.substring(0,9).equals("Attribute")) {
                                m_codeHasStarted = true;
                                parseLine(strLine);
                            }
                        }
                    }
                    else {
                        if (strLine.length() >= 9) {
                            if (strLine.substring(0,9).equals("Attribute")) {
                                m_attributeBlockHasStarted = true;
                            }
                        }
                    }
                }
            }
        }
    }

    public String translate(String strLine) {
        String rtn = "";
        if (m_isVbSource) {
            if (m_codeHasStarted) {
                rtn = translateLine(strLine);
            }
            else {
                if (strLine.contains("Attribute VB_Name = \"")) {
                    m_attributeBlockHasStarted = true;
                    String className = strLine.substring(21, strLine.length()-1);
                    m_vbClassName = className;
                    m_javaClassName = m_vbClassName;
                    m_classObject.setPackageName(m_packageName);
                    m_classObject.setVbName(m_vbClassName);
                    m_classObject.setJavaName(m_javaClassName);
                    m_classObject.getClassIdFromClassName();
                    m_classObject.saveClass();
                    m_tabCount++;
                    rtn = "public class " + className + " {" + newline + newline;
                }
                else {
                    if (m_attributeBlockHasStarted) {
                        if (strLine.length() < 9) {
                            m_codeHasStarted = true;
                            rtn = translateLine(strLine);
                        }
                        else {
                            if (!strLine.substring(0,9).equals("Attribute")) {
                                m_codeHasStarted = true;
                                rtn = translateLine(strLine);
                            }
                            else
                                rtn = "";
                        }
                    }
                    else {
                        if (strLine.length() < 9) {
                            rtn = "";
                        }
                        else {
                            if (strLine.substring(0,9).equals("Attribute")) {
                                m_attributeBlockHasStarted = true;
                            }
                            rtn = "";
                        }
                    }
                }
            }
        }
        if (rtn.contains("return ")) {
            m_previousWasReturn = true;
        }
        else if (rtn.contains("return;")) {
            m_previousWasReturn = true;
        }
        else if (!rtn.trim().isEmpty()) {
            m_previousWasReturn = false;
        }
        if (m_emptyLine) {
            m_emptyLine = false;
            rtn = "";
        }
        return rtn;
    }

    public String getImportSection() {
        String rtn = "";

        if (m_addDateAuxFunction || m_addParseDateAuxFunction) {
            addToImportList("import java.text.DateFormat;");
            addToImportList("import java.text.ParseException;");
            addToImportList("import java.text.SimpleDateFormat;");
            addToImportList("import java.text.Date;");
        }

        if (m_addIsNumericAuxFunction) {
            addToImportList("import java.text.ParseException;");
        }

        for (int i = 0; i < m_importCount; i++) {
            rtn += m_imports[i] + newline;
        }

        if (!rtn.isEmpty())
            rtn = newline + rtn + newline;

        return rtn;
    }

    private void addToImportList(String reference) {
        for (int i = 0; i < m_importCount; i++) {
            if (m_imports[i].equals(reference)) {
                return;
            }
        }
        m_importCount++;
        m_imports[m_importCount-1] = reference;
    }

    public String getEventListenerCollection() {
        if (m_raiseEvents) {
            String className = m_javaClassName + C_INTERFACE_POSTIFX;
            String rtn = newline + "    // event listener collection"
                            + newline + "    //" + newline
                            + "    private ArrayList<"
                            + className
                            + "> m_listeners = new ArrayList<"
                            + className
                            + ">();" + newline + newline
                            + "    public synchronized void addListener("
                            + className
                            + " l) {" + newline
                            + "        m_listeners.add(l);" + newline
                            + "    }" + newline + newline
                            + "    public synchronized void removeListener("
                            + className
                            + " l) {" + newline
                            + "        m_listeners.remove(l);" + newline
                            + "    }" + newline;
            return rtn;
        }
        else
            return "";
    }

    public String getAuxFunctions() {
        String rtn = "";

        if (m_addDateAuxFunction) {
            rtn += newline + getDateAuxFunction();
        }

        if (m_addParseDateAuxFunction) {
            rtn += newline + getParseDateAuxFunction();
        }

        if (m_addIsNumericAuxFunction) {
            rtn += newline + getIsNumericAuxFunction();
        }

        if (m_addRedimAuxFunction) {
            rtn += newline + getRedimAuxFunction();
        }

        if (m_addRedimPreserveAuxFunction) {
            rtn += newline + getRedimPreserveAuxFunction();
        }

        return rtn;
    }

    public String getGImportSection() {
        String rtn = "";
        m_importCount = 0;

        if (m_addDateAuxFunctionToG || m_addParseDateAuxFunctionToG) {
            addToImportList("import java.text.DateFormat;");
            addToImportList("import java.text.ParseException;");
            addToImportList("import java.text.SimpleDateFormat;");
            addToImportList("import java.text.Date;");
        }

        if (m_addIsNumericAuxFunctionToG) {
            addToImportList("import java.text.ParseException;");
        }

        for (int i = 0; i < m_importCount; i++) {
            rtn += m_imports[i] + newline;
        }

        if (!rtn.isEmpty())
            rtn = newline + rtn + newline;

        return rtn;
    }

    public String getGClass() {
        if (m_UseGAuxFunctions) {

            String rtn = "// Class G : Auxiliary functions" 
                            + newline + "//" + newline + newline
                            + getGImportSection()
                            + "public class G {" + newline;

            if (m_addDateAuxFunctionToG) {
                rtn += newline + getDateAuxFunction();
            }

            if (m_addParseDateAuxFunctionToG) {
                rtn += newline + getParseDateAuxFunction();
            }

            if (m_addIsNumericAuxFunctionToG) {
                rtn += newline + getIsNumericAuxFunction();
            }

            if (m_addRedimAuxFunctionToG) {
                rtn += newline + getRedimAuxFunction();
            }

            if (m_addRedimPreserveAuxFunctionToG) {
                rtn += newline + getRedimPreserveAuxFunction();
            }

            rtn += newline + "}";

            return rtn;

        }
        else
            return "";

    }

    private String getDateAuxFunction() {
        return
                    "    private static Date getDateFromString(String date) {" + newline +
                    "        DateFormat df = new SimpleDateFormat(\"MM/dd/yyyy\");" + newline +
                    "        date = date.replace(\"#\",\"\");" + newline +
                    "        Date dateValue = null;" + newline +
                    "        try {" + newline +
                    "            dateValue = df.parse(date);" + newline +
                    "        } catch (ParseException ex) {/* it can not be possible*/}" + newline +
                    "        return dateValue;" + newline +
                    "    }" + newline;

    }

    private String getParseDateAuxFunction() {
        return
                    "    private static Date parseDate(String date) throws ParseException {" + newline +
                    "        DateFormat df = new SimpleDateFormat(\"MM/dd/yyyy\");" + newline +
                    "        return df.parse(date);" + newline +
                    "    }" + newline;
    }

    private String getIsNumericAuxFunction() {
        return
                    "    private static boolean isNumeric(String number) {" + newline +
                    "        try {" + newline +
                    "            Double.parseDouble(number);" + newline +
                    "            return true;" + newline +
                    "        } " + newline +
                    "        catch (ParseException ex) {" + newline +
                    "            return false;" + newline +
                    "        }" + newline +
                    "    }" + newline;
    }

    private String getRedimAuxFunction() {
        return getRedimAuxFunctionForType("String");
    }

    private String getRedimAuxFunctionForType(String type) {
        return
                    "    public static " + type + "[] redim(" + type + "[] source, int size) {" + newline +
                    "        if (size == 0) {" + newline +
                    "            return null;" + newline +
                    "        }" + newline +
                    "        else {" + newline +
                    "            return new " + type + "[size];" + newline +
                    "        }" + newline +
                    "    }" + newline;
    }

    private String getRedimPreserveAuxFunction() {
        return getRedimPreserveAuxFunctionForType("String");
    }

    private String getRedimPreserveAuxFunctionForType(String type) {
        return
                    "    public static " + type + "[] redimPreserve(" + type + "[] source, int size) {" + newline +
                    "        if (size == 0) {" + newline +
                    "            return null;" + newline +
                    "        }" + newline +
                    "        else {" + newline +
                    "            " + type + "[] tmp = new " + type + "[size];" + newline +
                    "            if (source != null) {" + newline +
                    "                for (int i = 0; i < Math.min(source.length, tmp.length); i++) {" + newline +
                    "                    tmp[i] = source[i];" + newline +
                    "                }" + newline +
                    "            }" + newline +
                    "            return tmp;" + newline +
                    "        }" + newline +
                    "    }" + newline;
    }

    private void parseLine(String strLine) {
        // two kind of sentences
            // In function
            // Declarations

        // functions
            // Function
            // Sub
            // Property

        // if the sentence is split in two or more lines
        // we need to join the lines before translate it
        //
        if (isSentenceComplete(strLine)) {

            strLine = m_strBuffer + G.ltrimTab(strLine);
            m_strBuffer = "";

            strLine = removeLineNumbers(strLine);

            if (isEmptyLine(strLine)) {
                return;
            }
            if (isVbSpecificCode(strLine)) {
                return;
            }
            if (isComment(strLine)) {
                return;
            }
            if (isDeclareApi(strLine)) {
                return;
            }
            else if (isBeginOfType(strLine)) {
                m_inType = true;
                return;
            }
            else if (isEndOfType(strLine)) {
                return;
            }
            if (m_inType) {
                return;
            }
            else if (isBeginOfEnum(strLine)) {
                m_inEnum = true;
                return;
            }
            else if (isEndOfEnum(strLine)) {
                return;
            }
            else if (m_inEnum) {
                return;
            }
            else if (isEndFunction(strLine)) {
                setNeedReturnValue();
                m_function = null;
                m_inFunction = false;
                return;
            }
            else if (m_inFunction) {
                checkRaiseEvent(strLine);
                checkNeedReturnVariable(strLine);
                return;
            }
            else {
                // first check for Function | Sub | Property
                if (isBeginOfFunction(strLine)) {
                    parseFunctionDeclaration(strLine);
                    return;
                }
                // declarations
                else {
                    parsePublicMember(strLine);
                    return;
                }
            }
        }
        // split sentences
        else {
            m_strBuffer += G.rtrim(strLine.substring(0, strLine.length()-1)) + " " ;
            return;
        }
    }

    private String translateLine(String strLine) {
        // two kind of sentences
            // In function
            // Declarations

        // functions
            // Function
            // Sub
            // Property

        // if the sentence is split in two or more lines
        // we need to join the lines before translate it
        //
        if (isSentenceComplete(strLine)) {

            strLine = m_strBuffer + G.ltrimTab(strLine);
            m_strBuffer = "";

            strLine = removeLineNumbers(strLine);

            if (isEmptyLine(strLine)) {
                return strLine + newline;
            }
            if (isVbSpecificCode(strLine)) {
                return "//" + strLine + newline;
            }
            if (isComment(strLine)) {
                return getTabs() + commentLine(strLine);
            }
            if (isDeclareApi(strLine)) {
                return declareApiLine(strLine);
            }
            if (m_inType) {
                addToType(strLine);
                return "";
            }
            else if (isBeginOfType(strLine)) {
                addToType(strLine);
                return "//*TODO:** type is translated as a new class at the end of the file " + strLine + newline;
            }
            else if (isBeginOfEnum(strLine)) {
                addToEnum(strLine);
                return "//*TODO:** enum is translated as a new class at the end of the file " + strLine + newline;
            }
            else if (m_inEnum) {
                addToEnum(strLine);
                return "";
            }
            else if (m_inFunction) {
                checkEndBlock(strLine);
                String rtn = getTabs() + translateLineInFunction(strLine);
                checkBeginBlock(strLine);
                return rtn;
            }
            else {
                // first check for Function | Sub | Property
                if (isBeginOfFunction(strLine)) {
                    checkEndBlock(strLine);
                    String rtn = getTabs() + translateLineInFunction(strLine);
                    checkBeginBlock(strLine);
                    return rtn;
                }
                // declarations
                else {
                    checkEndBlock(strLine);
                    String rtn = getTabs() + translateLineInDeclaration(strLine);
                    checkBeginBlock(strLine);
                    return rtn;
                }
            }
        }
        // split sentences
        else {
            m_strBuffer += strLine.substring(0, strLine.length()-1).trim() + " ";
            return "";
        }
    }

    private boolean isSentenceComplete(String strLine) {
        strLine = G.ltrimTab(strLine);
        if (strLine.isEmpty())
            return true;
        else if (getStartComment(strLine) >= 0)
            return true;
        else if (strLine.length() < 2)
            return true;
        else
            return !(strLine.substring(strLine.length()-2).equals(" _"));
    }

    private int getStartComment(String strLine) {
        boolean literalFlag = false;
        for (int i = 0; i < strLine.length(); i++) {
            if (strLine.charAt(i) == '"') {
                literalFlag = !literalFlag;
            }
            else if (strLine.charAt(i) == '\'') {
                if (!literalFlag) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean isEmptyLine(String strLine) {
        strLine = G.ltrimTab(strLine);
        return strLine.isEmpty();
    }

    private boolean isVbSpecificCode(String strLine) {
        strLine = G.ltrimTab(strLine).toLowerCase();
        if (strLine.isEmpty())
            return false;
        if (strLine.equals("option explicit"))
            return true;
        else
            return false;
    }

    private boolean isComment(String strLine) {
        strLine = G.ltrimTab(strLine);
        if (strLine.isEmpty())
            return false;
        else
            return strLine.charAt(0) == '\'';
    }

    private String commentLine(String strLine) {
        return G.ltrimTab(strLine.replaceFirst("'", "//")) + newline;
    }

    private boolean isDeclareApi(String strLine) {
        strLine = G.ltrimTab(strLine);
        if (strLine.isEmpty()) {
            return false;
        }
        else {
            if (strLine.length() >= 15) {
                if (strLine.substring(0, 15).equalsIgnoreCase("public declare ")) {
                    return true;
                }
                else if (strLine.length() >= 16) {
                    return strLine.substring(0, 16).equalsIgnoreCase("private declare ");
                }
                else {
                    return false;
                }
            }
            else {
                return false;
            }
        }
    }

    private String declareApiLine(String strLine) {
        return "*TODO: API " + strLine + newline;
    }

    private boolean isBeginOfEnum(String strLine) {
        strLine = G.ltrim(strLine);
        strLine = removeExtraSpaces(strLine);
        if (strLine.length() > 5) {
            if (strLine.substring(0,5).toLowerCase().equals("enum ")) {
                m_inEnum = true;
                return true;
            }
        }
        if (strLine.length() > 12) {
            if (strLine.substring(0,12).toLowerCase().equals("public enum ")) {
                m_inEnum = true;
                return true;
            }
        }
        if (strLine.length() > 13) {
            if (strLine.substring(0,13).toLowerCase().equals("private enum ")) {
                m_inEnum = true;
                return true;
            }
        }
        return false;
    }

    private boolean isEndOfEnum(String strLine) {
        strLine = G.ltrim(strLine);

        if (strLine.trim().length() == 8) {
            if (strLine.substring(0,8).toLowerCase().equals("end enum")) {
                m_inEnum = false;
                return true;
            }
        }
        return false;
    }

    private boolean isBeginOfType(String strLine) {
        strLine = G.ltrim(strLine);
        strLine = removeExtraSpaces(strLine);
        if (strLine.length() > 5) {
            if (strLine.substring(0,5).toLowerCase().equals("type ")) {
                m_inType = true;
                return true;
            }
        }
        if (strLine.length() > 12) {
            if (strLine.substring(0,12).toLowerCase().equals("public type ")) {
                m_inType = true;
                return true;
            }
        }
        if (strLine.length() > 13) {
            if (strLine.substring(0,13).toLowerCase().equals("private type ")) {
                m_inType = true;
                return true;
            }
        }
        return false;
    }

    private boolean isEndOfType(String strLine) {
        strLine = G.ltrim(strLine);

        if (strLine.trim().length() == 8) {
            if (strLine.substring(0,8).toLowerCase().equals("end type")) {
                m_inType = false;
                return true;
            }
        }
        return false;
    }

    private boolean isBeginOfFunction(String strLine) {
        // functions
            // Function
            // Sub
            // Property
        strLine = strLine.toLowerCase();
        strLine = strLine.replaceAll("public", "");
        strLine = strLine.replaceAll("private", "");
        strLine = strLine.replaceAll("friend", "");
        strLine = G.ltrimTab(strLine);
        if (strLine.length() > 10) {
            if (strLine.substring(0,9).toLowerCase().equals("function ")) {
                m_inFunction = true;
                return true;
            }
        }
        if (strLine.length() > 5) {
            if (strLine.substring(0,4).toLowerCase().equals("sub ")) {
                m_inFunction = true;
                return true;
            }
        }
        if (strLine.length() > 10) {
            if (strLine.substring(0,9).toLowerCase().equals("property ")) {
                m_inFunction = true;
                return true;
            }
        }
        return false;
    }

    // posible lines
        // declarations
        // block: If , While, For, Do, Case
        // asignment
        // calls
        // others: comments, blank, visual basic expecific liek #If

    private String translateLineInDeclaration(String strLine) {
        // declaration expecific stuff
        //
        return translateCode(strLine, true);
    }
    private String translateLineInFunction(String strLine) {
        // function expecific stuff
        //
        strLine = translateCode(strLine, false);
        return strLine;
    }

    private String translateWithSentence(String strLine) {
        // translate inner with block
        //
        if (m_inWith) {
            if (!m_withDeclaration && !m_endWithDeclaration) {
                boolean evalWith = false;
                if (strLine.startsWith("."))
                    evalWith = true;
                else if (strLine.contains(" ."))
                    evalWith = true;
                else if (strLine.contains("(."))
                    evalWith = true;
                else if (strLine.contains("\t."))
                    evalWith = true;
                else if (strLine.contains("!."))
                    evalWith = true;
                if (evalWith) {
                    String withVariable = m_collWiths.get(m_collWiths.size()-1).getJavaName();
                    String workLine = "";
                    boolean literalFlag = false;
                    for (int i = 0; i < strLine.length(); i++) {
                        if (strLine.charAt(i) == '"') {
                            literalFlag = !literalFlag;
                        }
                        else if (!literalFlag) {
                            if (strLine.charAt(i) == '.') {
                                if (i > 0) {
                                    if (strLine.charAt(i - 1) == ' ') {
                                        workLine += withVariable;
                                    }
                                    else if (strLine.charAt(i - 1) == '(') {
                                        workLine += withVariable;
                                    }
                                    else if (strLine.charAt(i - 1) == '\t') {
                                        workLine += withVariable;
                                    }
                                    else if (strLine.charAt(i - 1) == '!') {
                                        workLine += withVariable;
                                    }
                                }
                                else {
                                    workLine += withVariable;
                                }
                            }
                        }
                        workLine += strLine.charAt(i);
                    }
                    strLine = workLine;
                }
            }
        }
        return strLine;
    }

    private String translateCode(String strLine, boolean inDeclaration) {
        
        // debug
        //  
        
        if (strLine.contains("pIsSeparator(")) {
            int i = 9999;
        }
         /* 
         */
        // debug
        
        // first we extract comments
        // so the code only works over executable code
        //
        int startComment = getStartComment(strLine);
        String workLine = strLine;
        String comments = "";
        if (startComment >= 0) {
            comments =  "//" + workLine.substring(startComment);
            workLine = workLine.substring(0, startComment-1);
        }

        String rtn = translateCodeAux(workLine, inDeclaration);
        rtn = translateDateConstant(rtn);
        rtn = translateUbound(rtn);
        rtn = translateIsNull(rtn);

        if (!comments.isEmpty())
            rtn = comments + newline + getTabs() + rtn;

        //rtn = translateComments(rtn);
        return rtn;
    }

    private String translateCodeAux(String strLine, boolean inDeclaration) {

        // get out spaces even tabs
        //
        String workLine = G.ltrimTab(strLine).toLowerCase();
        // dim
        if (workLine.length() > 4) {
            if (workLine.substring(0,4).equals("dim ")) {
                return translateDim(strLine);
            }
        }
        // in declaration
            // private and public can be modifiers of member variables
            // or events
            //
        if (inDeclaration) {
            if (workLine.length() > 8) {
                if (workLine.substring(0,8).equals("private ")) {
                    if (workLine.contains(" const ")) {
                        return translatePrivateConstMember(strLine);                        
                    }
                    else {
                        return translatePrivateMember(strLine);
                    }
                }
            }
            if (workLine.length() > 7) {
                if (workLine.substring(0,7).equals("public ")) {
                    if (workLine.contains(" const ")) {
                        return translatePublicConstMember(strLine);
                    }
                    else if (workLine.contains(" event ")) {
                        return translateEventDeclaration(strLine);
                    }
                    else {
                        return translatePublicMember(strLine);
                    }
                }
            }
        }
        // in function
            // private and public only can be modifier of functions
            //
        else {
            // a function declaration is like this
                // Public Function ShowPrintDialog(ByVal
            if (isFunctionDeclaration(workLine)) {
                strLine = translateFunctionDeclaration(strLine);
                checkEventHandler(strLine);
                strLine = translateFunctionReturnVariable(strLine);
                return strLine;
            }
            else {
                if (isEndFunction(workLine)) {
                    String onErrorLabelNotFound = checkOnErrorLabelFound();
                    String endOfPreviousOnError = getEndOfPreviousOnError(C_TAB);
                    strLine = onErrorLabelNotFound
                                + endOfPreviousOnError
                                + getReturnLine()
                                + "}" + newline;
                    m_function = null;
                    m_inFunction = false;
                    return strLine;
                }
                // function's body
                //
                else {
                    // types of sentences
                        // conditional block
                            // if, select case, elseif, else
                    if (isIfSentence(workLine))
                        return translateIfSentence(strLine);
                    if (isElseIfSentence(workLine))
                        return translateElseIfSentence(strLine);
                    else if (isElseSentence(workLine))
                        return translateElseSentence(strLine);
                    else if (isEndIfSentence(workLine))
                        return translateEndIfSentence(strLine);
                    else if (isSelectCaseSentence(workLine))
                        return translateSelectCaseSentence(strLine);
                    else if (isCaseSentence(workLine))
                        return translateCaseSentence(strLine);
                    else if (isEndSelectSentence(workLine))
                        return translateEndSelectSentence(strLine);
                    else if (isExitFunctionSentence(workLine))
                        return translateExitFunctionSentence(strLine);
                    else if (isDoWhileSentence(workLine))
                        return translateDoWhileSentence(strLine);
                    else if (isDoSentence(workLine))
                        return translateDoSentence(strLine);
                    else if (isWhileSentence(workLine))
                        return translateWhileSentence(strLine);
                    else if (isLoopUntilSentence(workLine))
                        return translateLoopUntilWhileSentence(strLine, true);
                    else if (isLoopWhileSentence(workLine))
                        return translateLoopUntilWhileSentence(strLine, false);
                    else if (isWendSentence(workLine))
                        return translateWendSentence(strLine);
                    else if (isLoopSentence(workLine))
                        return translateLoopSentence(strLine);
                    else if (isForSentence(workLine))
                        return translateForSentence(strLine);
                    else if (isNextSentence(workLine))
                        return translateNextSentence(strLine);
                    else if (isOnErrorSentence(workLine))
                        return translateOnErrorSentence(strLine);
                    else if (isOnErrorLabelSentence(workLine))
                        return translateOnErrorLabelSentence(strLine);
                    else
                        return translateSentenceWithNewLine(strLine);
                        // loop block
                            // for, while, do, loop

                        // asignment sentence
                            // set, =

                        // call sentence
                }
            }
        }
        return "*" + strLine + newline;
    }

    private String translateDateConstant(String strLine) {
        String rtn = "";
        String[] words = G.split2(strLine);
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() >= 8) {
                if (words[i].charAt(0) == '#') {
                    if (words[i].charAt(words[i].length() - 1) == '#') {
                        if (m_AddAuxFunctionsToClass) {
                            m_addDateAuxFunction = true;
                            words[i] = "getDateFromString(\""
                                        + words[i].substring(1, words[i].length() - 1)
                                        + "\")";
                        }
                        // when preference are setting to use G class or CSUtils
                        // it is translated using G.{auxfunction}
                        //
                        else {
                            m_addDateAuxFunctionToG = m_UseGAuxFunctions;
                            words[i] = "G.getDateFromString(\""
                                        + words[i].substring(1, words[i].length() - 1)
                                        + "\")";
                        }
                    }
                }
            }
            rtn += words[i];
        }
        return rtn;
    }

    private String translateUbound(String strLine) {
        boolean openParentheses = false;
        boolean uboundFound = false;
        int iOpenParentheses = 0;
        String arrayExpression = "";
        String rtn = "";
        String[] words = G.split(strLine);
        for (int i = 0; i < words.length; i++) {
            if (uboundFound) {
                if (words[i].equals("(")) {
                    iOpenParentheses++;
                    if (iOpenParentheses > 1) {
                        arrayExpression += words[i];
                    }
                }
                else if (words[i].equals(")")) {
                    iOpenParentheses--;
                    if (iOpenParentheses == 0) {
                        if (arrayExpression.contains(" ")) {
                            if (m_translateToJava)
                                rtn += "(" + arrayExpression + ").length";
                            else
                                rtn += "(" + arrayExpression + ").Length";
                        }
                        else {
                            if (m_translateToJava)
                                rtn += arrayExpression + ".length";
                            else
                                rtn += arrayExpression + ".Length";
                        }
                        uboundFound = false;
                    }
                    else {
                        arrayExpression += words[i];
                    }

                }
                else if (!words[i].equalsIgnoreCase("Ubound")) {
                    arrayExpression += words[i];
                }
            }
            else {
                if (words[i].equals("(")) {
                    openParentheses = true;
                }
                else {
                    if (openParentheses) {
                        openParentheses = false;
                        words[i] = translateUbound(words[i]);
                    }
                    else {
                        if (words[i].length() == 6) {
                            if (words[i].equalsIgnoreCase("Ubound")) {
                                uboundFound = true;
                            }
                        }
                    }
                }
                if (!uboundFound) {
                    rtn += words[i];
                }
            }
        }
        return rtn;
    }

    private String translateIsNull(String strLine) {
        boolean openParentheses = false;
        boolean isNullFound = false;
        int iOpenParentheses = 0;
        String nullExpression = "";
        String rtn = "";
        String[] words = G.split(strLine);
        for (int i = 0; i < words.length; i++) {
            if (isNullFound) {
                if (words[i].equals("(")) {
                    iOpenParentheses++;
                    if (iOpenParentheses > 1) {
                        nullExpression += words[i];
                    }
                }
                else if (words[i].equals(")")) {
                    iOpenParentheses--;
                    if (iOpenParentheses == 0) {
                        if (nullExpression.contains(" ")) {
                            rtn += "(" + nullExpression + ") == null";
                        }
                        else {
                            rtn += nullExpression + " == null";
                        }
                        isNullFound = false;
                    }
                    else {
                        nullExpression += words[i];
                    }

                }
                else if (!words[i].equalsIgnoreCase("IsNull")) {
                    nullExpression += words[i];
                }
            }
            else {
                if (words[i].equals("(")) {
                    openParentheses = true;
                }
                else {
                    if (openParentheses) {
                        openParentheses = false;
                        words[i] = translateIsNull(words[i]);
                    }
                    else {
                        if (words[i].length() == 6) {
                            if (words[i].equalsIgnoreCase("IsNull")) {
                                isNullFound = true;
                            }
                        }
                    }
                }
                if (!isNullFound) {
                    rtn += words[i];
                }
            }
        }
        return rtn;
    }

    private void parseFunctionDeclaration(String strLine) {
        // first we reset this flag which is used to determine
        // if the function need a variable rtn to hold the return value
        //
        m_setReturnValueFound = false;
        m_needReturnVariable = false;

        // On Error flag is reset in every function
        //
        m_onErrorFound = false;
        m_onErrorResumeNext = false;
        m_onErrorLabel = "";
        m_onCatchBlock = false;

        // get out spaces even tabs
        //
        String workLine = G.ltrimTab(strLine).toLowerCase();
        // dim
        if (workLine.length() > 4) {
            if (workLine.substring(0,4).equals("dim ")) {
                return;
            }
        }
        // in function
            // private and public only can be modifier of functions
            //

        // a function declaration is like this
            // Public Function ShowPrintDialog(ByVal
        if (isFunctionDeclaration(workLine)) {
            String functionDeclaration = translateFunctionDeclaration(strLine);
            String[] words = G.splitSpace(functionDeclaration);
            if (words.length >= 3) {
                Function function = new Function();
                function.vbDeclaration = strLine;
                function.javaDeclaration = functionDeclaration;
                function.setJavaClassName(m_javaClassName);
                int k = 2;
                int t = 1;
                if (words[1].equals("static")) {
                    k = 3;
                    t = 2;
                }
                if (words[k].contains("(")) {
                    int i = words[k].indexOf("(");
                    function.getReturnType().setJavaName(words[k].substring(0, i));
                }
                else
                    function.getReturnType().setJavaName(words[k]);
                function.getReturnType().setVbName(m_vbFunctionName);
                function.getReturnType().setType(words[t]);
                m_function = function;
                if (words[0].equals("private")) {
                    m_privateFunctions.add(function);
                }
                else {
                    m_publicFunctions.add(function);
                }
            }
        }
    }

    private void parsePublicMember(String strLine) {
        
        // get out spaces even tabs
        //
        String workLine = G.ltrimTab(strLine).toLowerCase();
        // dim
        if (workLine.length() > 4) {
            if (workLine.substring(0,4).equals("dim ")) {
                return;
            }
        }
        // in declaration
            // private and public can be modifiers of member variables
            // or events
            //
        if (workLine.length() > 8) {
            if (workLine.substring(0,8).equals("private ")) {
                return;
            }
        }
        if (workLine.length() > 7) {
            if (workLine.substring(0,7).equals("public ")) {
                if (workLine.contains(" const ")) {
                    return;
                }
                else if (workLine.contains(" event ")) {
                    return;
                }
                else {
                    
                    // form is
                        // dim variable_name as data_type
                    strLine = strLine.trim();
                    String[] words = G.splitSpace(strLine);//strLine.split("\\s+");
                    String dataType = "";
                    String identifier = "";
                    String vbIdentifier = "";
                    boolean isEventGenerator = false;

                    if (words.length > 1) {
                        vbIdentifier = words[1];
                        // with events eg:
                        //      private withevents my_obj_with_events as CObjetWithEvents ' some comments
                        //      0       1           2                 3        4          >= 5
                        //
                        if (vbIdentifier.equalsIgnoreCase("WithEvents")) {
                            vbIdentifier = words[2];
                            identifier = getIdentifier(vbIdentifier);
                            if (words.length > 4) {
                                dataType = words[4];
                            }
                            isEventGenerator = true;
                        }
                        else {
                            identifier = getIdentifier(vbIdentifier);
                            if (words.length > 3) {
                                dataType = words[3];
                            }
                        }
                    }
                    if (dataType.isEmpty()) {
                        dataType = getObjectTypeName();
                    }
                    dataType = getDataType(dataType);

                    boolean isArray = identifier.endsWith("()");
                    if (isArray) {
                        identifier = identifier.substring(0, identifier.length() - 2);
                    }

                    Variable var = new Variable();
                    var.setVbName(vbIdentifier);
                    var.setJavaName(identifier);
                    var.className = m_javaClassName;
                    var.packageName = m_packageName;
                    var.setType(dataType);
                    var.isPublic = true;
                    var.isArray = isArray;
                    var.isEventGenerator = isEventGenerator;
                    m_publicVariables.add(var);
                }
            }
        }
    }

    private boolean isCaseSentence(String strLine) {
        if (G.beginLike(strLine, "Case ")) {
            return true;
        }
        else
            return false;
    }

    private boolean isSelectCaseSentence(String strLine) {
        if (G.beginLike(strLine, "Select Case ")) {
            return true;
        }
        else
            return false;
    }

    private boolean isIfSentence(String strLine) {
        if (G.beginLike(strLine, "If ")) {
            return true;
        }
        else
            return false;
    }

    private boolean isDoWhileSentence(String strLine) {
        if (G.beginLike(strLine, "Do While ")) {
            return true;
        }
        else
            return false;
    }

    private boolean isDoSentence(String strLine) {
        if (strLine.equalsIgnoreCase("Do") || G.beginLike(strLine, "Do ")) {
            return true;
        }
        else
            return false;
    }

    private boolean isWhileSentence(String strLine) {
        if (G.beginLike(strLine, "While ")) {
            return true;
        }
        else
            return false;
    }

    private boolean isForSentence(String strLine) {
        if (G.beginLike(strLine, "For ")) {
            return true;
        }
        else
            return false;
    }

    private boolean isElseIfSentence(String strLine) {
        if (G.beginLike(strLine, "ElseIf ")) {
            return true;
        }
        else
            return false;
    }

    private boolean isElseSentence(String strLine) {
        if (G.beginLike(strLine, "Else ")) {
            return true;
        }
        else {
            int startComment = getStartComment(strLine);
            if (startComment >= 0) {
                strLine = strLine.substring(0, startComment-1);
            }
            strLine = G.ltrimTab(strLine);
            if (strLine.equalsIgnoreCase("Else")) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    private boolean isEndSelectSentence(String strLine) {
        if (G.beginLike(strLine, "End Select ")) {
            return true;
        }
        else {
            int startComment = getStartComment(strLine);
            if (startComment >= 0) {
                strLine = strLine.substring(0, startComment-1);
            }
            strLine = G.ltrimTab(strLine);
            if (strLine.equalsIgnoreCase("End Select")) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    private boolean isExitFunctionSentence(String strLine) {
        if (G.beginLike(strLine, "Exit Function ")) {
            return true;
        }
        else {
            int startComment = getStartComment(strLine);
            if (startComment >= 0) {
                strLine = strLine.substring(0, startComment-1);
            }
            strLine = G.ltrimTab(strLine);
            if (strLine.equalsIgnoreCase("Exit Function")) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    private boolean isLoopUntilSentence(String strLine) {
        if (G.beginLike(strLine, "Loop Until")) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isLoopWhileSentence(String strLine) {
        if (G.beginLike(strLine, "Loop While")) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isLoopSentence(String strLine) {
        if (G.beginLike(strLine, "Loop ")) {
            return true;
        }
        else {
            int startComment = getStartComment(strLine);
            if (startComment >= 0) {
                strLine = strLine.substring(0, startComment-1);
            }
            strLine = G.ltrimTab(strLine);
            if (strLine.equalsIgnoreCase("Loop")) {
                return true;
            }
            else {
                return false;
            }
        }
    }
    
    private boolean isWendSentence(String strLine) {
        if (G.beginLike(strLine, "Wend ")) {
            return true;
        }
        else {
            int startComment = getStartComment(strLine);
            if (startComment >= 0) {
                strLine = strLine.substring(0, startComment-1);
            }
            strLine = G.ltrimTab(strLine);
            if (strLine.equalsIgnoreCase("Wend")) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    private boolean isNextSentence(String strLine) {
        if (G.beginLike(strLine, "Next ")) {
            return true;
        }
        else {
            int startComment = getStartComment(strLine);
            if (startComment >= 0) {
                strLine = strLine.substring(0, startComment-1);
            }
            strLine = G.ltrimTab(strLine);
            if (strLine.equalsIgnoreCase("Next")) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    private boolean isEndIfSentence(String strLine) {
        if (G.beginLike(strLine, "End If ")) {
            return true;
        }
        else {
            int startComment = getStartComment(strLine);
            if (startComment >= 0) {
                strLine = strLine.substring(0, startComment-1);
            }
            strLine = G.ltrimTab(strLine);
            if (strLine.equalsIgnoreCase("End If")) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    private boolean isOnErrorSentence(String strLine) {
        if (G.beginLike(strLine, "On Error ")) {
            return true;
        }
        else {
            int startComment = getStartComment(strLine);
            if (startComment >= 0) {
                strLine = strLine.substring(0, startComment-1);
            }
            strLine = G.ltrimTab(strLine);
            if (strLine.equalsIgnoreCase("On Error")) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    private boolean isOnErrorLabelSentence(String strLine) {
        if (m_onErrorLabel.isEmpty()) {
            return false;
        }
        else if (G.beginLike(strLine, m_onErrorLabel)) {
            return true;
        }
        else {
            int startComment = getStartComment(strLine);
            if (startComment >= 0) {
                strLine = strLine.substring(0, startComment-1);
            }
            strLine = G.ltrimTab(strLine);
            if (strLine.equalsIgnoreCase(m_onErrorLabel)) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    /*
    private String translateComments(String strLine) {
        // We only translate ' in // if the line doesn't contain a // yet
        // because if the line does, it means that the comments
        // has already been translated
        //
        if (!strLine.contains("//")) {
            int startComment = getStartComment(strLine);
            if (startComment >= 0) {
                strLine = strLine.substring(0, startComment-1)
                            + " //"
                            + strLine.substring(startComment);
            }
        }
        return strLine;
    }
    */

    private String translateCaseSentence(String strLine) {
        String switchStatetment = "";
        boolean identifierHasStarted = false;
        boolean parenthesesClosed = false;
        boolean isCaseElse = false;
        String[] words = G.split(strLine);

        if (m_isFirstCase) {
            m_isFirstCase = false;
            switchStatetment = "case ";
        }
        else {
            switchStatetment = "    break;" + newline + getTabs();
            if (G.beginLike(strLine, "case else")) {
                switchStatetment += "default ";
                isCaseElse = true;
            }
            else                
                switchStatetment += "case ";
        }

        for (int i = 0; i < words.length; i++) {
            if (identifierHasStarted) {
                if (G.beginLike(words[i], "'")) {
                    switchStatetment += ": //";
                    parenthesesClosed = true;
                }
                if (!parenthesesClosed) {
                    if (words[i].equals(",")) {
                        switchStatetment += ":" + newline + getTabs() + "case ";
                    }
                    else {
                        switchStatetment += words[i];
                    }
                }
                else {
                    switchStatetment += words[i];
                }
            }
            else {
                if (isCaseElse) {
                    if (words[i].toLowerCase().equals("else"))
                        identifierHasStarted = true;                    
                }
                else if (words[i].toLowerCase().equals("case"))
                    identifierHasStarted = true;
            }
        }
        if (!parenthesesClosed) {
            if (isCaseElse)
                switchStatetment = switchStatetment.trim() + ":";
            else
                switchStatetment += ":";
        }
        return switchStatetment + newline;
    }

    private String translateSelectCaseSentence(String strLine) {
        String switchStatetment = "";
        boolean identifierHasStarted = false;
        boolean parenthesesClosed = false;
        String[] words = G.split(strLine);

        m_isFirstCase = true;

        for (int i = 0; i < words.length; i++) {
            if (identifierHasStarted) {
                if (G.beginLike(words[i], "'")) {
                    switchStatetment += ") { //";
                    parenthesesClosed = true;
                }
                switchStatetment += words[i];
            }
            else {
                if (words[i].toLowerCase().equals("case"))
                    identifierHasStarted = true;
            }
        }
        switchStatetment = translateSentence(switchStatetment.trim());
        if (!parenthesesClosed)
            switchStatetment += ") {";
        return "switch (" + switchStatetment + newline;
    }

    private String translateIfSentence(String strLine) {
        // the if block can contain an or more call sentence
        // and one or more logic operators and unary operators
        // binary operators: and, or
        // unary operator: not
        //
        boolean literalFlag = false;
        boolean thenFound = false;
        boolean previousWasNot = false;
        boolean previousWasParentheses = false;
        boolean isFirstWord = true;
        String javaSentenceIf = "";
        String javaSentenceBlock = "";
        String comments = "";

        int startComment = getStartComment(strLine);
        if (startComment >= 0) {
            comments =  "//" + strLine.substring(startComment);
            strLine = strLine.substring(0, startComment-1);
        }

        String[] words = G.splitSpace(strLine);//strLine.split("\\s+");

        // we start in 1 because word[0] is "If"
        //
        for (int i = 1; i < words.length; i++) {
            // typical sentence:
                // " if x then " -> 3 words
                // " if x and z then " -> 5 words
                // " if x and callFunction() then " -> 5 words
                // " if ((x or z) and y) or callFunction(param1, param2, param3)) then " -> too many words :)
                //
            // rules
                // 1- we have to add parentheses
                // 2- we have to respect parentheses
                // 3- we have to detect function calls
                // 4- we have to translate "or", "and", and "not"

            for (int j = 0; j < words[i].length(); j++) {
                if (words[i].charAt(j) == '"') {
                    literalFlag = !literalFlag;
                }
            }

            if (literalFlag) {
                if (thenFound) {
                    javaSentenceBlock += " " + words[i];
                }
                else {
                    javaSentenceIf += " " + words[i];
                }
            }
            else {
                if (thenFound) {
                    javaSentenceBlock += " " + words[i];
                }
                else {
                    if (words[i].equalsIgnoreCase("then")) {
                        thenFound = true;
                    }
                    else if (words[i].equalsIgnoreCase("and")) {
                        javaSentenceIf += " &&";
                    }
                    else if (words[i].equalsIgnoreCase(")and")) {
                        javaSentenceIf += ") &&";
                    }
                    else if (words[i].equalsIgnoreCase("and(")) {
                        javaSentenceIf += " && (";
                    }
                    else if (words[i].equalsIgnoreCase("or")) {
                        javaSentenceIf += " ||";
                    }
                    else if (words[i].equalsIgnoreCase(")or")) {
                        javaSentenceIf += ") ||";
                    }
                    else if (words[i].equalsIgnoreCase("or(")) {
                        javaSentenceIf += " || (";
                    }
                    else if (words[i].equalsIgnoreCase("<>")) {
                        javaSentenceIf += " !=";
                    }
                    else if (words[i].equalsIgnoreCase("not")) {
                        javaSentenceIf += " !";
                    }
                    else if (words[i].equals("(")) {
                        if (previousWasNot)
                            javaSentenceIf += "(";
                        else
                            javaSentenceIf += " (";
                    }
                    else if (words[i].equals(")")) {
                        javaSentenceIf += ")";
                    }
                    else if (words[i].equalsIgnoreCase("=")) {
                        javaSentenceIf += " ==";
                    }
                    else {
                        if (isFirstWord) {
                            javaSentenceIf += words[i];
                            isFirstWord = false;
                        }
                        else if (previousWasNot || previousWasParentheses) {
                            javaSentenceIf += words[i];
                        }
                        else {
                            javaSentenceIf += " " + words[i];
                        }
                    }

                    // flags
                    //
                    if (words[i].equalsIgnoreCase("not")) {
                        previousWasNot = true;
                    }
                    else {
                        previousWasNot = false;
                    }

                    if (words[i].charAt(words[i].length()-1) == '(') {
                        previousWasParentheses = true;
                    }
                    else {
                        previousWasParentheses = false;
                    }
                }
            }
        }
        if (javaSentenceBlock.isEmpty()) {
            return "if (" + translateSentence(javaSentenceIf) + ") {"
                    + comments + newline;
        }
        else {
            javaSentenceIf = translateSentence(javaSentenceIf);
            javaSentenceBlock = translateSentenceWithColon(G.ltrimTab(javaSentenceBlock));
            // if "one line if" sentence in vb became two or more sentence
            // we have to add a tab after every \n to keep the if
            // indented
            //
            if (javaSentenceBlock.contains("\n")) {
                return comments
                        + "if (" + javaSentenceIf + ") { " + newline
                        + getTabs() + C_TAB
                        + javaSentenceBlock.replace("\n", "\n    ") + newline
                        + getTabs() + "}"  + newline;
            }
            else {
                return "if (" + javaSentenceIf + ") { "
                        + javaSentenceBlock
                        + " }" + comments + newline;
            }
        }
    }

    private String translateElseIfSentence(String strLine) {
        if (m_wasSingleLineIf) {
            return "else " + translateIfSentence(strLine);
        }
        else {
            return "} "
                    + newline
                    + getTabs()
                    + "else "
                    + translateIfSentence(strLine);
        }
    }

    private String translateElseSentence(String strLine) {
        String javaSentenceBlock = "";
        String comments = "";
        int startComment = getStartComment(strLine);
        if (startComment >= 0) {
            comments =  " //" + strLine.substring(startComment);
            strLine = strLine.substring(0, startComment-1);
        }
        strLine = strLine.trim();
        if (!strLine.equalsIgnoreCase("Else")) {
            javaSentenceBlock = " "
                                + translateSentenceWithColon(G.ltrimTab(strLine.substring(4)))
                                + " }";
        }
        if (m_wasSingleLineIf) {
            return "else {"
                    + javaSentenceBlock
                    + comments
                    + newline;
        }
        else {
            return "} "
                    + newline
                    + getTabs()
                    + "else {"
                    + javaSentenceBlock
                    + comments
                    + newline;
        }
    }

    private String translateEndIfSentence(String strLine) {
        int startComment = getStartComment(strLine);
        if (startComment >= 0) {
            String comments = "";
            comments =  "//" + strLine.substring(startComment);
            return "} " + comments + newline;
        }
        else {
            return "}" + newline;
        }
    }

    private String translateEndSelectSentence(String strLine) {
        int startComment = getStartComment(strLine);
        if (startComment >= 0) {
            String comments = "";
            comments =  "//" + strLine.substring(startComment);
            return "        break;" + newline + getTabs() + "} " + comments + newline;
        }
        else {
            return "        break;" + newline + getTabs() + "}" + newline;
        }
    }

    private String translateExitFunctionSentence(String strLine) {
        int startComment = getStartComment(strLine);
        if (startComment >= 0) {
            String comments = "";
            comments =  "//" + strLine.substring(startComment);
            if (m_previousWasReturn)
                return comments + newline;
            else
                if (m_function.getNeedReturnVariable())
                    return "return _rtn;" + comments + newline;
                else
                    return "return null;" + comments + newline;
        }
        else {
            if (m_previousWasReturn) {
                m_emptyLine = true;
                return "";
            }
            else {
                if (m_function.getNeedReturnVariable())
                    return "return _rtn;" + newline;
                else
                    return "return null;" + newline;
            }
        }
    }

    private String translateDoWhileSentence(String strLine) {
        // the 'do while' is a while sentence in java. 
        // it only exists in vb 6 to allow 'exit do', because
        // 'exit while' is not a recognized expresion in vb 6
        //
        strLine = strLine.substring(3); // remove the do
        return translateWhileSentence(strLine);
    }

    private String translateDoSentence(String strLine) {
        if (strLine.length() > 2)
            return "do " + strLine.substring(3);
        else
            return "do";
    }
    
    private String translateWhileSentence(String strLine) {
        // the while block can contain an or more call sentence
        // and one or more logic operators and unary operators
        // binary operators: and, or
        // unary operator: not
        //
        boolean literalFlag = false;
        boolean previousWasNot = false;
        boolean previousWasParentheses = false;
        boolean isFirstWord = true;
        String javaSentenceWhile = "";
        String comments = "";

        int startComment = getStartComment(strLine);
        if (startComment >= 0) {
            comments =  "//" + strLine.substring(startComment);
            strLine = strLine.substring(0, startComment-1);
        }

        String[] words = G.splitSpace(strLine);//strLine.split("\\s+");

        // we start in 1 because word[0] is "While"
        //
        for (int i = 1; i < words.length; i++) {
            // typical sentence:
                // " while x " -> 2 words
                // " while x and z " -> 4 words
                // " while x and callFunction() " -> 4 words
                // " while ((x or z) and y) or callFunction(param1, param2, param3)) " -> too many words :)
                //
            // rules
                // 1- we have to add parentheses
                // 2- we have to respect parentheses
                // 3- we have to detect function calls
                // 4- we have to translate "or", "and", and "not"

            for (int j = 0; j < words[i].length(); j++) {
                if (words[i].charAt(j) == '"') {
                    literalFlag = !literalFlag;
                }
            }

            if (literalFlag) {
                javaSentenceWhile += " " + words[i];
            }
            else {
                if (words[i].equalsIgnoreCase("and")) {
                    javaSentenceWhile += " &&";
                }
                else if (words[i].equalsIgnoreCase(")and")) {
                    javaSentenceWhile += ") &&";
                }
                else if (words[i].equalsIgnoreCase("and(")) {
                    javaSentenceWhile += " && (";
                }
                else if (words[i].equalsIgnoreCase("or")) {
                    javaSentenceWhile += " ||";
                }
                else if (words[i].equalsIgnoreCase(")or")) {
                    javaSentenceWhile += ") ||";
                }
                else if (words[i].equalsIgnoreCase("or(")) {
                    javaSentenceWhile += " || (";
                }
                else if (words[i].equalsIgnoreCase("<>")) {
                    javaSentenceWhile += " !=";
                }
                else if (words[i].equalsIgnoreCase("not")) {
                    javaSentenceWhile += " !";
                }
                else if (words[i].equals("(")) {
                    if (previousWasNot)
                        javaSentenceWhile += "(";
                    else
                        javaSentenceWhile += " (";
                }
                else if (words[i].equals(")")) {
                    javaSentenceWhile += ")";
                }
                else if (words[i].equalsIgnoreCase("=")) {
                    javaSentenceWhile += " ==";
                }
                else {
                    if (isFirstWord) {
                        javaSentenceWhile += words[i];
                        isFirstWord = false;
                    }
                    else if (previousWasNot || previousWasParentheses) {
                        javaSentenceWhile += words[i];
                    }
                    else {
                        javaSentenceWhile += " " + words[i];
                    }
                }

                // flags
                //
                if (words[i].equalsIgnoreCase("not")) {
                    previousWasNot = true;
                }
                else {
                    previousWasNot = false;
                }

                if (words[i].charAt(words[i].length()-1) == '(') {
                    previousWasParentheses = true;
                }
                else {
                    previousWasParentheses = false;
                }
            }
        }
        return "while (" + translateSentence(javaSentenceWhile) + ") {"
                + comments + newline;
    }

    private String translateForSentence(String strLine) {

        m_iteratorIndex++;

        // the for block can have three forms:
        //   for each var in collection
        //   for var = value_x to value_y
        //   for var = value_x to value_y step step_value
        //
        boolean literalFlag = false;
        boolean eachFound = false;
        boolean toFound = false;
        boolean inFound = false;
        boolean equalsFound = false;
        boolean stepFound = false;
        String iterator = "";
        String endValue = "";
        String startValue = "";
        String increment = "";
        String step = "";
        String collection = "";
        String comments = "";

        int startComment = getStartComment(strLine);
        if (startComment >= 0) {
            comments =  "//" + strLine.substring(startComment);
            strLine = strLine.substring(0, startComment-1);
        }

        String[] words = G.splitSpace(strLine);//strLine.split("\\s+");

        // we start in 1 because word[0] is "For"
        //
        for (int i = 1; i < words.length; i++) {
            // typical sentence:
                //   for each var in collection
                //   for var = value_x to value_y
                //   for var = value_x to value_y step step_value
                //
            // rules
                // 1- we have to add parentheses
                // 2- we have to respect parentheses
                // 3- we have to detect function calls

            for (int j = 0; j < words[i].length(); j++) {
                if (words[i].charAt(j) == '"') {
                    literalFlag = !literalFlag;
                }
            }

            if (literalFlag) {
                if (eachFound) {
                    if (inFound) {
                        collection += " " + words[i];
                    }
                    else {
                        iterator += " " + words[i];
                    }
                }
                else if (equalsFound) {
                    if (stepFound) {
                        step += " " + words[i];
                    }
                    else if (toFound) {
                        endValue += " " + words[i];
                    }
                    else {
                        startValue += " " + words[i];
                    }
                }
                else {
                    iterator += " " + words[i];
                }
            }
            else {
                if (eachFound) {
                    if (inFound) {
                        collection += " " + words[i];
                    }
                    else if (words[i].equalsIgnoreCase("in")) {
                        inFound = true;
                    }
                    else {
                        iterator += " " + words[i];
                    }
                }
                else if (equalsFound) {
                    if (stepFound) {
                        step += " " + words[i];
                    }
                    else if (words[i].equalsIgnoreCase("step")) {
                        stepFound = true;
                    }
                    else if (toFound) {
                        endValue += " " + words[i];
                    }
                    else if (words[i].equalsIgnoreCase("to")) {
                        toFound = true;
                    }
                    else {
                        startValue += " " + words[i];
                    }
                }
                else {
                    if (words[i].equalsIgnoreCase("each")) {
                        eachFound = true;
                    }
                    else if (words[i].equalsIgnoreCase("=")) {
                        equalsFound = true;
                    }
                    else if (words[i].equals("(")) {
                        iterator += "(";
                    }
                    else if (words[i].equals(")")) {
                        iterator += ")";
                    }
                    else {
                        iterator += " " + words[i];
                    }
                }
            }
        }
        if (eachFound) {
            collection = collection.trim();
            iterator = iterator.trim();
            return "for (int " + m_iterators[m_iteratorIndex] + " = 0;"
                            + " " + m_iterators[m_iteratorIndex] + " < "
                            + translateSentence(collection) + ".size();"
                            + " " + m_iterators[m_iteratorIndex] + "++) {"
                            + comments + newline
                            + getTabs() + C_TAB
                            + iterator + " = " + collection 
                            + ".getItem(" + m_iterators[m_iteratorIndex] + ");" + newline;
        }
        else {

            if (step.replace(" ","").equals("+1")) {
                increment = "++";
            }
            else if (step.replace(" ","").equals("-1")) {
                increment = "--";
            }
            else if (step.isEmpty()) {
                increment = "++";
            }
            else {
                increment = " = " + iterator + step;
            }
            iterator = iterator.trim();
            startValue = startValue.trim();
            return "for (" + iterator + " = " + translateSentence(startValue) + "; "
                            + iterator + " <= " + translateSentence(endValue) + "; "
                            + iterator + increment + ") {"
                            + comments + newline;
        }
    }

    private String translateLoopUntilWhileSentence(String strLine, boolean isWhile) {
        // the 'loop while' and the 'loop until' are a while sentence in java. 
        // it only exists in vb 6 to allow 'exit do', because
        // 'exit while' is not a recognized expresion in vb 6
        //
                                        // remove the 'loop ' in 'loop while' or 'loop until'
        strLine = translateWhileSentence(strLine.substring(5));
        if (isWhile)
            strLine = "} " + strLine;
        else
            strLine = "} while !(" + strLine.substring(6) + ")";
        return strLine;
    }

    private String translateLoopSentence(String strLine) {
        int startComment = getStartComment(strLine);
        if (startComment >= 0) {
            String comments = "";
            comments =  "//" + strLine.substring(startComment);
            return "}" + comments + newline;
        }
        else {
            return "}" + newline;
        }
    }
    
    private String translateWendSentence(String strLine) {
        int startComment = getStartComment(strLine);
        if (startComment >= 0) {
            String comments = "";
            comments =  "//" + strLine.substring(startComment);
            return "}" + comments + newline;
        }
        else {
            return "}" + newline;
        }
    }

    private String translateNextSentence(String strLine) {

        m_iteratorIndex--;

        int startComment = getStartComment(strLine);
        if (startComment >= 0) {
            String comments = "";
            comments =  "//" + strLine.substring(startComment);
            return "} " + comments + newline;
        }
        else {
            return "}" + newline;
        }
    }

    private String checkOnErrorLabelFound() {
        if (!m_onErrorLabel.isEmpty())
            return "//*TODO:** the error label " + m_onErrorLabel + " couldn't be found" + newline + getTabs();
        else
            return "";
    }

    private String getEndOfPreviousOnError(String tabs) {
        if (m_onErrorFound) 
            return tabs + "}" + newline + getTabs();
        else
            return "";
    }

    private String translateOnErrorSentence(String strLine) {
        // On Error Goto error_label
        // On Error Resume Next
        // On Error Goto 0
        //
        String onErrorLabelNotFound = checkOnErrorLabelFound();
        m_onErrorResumeNext = false;
        m_onErrorLabel = "";
        String comments = "";
        int startComment = getStartComment(strLine);
        if (startComment >= 0) {
            comments =  " //" + strLine.substring(startComment);
            strLine = strLine.substring(9, startComment).trim();
        }
        else
            strLine = strLine.substring(9).trim();

        String trySentence = "";
        if (strLine.equalsIgnoreCase("Resume next")) {
            m_onErrorResumeNext = true;
            m_onErrorFound = false;
        }
        else if (G.beginLike(strLine, "GoTo")) {
            m_onErrorLabel = strLine.substring(5).trim() + ":";
            trySentence = "try {";
        }

        String endOfPreviousOnError = getEndOfPreviousOnError("");

        return endOfPreviousOnError
                + onErrorLabelNotFound
                + trySentence
                + comments + newline;
    }

    private String translateOnErrorLabelSentence(String strLine) {
        m_onErrorLabel = "";
        String comments = "";
        int startComment = getStartComment(strLine);
        if (startComment >= 0) {
            comments =  " //" + strLine.substring(startComment);
        }
        return "} catch (Exception ex) {" + comments + newline;
    }

    private String translateSentenceWithNewLine(String strLine) {
        return translateSentenceWithColon(strLine) + newline;
    }

    private String translateSentenceWithColon(String strLine) {
        strLine = translateSentence(strLine);
        if (!strLine.trim().isEmpty()) {
            if (strLine.startsWith("/*") && strLine.endsWith("*/")) {
                return strLine;
            }
            else if (";}".contains(strLine.substring(strLine.length() - 1))) {
                return strLine;
            }
        }
        return G.rtrim(strLine) + ";";
    }

    private String translateSentence(String strLine) {
        strLine = G.ltrimTab(strLine);
        int startComment = getStartComment(strLine);
        if (G.beginLike(strLine,m_vbFunctionName + " = ")) {
            if (startComment > 0) {
                String comments = "";
                comments =  " //" + strLine.substring(startComment);
                strLine = strLine.substring(0, startComment).trim()
                            + ";"
                            + comments;
            }
        }
        if (G.beginLike(strLine,"Set " + m_vbFunctionName + " = ")) {
            if (startComment > 0) {
                String comments = "";
                comments =  " //" + strLine.substring(startComment);
                strLine = strLine.substring(4, startComment).trim()
                            + ";"
                            + comments;
            }
            else {
                strLine = strLine.substring(4);
            }
        }
        // now we replace setences which set the value of return value using
        // the name of the function like getPrinterName = apiCallGetDefaultPrinter()
        //
        strLine = replaceSetReturnValueSentence(strLine);

        if (G.beginLike(strLine,"Set ")) {
            strLine = strLine.substring(4);
        }
        if (G.beginLike(strLine,"Let ")) {
            strLine = strLine.substring(4);
        }
        strLine = replaceMemberVariables(strLine);
        strLine = replaceFunctionVariables(strLine);
        strLine = replaceMidSentence(strLine);
        strLine = replaceTypeOfSentence(strLine);
        strLine = replaceLeftSentence(strLine);
        strLine = replaceRightSentence(strLine);
        strLine = replaceLCaseSentence(strLine);
        strLine = replaceUCaseSentence(strLine);
        strLine = replaceLenSentence(strLine);
        strLine = replaceTrimSentence(strLine);
        strLine = replaceReplaceSentence(strLine);
        strLine = translateVbOperators(strLine);
        if (m_translateToJava) {
            strLine = replaceStringComparison(strLine, "==");
            strLine = replaceStringComparison(strLine, "!=");
            strLine = replaceStringCompareNullString(strLine);
        }            
        strLine = replaceInStrSentence(strLine);
        strLine = replaceVbWords(strLine);
        strLine = replaceIsNothing(strLine);
        strLine = replaceNothing(strLine);
        strLine = replaceAmpersand(strLine);
        strLine = translateFunctionCall(strLine);
        strLine = replaceWithSentence(strLine);
        strLine = replaceEndWithSentence(strLine);
        strLine = replaceRedimSentence(strLine);
        strLine = translateWithSentence(strLine);
        strLine = replaceVbNameWithJavaName(strLine);
        strLine = replaceExitSentence(strLine);
        strLine = replaceSlashInLiterals(strLine);
        strLine = replaceNewSentence(strLine);
        strLine = replaceRaiseEvent(strLine);
        strLine = replaceIsNumericSentence(strLine);
        strLine = replaceCDblSentence(strLine);
        strLine = replaceCIntSentence(strLine);
        strLine = replaceCLngSentence(strLine);
        strLine = replaceCSngSentence(strLine);
        strLine = replaceCCurSentence(strLine);
        strLine = replaceCDateSentence(strLine);
        strLine = replacePropertySetSentence(strLine);
        strLine = replaceNotSentence(strLine);
        strLine = replaceADODBSentence(strLine);
        strLine = replaceResumeSentence(strLine);
        strLine = replaceGotoSentence(strLine);
        strLine = replaceLabelSentence(strLine);
        
        // this has to be at the end because the ? : sintax
        // add some complexity that we don't need to process
        // in any of the previews function
        //
        strLine = replaceIifSentence(strLine);

        // this call has to be the last sentences in this function
        // all the changes have to be done before this call
        //
        strLine = checkEventVariableInitialization(strLine);

        return strLine;
    }

    // if the function is an event handler we will call
    // to this function in the anonymous inner class
    // which extends the adapter class of the event listener
    //
    private void checkEventHandler(String strLine) {
        // in vb all event handler functions have an
        // underscore which divide the name of the
        // variable and the name of the event
        //
        if (m_vbFunctionName.indexOf("_") > 0) {
            int i = 0;
            for (i = m_vbFunctionName.length() - 1; i > 0; i--) {
                if (m_vbFunctionName.charAt(i) == '_') {
                    break;
                }
            }
            if (i > 0) {
                String variable = m_vbFunctionName.substring(0, i);
                Iterator itrListener = m_eventListeners.iterator();
                while(itrListener.hasNext()) {
                    EventListener listener = (EventListener)itrListener.next();
                    if (variable.equals(listener.getGeneratorVb())) {
                        listener.getSourceCode().append(
                                getEventHandlerDeclaration(strLine));
                        break;
                    }
                }
            }
        }
    }

    private String getEventHandlerDeclaration(String strLine) {
        String handler = "";
        int i = strLine.indexOf("(");
        if (i > 0) {
            int j = 0;
            for (j = i; j > 0; j--) {
                if (strLine.charAt(j) == ' ') {
                    break;
                }
            }
            if (j > 0) {
                String functionCall = strLine.substring(j + 1, i);
                for (j = functionCall.length() - 1; j > 0; j--) {
                    if (functionCall.charAt(j) == '_') {
                        break;
                    }
                }
                String functionName = functionCall.substring(j + 1);
                j = strLine.indexOf(")");
                String params = "";
                String paramsCall = "";
                // check for empty params eg: function()
                if (j - i > 1) {
                    params = strLine.substring(i + 1, j);
                    String[] words = G.split3(params, ",");
                    for (i = 0; i < words.length; i++) {
                        String param = words[i].trim();
                        j = param.indexOf(" ");
                        if (j > 0)
                            j++;
                        else
                            j = 0;
                        paramsCall += param.substring(j) + ", ";
                    }
                    if (paramsCall.length() > 0)
                        paramsCall = paramsCall.substring(0, paramsCall.length() - 1);
                }
                handler = outerTabHandler
                            +"public void " + functionName + "(" + params  + ") {"
                            + newline
                            + innerTabHandler + functionCall + "(" + paramsCall + ");"
                            + newline
                            + outerTabHandler + "}" + newline;
            }
        }
        if (handler.isEmpty())
            handler = "//*TODO:** the event handler couldn't be translated: "
                            + strLine + newline;
        return handler;
    }

    private String checkEventVariableInitialization(String strLine) {
        int i = strLine.toLowerCase().indexOf(" = new ");
        if (i > 0) {
            String variable = strLine.substring(0, i).trim();
            Variable var = getMemberVariable(variable);
            if (var != null) {
                if (var.isEventGenerator) {
                    if (var.getVbName().equals("m_fFormula")) {
                        int q = 0;
                    }
                    strLine += ";"
                                + newline
                                + getTabs()
                                + getEventMacroName(var.getJavaName());
                }
            }
        }
        return strLine;
    }

    private void checkRaiseEvent(String strLine) {
        if (strLine.toLowerCase().contains("raiseevent")) {
            m_raiseEventFunctions.add(m_vbFunctionName);
        }
    }

    private void checkNeedReturnVariable(String strLine) {
        if (m_setReturnValueFound) {
            if (!strLine.trim().isEmpty())
                m_needReturnVariable = true;
        }
        else if (G.beginLike(strLine, m_vbFunctionName + " = ")) {
            m_setReturnValueFound = true;
        }
        else if (strLine.toLowerCase().contains(" " + m_vbFunctionName + " = ")) {
            m_setReturnValueFound = true;
        }
    }

    private void setNeedReturnValue() {
        m_function.setNeedReturnVariable(m_needReturnVariable);
    }

    private String replaceSlashInLiterals(String strLine) {
        boolean literalFlag = false;
        String workLine = "";
        for (int i = 0; i < strLine.length(); i++) {
            if (strLine.charAt(i) == '"') {
                literalFlag = !literalFlag;
            }
            if (literalFlag) {
                if (strLine.charAt(i) == '\\') {
                    workLine += "\\\\";
                }
                else {
                    workLine += String.valueOf(strLine.charAt(i));
                }
            }
            else {
                workLine += String.valueOf(strLine.charAt(i));
            }
        }
        return workLine;
    }

    private String replaceExitSentence(String strLine) {
        if (G.endLike(strLine, "Exit Function")) {
            if (m_function.getNeedReturnVariable()) {
                return "return _rtn";
            }
            else {
                return "return " + m_returnValue;
            }
        }
        else if (G.endLike(strLine, "Exit Sub")) {
            return "return";
        }
        else if (G.endLike(strLine, "Exit For")) {
            return "break";
        }
        else if (G.endLike(strLine, "Exit Do")) {
            return "break";
        }
        else {
            return strLine;
        }
    }

    private String replaceVbNameWithJavaName(String strLine) {
        
        IdentifierInfo info = null;
        String type = "";
        String parent = "";
        String[] words = G.split2(strLine, "!\t/*-+ ,.()[]");
        strLine = "";
        String[] parents = new String[30]; // why 30? who nows :P, 30 should be enough :)
        int openParentheses = 0;
        boolean previousWasPeriod = false;

        for (int i = 0; i < words.length; i++) {
            if (!("!\t/*-+ ,.()[]'\"".contains(words[i]))) {
                info = getIdentifierInfo(words[i], parent, !parent.isEmpty());
                if (info == null)
                    type = "";
                else if (info.isFunction) {
                    type = info.function.getReturnType().dataType;
                    words[i] = info.function.getJavaName();
                    String functionClassName = info.function.getJavaClassName();
                    // if the function doesn't have a parent and it is not declared
                    // in this class it must be a statict function (the equivalent
                    // java to vb6 public functions in bas files)
                    //
                    if (parent.isEmpty() 
                            && !functionClassName.equals(m_javaClassName)
                            && !functionClassName.isEmpty()) {
                        // we need to add the class name to access the function
                        //
                        words[i] = functionClassName + '.' + words[i];
                    }
                    if (i + 1 < words.length) {
                        if (!words[i + 1].equals("("))
                            words[i] += "()";
                    }
                    else {
                        words[i] += "()";
                    }
                }
                else {
                    if (info.variable.isArray) {
                        int arrayParentheses = 0;
                        for (int k = i + 1; k < words.length; k++) {
                            if (words[k].equals("(")) {
                                if (arrayParentheses == 0) {
                                    words[k] = "[";
                                }
                                arrayParentheses++;
                            }
                            else if (words[k].equals(")")) {
                                arrayParentheses--;
                                if (arrayParentheses == 0) {
                                    words[k] = "]";
                                    break;
                                }
                            }
                            else if (arrayParentheses == 0
                                    && !"\t ".contains(words[k])) {
                                break;
                            }
                        }
                    }
                    type = info.variable.dataType;
                    if (!previousWasPeriod) {
                        if (info.variable.isEnumMember)
                            words[i] = info.variable.className
                                        + "." + info.variable.getJavaName();
                        else {
                            if (info.variable.isPublic) {
                                words[i] = info.variable.className
                                            + "." + info.variable.getJavaName();                            
                            }
                            else
                                words[i] = info.variable.getJavaName();
                        }
                    }
                    else {
                        words[i] = info.variable.getJavaName();
                    }
                }
                parent = type;
            }
            else if (words[i].equals("(")) {
                parents[openParentheses] = parent;
                parent = "";
                openParentheses++;
            }
            else if (words[i].equals(")")) {
                openParentheses--;
                
                // debug
                /*
                if (openParentheses > parents.length-1 || openParentheses < 0)
                {
                    int q = 0;
                }
                 /* 
                 */
                // debug
                parent = parents[openParentheses];
            }
            else if (words[i].equals("[")) {
                parents[openParentheses] = parent;
                parent = "";
                openParentheses++;
            }
            else if (words[i].equals("]")) {
                openParentheses--;
                parent = parents[openParentheses];
                parents[openParentheses] = "";
            }
            else if (words[i].equals(" ")) {
                parent = "";
            }
            previousWasPeriod = words[i].equals(".");
            strLine += words[i];
        }
        return strLine;
    }

    private String replacePropertySetSentence(String strLine) {
        IdentifierInfo info = null;
        String type = "";
        String parent = "";
        boolean addParentheses = false;
        String[] words = G.split2(strLine, "\t/*-+ .()");
        strLine = "";
        String[] parents = new String[30]; // why 30? who nows :P, 30 should be enough :)
        int openParentheses = 0;

        for (int i = 0; i < words.length; i++) {
            if (!(",.()\"'".contains(words[i]))) {
                info = getIdentifierInfo(words[i], parent, !parent.isEmpty());
                if (info == null)
                    type = "";
                else if (info.isFunction) {
                    type = info.function.getReturnType().dataType;
                    
                    if (i + 4 < words.length) {
                        // set with one parameter eg: property set value(byval rhs as object)
                        //
                        if (words[i + 4].equals("=")) {
                            String setter = info.function.getJavaName();
                            setter = "set" + setter.substring(3, setter.length());
                            words[i] = setter;
                            words[i + 2] = ""; // )
                            words[i + 3] = ""; // space character
                            words[i + 4] = ""; // =
                            words[i + 5] = ""; // space character
                            addParentheses = true;
                        }
                        // set with two or more parameters
                        // eg: property set value(byval name as string, byval rhs as object)
                        //
                        // we can have to translate something like this
                        //
                        //  myObject.value(getValueForKey(gdb.ValField(rs.fields, "myKey"))) = theValue
                        //
                        // to this
                        //
                        //  myObject.setValue(getValueForKey(gdb.ValField(rs.fields, "myKey"))), theValue);
                        //
                        else {
                            int equalsIdx = 0;
                            int parentheses = 0;
                            boolean closedParenthesesFound = false;
                            boolean moreThanOneParenthesesFound = false;
                            String parameters = "";
                            for (int k = i + 1; k < words.length; k++) {
                                if (words[k].equals("=")) {
                                    equalsIdx = k;
                                    break;
                                }
                            }
                            if (equalsIdx > 0) {
                                for (int k = i +1; k < equalsIdx; k++) {
                                    // skip spaces
                                    //
                                    if (!words[k].trim().isEmpty()) {
                                        // if it is not a space and we
                                        // have found a close parentheses
                                        // for this function we are
                                        // dealing with something like this
                                        //
                                        // myObject.setProperty(someParameters) somethingElse =
                                        //
                                        // we can not translate this
                                        //
                                        if (closedParenthesesFound) {
                                            moreThanOneParenthesesFound = true;
                                            break;
                                        }
                                        // for each ( we increase prentheses variable
                                        //
                                        else if (words[k].equals("(")) {
                                            parameters += words[k];
                                            parentheses++;
                                        }
                                        // for each ) we decrease parentheses variable
                                        //
                                        else if (words[k].equals(")")) {
                                            parentheses--;
                                            // if parentheses reach 0 we have
                                            // found the close parentheses of
                                            // the parameters of the function
                                            //
                                            if (parentheses == 0) {
                                                // we turn on this flag to detect
                                                // sentences like:
                                                //
                                                // myObject.setProperty(paramethers)(paramethers) =
                                                //
                                                // we can not translate this
                                                //
                                                closedParenthesesFound = true;
                                            }
                                            else
                                                parameters += words[k];
                                        }
                                        else
                                            parameters += words[k];
                                    }
                                    else
                                        parameters += words[k];
                                }
                                if (!moreThanOneParenthesesFound && parentheses == 0) {
                                    String setter = info.function.getJavaName();
                                    setter = "set" + setter.substring(3, setter.length());
                                    addParentheses = true;
                                    strLine += setter + parameters.trim() + ",";
                                    i = equalsIdx + 1;
                                }
                            }
                        }
                    }
                }
                else {
                    type = info.variable.dataType;
                }
                parent = type;
            }
            else if (words[i].equals("(")) {
                parents[openParentheses] = parent;
                parent = "";
                openParentheses++;
            }
            else if (words[i].equals(")")) {
                openParentheses--;
                parent = parents[openParentheses];
            }
            else if (words[i].equals(" ")) {
                parent = "";
            }
            
            strLine += words[i];
        }
        if (addParentheses)
            strLine += ")";
        return strLine;
    }

    private String replaceIsNothing(String strLine) {
        // we contemplate Not identifier Is Nothing
        //
        String[] words = G.split2(strLine, "!\t/*-+ .()");
        strLine = "";
        for (int i = 0; i < words.length; i++) {
            if (words[i].equals("!")) {
                if (i + 5 < words.length ) {
                    if (words[i+3].equalsIgnoreCase("Is")
                            && words[i+5].equalsIgnoreCase("Nothing")) {
                        words[i] = "";
                        words[i+3] = "!=";
                        words[i+5] = "null";
                    }
                }
            }
            strLine += words[i];
        }
        return strLine.replaceAll("Is Nothing", "== null");
    }

    private String replaceNothing(String strLine) {
        if (strLine.contains("Nothing")) {
            strLine = strLine.replaceAll(" Nothing ", " null");
            strLine = strLine.replaceAll("\\(Nothing\\)", "(null)");
            strLine = strLine.replaceAll("\\(Nothing,", "(null,");
            strLine = strLine.replaceAll(", Nothing\\)", ", null)");
            strLine = strLine.replaceAll(" Nothing,", ", null,");
            // ", Nothing" or "= Nothing"
            if (strLine.endsWith(" Nothing"))
                strLine = strLine.substring(0, strLine.length() - 8) + " null";

        }
        return strLine;
    }

    private String replaceRedimSentence(String strLine) {
        if (!G.beginLike(strLine.trim().toLowerCase(),"redim "))
            return strLine;

        String array = "";
        String size = "";
        String[] words = G.split(strLine, "\t ");
        // posible sentences
        // 1 - redim variable(...)
        // 2 - redim preserve variable(...)
        //
        // not supported sentence
        if (words.length < 4) {
            return "/*TODO:** this redim sentence can't be translated " + strLine;
        }
        // 1
        else if (words.length < 7) {
            array = words[2];
            size = words[4];
            if (m_AddAuxFunctionsToClass) {
                m_addRedimAuxFunction = true;
                return "redim(" + array + ", " + size + ")";
            }
            else {
                m_addRedimAuxFunctionToG = m_UseGAuxFunctions;
                return "G.redim(" + array + ", " + size + ")";
            }
        }
        // 2
        else {
            array = words[4];
            size = words[6];
            if (m_AddAuxFunctionsToClass) {
                m_addRedimPreserveAuxFunction = true;
                return "redimPreserve(" + array + ", " + size + ")";
            }
            else {
                m_addRedimPreserveAuxFunctionToG = m_UseGAuxFunctions;
                return "G.redimPreserve(" + array + ", " + size + ")";
            }
        }
    }

    private String replaceWithSentence(String strLine) {

        // debug
        /*
        if (strLine.toLowerCase().contains("With m_Groups.Item(i)".toLowerCase())) {
            int i = 0;
        }
         /* 
         */
        // debug
        
        if (G.beginLike(strLine, "with ")) {
            m_withDeclaration = true;

            // first we have to get the variable
            // and then the type of it
            //
            int startComment = getStartComment(strLine);
            String workLine = strLine;
            String comments = "";
            if (startComment >= 0) {
                comments =  "; //" + workLine.substring(startComment);
                workLine = workLine.substring(0, startComment-1);
            }
            int i = workLine.toLowerCase().indexOf("with");
            IdentifierInfo info = null;
            String packageName = "";
            String type = "";
            String parent = "";
            String parentJavaName = "";
            boolean inWith = false;
            workLine = workLine.substring(i + 5).trim();
            boolean startWithPeriod = false;
            if (workLine.charAt(0) == '.') {
                startWithPeriod = true;
                if (workLine.length() > 1) {
                    workLine = workLine.substring(1);
                }
                else {
                    workLine = "";
                }
            }
            String[] words = G.split3(workLine,".");
            if (m_collWiths.size() > 0 && startWithPeriod) {
                parent = m_collWiths.get(m_collWiths.size()-1).dataType;
                parentJavaName = m_collWiths.get(m_collWiths.size()-1).getJavaName() + ".";
                inWith = true;
            }
            else {
                parent = "";
                parentJavaName = "";
            }
            for (i = 0; i < words.length; i++) {
                info = getIdentifierInfo(words[i], parent, !parent.isEmpty());
                if (info == null)
                    type = "";
                else if (info.isFunction)
                    type = info.function.getReturnType().dataType;
                else
                    type = info.variable.dataType;
                parent = type;
            }
            String parentWithCall = ""; // for example: "tBi." in "With tBI.bmiHeader"
            for (i = 0; i < words.length-1; i++) {
                parentWithCall += words[i] + ".";
            }

            String prefix = "";
            if (type.length() == 0) {
                type = "__TYPE_NOT_FOUND";
                prefix = "//*TODO:** can't found type for with block"
                            + newline
                            + getTabs()
                            + "//*"
                            + strLine
                            + newline
                            + getTabs();
            }

            Variable var = new Variable();
            var.setType(type);
            var.packageName = packageName;

            if (info == null) {
                var.setJavaName("w_" + var.dataType.substring(0, 1).toLowerCase()
                            + var.dataType.substring(1));

                if (inWith) {
                    strLine = prefix
                                + var.dataType
                                + " "
                                + var.getJavaName()
                                + " = "
                                + parentJavaName
                                + workLine
                                + comments;
                }
                else {
                    strLine = prefix
                                + var.dataType
                                + " "
                                + var.getJavaName()
                                + " = " + workLine
                                + comments;
                    m_inWith = true;
                }
            }
            else {
                if (info.isFunction) {
                    var.setJavaName("w_" + info.function.getVbName().substring(0, 1).toLowerCase()
                                + info.function.getVbName().substring(1));
                    String params = "";
                    int startParams = workLine.indexOf("(");
                    if (startParams >= 0) {
                        params = workLine.substring(startParams);
                    }
                    else {
                        params = "()";
                    }
                    if (inWith) {
                        strLine = prefix
                                    + var.dataType
                                    + " "
                                    + var.getJavaName()
                                    + " = "
                                    + parentJavaName
                                    + info.function.getJavaName()
                                    + params
                                    + comments;
                    }
                    else {
                        strLine = prefix
                                    + var.dataType
                                    + " "
                                    + var.getJavaName()
                                    + " = "
                                    + parentWithCall
                                    + info.function.getJavaName()
                                    + params
                                    + comments;
                        m_inWith = true;
                    }
                }
                else {
                    String arrayIndex = "";
                    if (info.variable.isArray) {
                        int startArrayIndex = workLine.indexOf("(");
                        if (startArrayIndex >= 0) {
                            arrayIndex = workLine.substring(startArrayIndex).replace("(","[").replace(")","]");
                        }
                    }
                    var.setJavaName(parentWithCall + info.variable.getJavaName() + arrayIndex);
                    strLine = "// " + strLine;
                    m_inWith = true;
                }
            }
            m_collWiths.add(var);
            m_functionVariables.add(var);
            
        }
        else {
            m_withDeclaration = false;
        }
        return strLine;
    }

    private String replaceRaiseEvent(String strLine) {
        if (G.beginLike(strLine, "RaiseEvent ")) {
            int i = strLine.toLowerCase().indexOf("raiseevent ") + 11;
            String call = strLine.substring(i);
            call = call.substring(0, 1).toLowerCase() + call.substring(1);
            if (!G.endLike(call, ")"))
                call += "()";
            String className = m_javaClassName + C_INTERFACE_POSTIFX;
            strLine = "Iterator listeners = m_listeners.iterator();"
                    + newline + getTabs() 
                    + "while(listeners.hasNext()) {"
                    + newline + getTabs() 
                    + "    ((" + className + ")listeners.next())." + call + ";"
                    + newline + getTabs() 
                    + "}";
        }
        return strLine;
    }

    private String replaceNewSentence(String strLine) {
        final String C_IDENTIFIER_FIRST_CHAR = "abcdefghijklmnopqrstuvwyxz";
        boolean newFound = false;
        String[] words = G.split2(strLine, "\t (),");
        strLine = "";
        for (int i = 0; i < words.length; i++) {
            if (words[i].equalsIgnoreCase("new")) {
                words[i] = "new";
                newFound = true;
            }
            else if (newFound) {
                if (C_IDENTIFIER_FIRST_CHAR.contains(words[i].substring(0,1).toLowerCase())) {
                    words[i] += "()";
                    newFound = false;
                }
                else if (!words[i].trim().isEmpty()) {
                    newFound = false;
                }
            }
            strLine += words[i];
        }
        return strLine;
    }

    private String replaceEndWithSentence(String strLine) {
        boolean isEndWith = false;
        if (strLine.equalsIgnoreCase("end with")) {
            isEndWith = true;
        }
        if (G.beginLike(strLine, "end with ")) {
            isEndWith = true;
        }
        m_endWithDeclaration = isEndWith;
        if (isEndWith) {
            String withName = "";
            if (m_collWiths.size() > 0) {
                withName = m_collWiths.get(m_collWiths.size()-1).getJavaName();
                m_collWiths.remove(m_collWiths.size()-1);
            }
            m_inWith = m_collWiths.size() > 0;
            return "// {end with: " + withName + "}";
        }
        else
            return strLine;
    }

    private IdentifierInfo getIdentifierInfo(String identifier, String className, boolean isField) {
        // - get the object from this class (member variables)
        // if the object is not found then
        // - get the object from the database (public variables)
        //      -- first objects in this package then objects in
        //         other packages in the order set in the vbp's
        //         reference list
        IdentifierInfo info = null;
        Variable var = getVariable(identifier, className, isField);
        if (var != null) {
            info = new IdentifierInfo();
            info.isFunction = false;
            info.variable = var;
        }
        else {
            Function function = getFunction(identifier, className);
            if (function != null) {
                info = new IdentifierInfo();
                info.isFunction = true;
                info.function = function;
            }
        }
        return info;
    }

    private String translateFunctionCall(String strLine) {

        // we will process with later
        //
        if (G.beginLike(strLine,"With ")) {
            return strLine;
        }

        // we will process raiseevent later
        //
        if (G.beginLike(strLine,"RaiseEvent ")) {
            return strLine;
        }

        if (G.beginLike(strLine,"Call ")) {
            strLine = strLine.substring(5);
        }

        // comments
        //
        if (G.beginLike(strLine,"// ")) {
            return strLine;
        }

        if (G.beginLike(strLine,"/* ")) {
            return strLine;
        }
        
        int startComment = getStartComment(strLine);
        String workLine = strLine;
        String comments = "";
        if (startComment >= 0) {
            comments =  "//" + workLine.substring(startComment);
            workLine = workLine.substring(0, startComment-1);
        }
        String[] words = getWordsFromSentence(workLine);
        if (words.length >= 2) {
            if (!words[0].equals("return")) {
                if (!words[0].equals("(") 
                        && !words[1].equals("(") 
                        && !words[2].equals("(")) {
                    if (!C_SEPARARTORS.contains("_" + words[2] + "_")) {
                        if (!isReservedWord(words[0])) {                                
                            strLine = words[0] + "(";
                            String params = "";
                            for (int i = 1; i < words.length; i++) {
                                params += words[i];
                            }
                            strLine += params.trim() + ")" + comments;
                        }
                    }
                }
                // special case when in vb we have a SUB call
                // with only one parameter sourronded by parentheses
                // like Col.Remove (Ctrl.Key)
                //
                else if (words[1].equals(" ") && words[2].equals("(")) {
                    if (!C_SEPARARTORS.contains("_" + words[2] + "_")) {
                        if (!isReservedWord(words[0])) {
                            strLine = words[0];
                            String params = "";
                            for (int i = 2; i < words.length; i++) {
                                params += words[i];
                            }
                            strLine += params.trim() + comments;
                        }
                    }
                }
            }
        }
        return strLine;
    }

    private boolean isReservedWord(String word) {
        if (m_translateToJava)
            return (C_RESERVED_WORDS.contains("_" + word.toLowerCase() + "_"));
        else {
            boolean rtn = C_RESERVED_WORDS.contains("_" + word.toLowerCase() + "_");
            if (!rtn)
                rtn = word.equals("Length");
            return rtn;
        }
    }

    private String replaceMemberVariables(String strLine) {
        boolean found = false;
        String rtn = "";
        String[] words = getWordsFromSentence(strLine);

        for (int i = 0; i < words.length; i++) {
            found = false;
            for (int j = 0; j < m_memberVariables.size(); j++) {
                /*System.out.println(m_memberVariables.get(j)
                        + C_TAB + words[i]
                        );*/
                if (words[i].equalsIgnoreCase(m_memberVariables.get(j).getJavaName())) {
                    rtn += m_memberVariables.get(j).getJavaName();
                    found = true;
                    break;
                }
            }
            if (!found) {
                rtn += words[i];
            }
        }
        return rtn;
    }

    private String replaceFunctionVariables(String strLine) {
        boolean found = false;
        String rtn = "";
        String[] words = getWordsFromSentence(strLine);

        for (int i = 0; i < words.length; i++) {
            found = false;
            for (int j = 0; j < m_functionVariables.size(); j++) {
                /*System.out.println(m_functionVariables.get(j)
                        + C_TAB + words[i]
                        );*/
                if (words[i].equalsIgnoreCase(m_functionVariables.get(j).getJavaName())) {
                    rtn += m_functionVariables.get(j).getJavaName();
                    found = true;
                    break;
                }
            }
            if (!found) {
                rtn += words[i];
            }
        }
        return rtn;
    }

    private String putParentheses(String strLine) {
        int openParentheses = 0;
        String rtn = "";
        String source = "";

        String[] words = G.split(strLine);

        for (int i = 0; i < words.length; i++) {

            if (words[i].equals("(")) {
                if (openParentheses == 0) {
                    if (!source.isEmpty()) {
                        rtn += putParenthesesAux(source);
                        source = "";
                    }
                }
                else {
                    source += words[i];
                }
                openParentheses++;
            }
            else if (words[i].equals(")")) {
                openParentheses--;
                if (openParentheses == 0) {
                    rtn += "(" + putParentheses(source) + ")";
                    source = "";
                }
                else {
                    source += words[i];
                }
            }
            else {
                source += words[i];
            }
        }
        if (!source.isEmpty()) {
            rtn += putParenthesesAux(source);
        }
        return rtn;
    }

    private String putParenthesesAux(String strLine) {
        String rtn = "";
        String source = "";
        String[] words = G.split(strLine, ",");
        for (int i = 0; i < words.length; i++) {
            if (words[i].equals(",")) {
                if (!source.isEmpty()) {
                    rtn += putParenthesesAux2(source) + ",";
                    source = "";
                }
                else
                    rtn += ",";
            }
            else {
                source += words[i];
            }
        }
        if (!source.isEmpty()) {
            rtn += putParenthesesAux2(source);
        }
        return rtn;
    }
    
    private String putParenthesesAux2(String strLine) {
        if (strLine.contains("&")) {
            String rtn = "";
            String[] words = G.split(strLine, "&");
            for (int i = 0; i < words.length; i++) {
                if (words[i].trim().isEmpty()) {
                    rtn += words[i];
                }
                else if (!"+-*/".contains(words[i].trim().substring(0, 1))) {
                    if(containsMathOperators(words[i])) {
                        if (words[i].startsWith(" "))
                            rtn += " ";
                        rtn += "(" + words[i].trim() + ")";
                        if (words[i].endsWith(" "))
                            rtn += " ";
                    }
                    else {
                        rtn += words[i];
                    }
                }
            }
            return rtn;
        }
        else
            return strLine;
    }
    private boolean containsMathOperators(String strLine) {
        strLine = removeLiterals(strLine);
        if(strLine.contains("+")) {
            return true;
        }
        else if(strLine.contains("-")) {
            return true;
        }
        else if(strLine.contains("*")) {
            return true;
        }
        else if(strLine.contains("/")) {
            return true;
        }
        else
            return false;

    }
    private String removeLiterals(String strLine) {
        boolean openLiterals = false;
        String rtn = "";
        for (int i = 0; i < strLine.length(); i++) {
            String c = strLine.substring(0, 1);
            if (c.equals("\""))
                openLiterals = !openLiterals;
            else if (!openLiterals) 
                rtn += c;
        }
        return rtn;
    }

    private String replaceAmpersand(String strLine) {
        if (strLine.contains("&")) {
            // we need to put parentheses to enforce
            // operator precedence
            //
            strLine = putParentheses(strLine);

            return replaceAmpersandAux(strLine);
        }
        else {
            return strLine;
        }
    }

    private String replaceAmpersandAux(String strLine) {
        int openParentheses = 0;
        boolean ampFound = false;
        String rtn = "";
        String source = "";
        String functionCall = "";
        String[] words = G.split(strLine);

        for (int i = 0; i < words.length; i++) {

            // all the code between parentheses is
            // saved in the source variable and then 
            // replaceAmpersandAux is called pasing 
            // source to process &
            //
            if (words[i].equals("(")) {
                openParentheses++;
            }
            else if (words[i].equals(")")) {
                openParentheses--;
                // if we faund the closing parentheses
                //
                if (openParentheses == 0) {
                    // if before the parent
                    //
                    if (ampFound) {
                        rtn += getCastToString(functionCall 
                                + "(" + replaceAmpersandAux(source) + ")");
                        ampFound = false;
                    }
                    else {
                        if (i < words.length-1) {
                            // if the next is & we have something like this:
                            // (i+2)& " row"
                            
                            // we need to cast to string source
                            //
                            if (words[i+1].equals("&")) {
                                rtn += getCastToString(functionCall 
                                        + "(" + replaceAmpersandAux(source) + ")");
                            }
                            else {
                                // if the next is & we have something like this:
                                // (i+2) & " row"
                                // we need to cast to string source
                                //
                                if (i < words.length-2) {
                                    if (words[i+2].equals("&")) {
                                        rtn += getCastToString(functionCall 
                                                + "(" + replaceAmpersandAux(source) + ")");
                                    }
                                    // we only need to call replaceAmpersandAux
                                    // to translate source
                                    //
                                    else {
                                        rtn += functionCall + "(" 
                                                + replaceAmpersandAux(source) + ")";
                                    }
                                }
                                // we only need to call replaceAmpersandAux
                                // to translate source
                                //
                                else {
                                    rtn += functionCall + "(" 
                                            + replaceAmpersandAux(source) + ")";
                                }
                            }
                        }
                        // we only need to call replaceAmpersandAux
                        // to translate source because there is no more words
                        // to translate
                        //
                        else {
                            rtn += functionCall + "(" 
                                    + replaceAmpersandAux(source) + ")";
                        }
                        //---------------------------------------------------
                         
                    }
                    source = "";
                    functionCall = "";
                }
            }
            // we need to put all the code sourronded by parantheses in source
            //
            else if (openParentheses > 0) {
                source += words[i];
            }
            else {
                // replace & with + and set the flag on
                // to process the next word
                //
                if (words[i].equals("&")) {
                    rtn += "+";
                    ampFound = true;
                }
                else {
                    if (ampFound) {
                        if (words[i].equals(" "))
                            rtn += " ";
                        else {
                            if (i+1 < words.length) {
                                if (words[i+1].equals("(")) {
                                    functionCall = words[i];
                                }
                                else {
                                    // we need to cast to string this word
                                    // because the previous was an &
                                    //
                                    rtn += getCastToString(words[i]);
                                    ampFound = false;                                
                                }
                            }
                            else {
                                // we need to cast to string this word
                                // because the previous was an &
                                //
                                rtn += getCastToString(words[i]);
                                ampFound = false;
                            }
                        }
                    }
                    else {
                        // if there is more words after this
                        //
                        if (i < words.length-1) {
                            // if the next word is an &
                            //
                            if (words[i+1].equals("&")) {
                                // we need to cast to string this word
                                //
                                rtn += getCastToString(words[i]);
                            }
                            // if the next word is an open parentheses
                            // we save this word in source to 
                            // process this when all the code sourronded
                            // by parentheses will be translated
                            //
                            else if (words[i+1].equals("(")){
                                functionCall = words[i]; 
                            }
                            else {
                                if (i < words.length-2) {
                                    // we need to cast to string this word
                                    //
                                    if (words[i+2].equals("&")) {
                                        rtn += getCastToString(words[i]);
                                    }
                                    else {
                                        rtn += words[i];
                                    }
                                }
                                else {
                                    rtn += words[i];
                                }
                            }
                        }
                        else {
                            rtn += words[i];
                        }
                    }
                }
            }
        }
        return rtn;
    }

    private String translateVbOperators(String strLine) {
        boolean literalFlag = false;
        boolean previousWasNot = false;
        boolean previousWasParentheses = false;
        boolean isFirstWord = true;
        String javaSentence = "";
        String comments = "";

        int startComment = getStartComment(strLine);
        if (startComment >= 0) {
            comments =  "//" + strLine.substring(startComment);
            strLine = strLine.substring(0, startComment-1);
        }

        String[] words = G.splitSpace(strLine);//strLine.split("\\s+");
        
        // we don't have to replace the first = in an asignment statetment
        //
        int k = 0;
        if (words.length > 2) {
            if (words[1].equals("="))
                k = 2;
        }
        
        for (int i = 0; i < words.length; i++) {
            // we have to translate "or", "and", and "not"

            for (int j = 0; j < words[i].length(); j++) {
                if (words[i].charAt(j) == '"') {
                    literalFlag = !literalFlag;
                }
            }

            if (literalFlag) {
                javaSentence += " " + words[i];
            }
            else {
                if (i > k) {
                    if (words[i].equalsIgnoreCase("and")) {
                        javaSentence += " &&";
                    }
                    else if (words[i].equalsIgnoreCase(")and")) {
                        javaSentence += ") &&";
                    }
                    else if (words[i].equalsIgnoreCase("and(")) {
                        javaSentence += " && (";
                    }
                    else if (words[i].equalsIgnoreCase("or")) {
                        javaSentence += " ||";
                    }
                    else if (words[i].equalsIgnoreCase(")or")) {
                        javaSentence += ") ||";
                    }
                    else if (words[i].equalsIgnoreCase("or(")) {
                        javaSentence += " || (";
                    }
                    else if (words[i].equalsIgnoreCase("<>")) {
                        javaSentence += " !=";
                    }
                    else if (words[i].equalsIgnoreCase("not")) {
                        javaSentence += " !";
                    }
                    else if (words[i].equals("(")) {
                        if (previousWasNot)
                            javaSentence += "(";
                        else
                            javaSentence += " (";
                    }
                    else if (words[i].equals(")")) {
                        javaSentence += ")";
                    }
                    else if (words[i].equalsIgnoreCase("=")) {
                        javaSentence += " ==";
                    }
                    else {
                        if (isFirstWord) {
                            javaSentence += words[i];
                            isFirstWord = false;
                        }
                        else if (previousWasNot || previousWasParentheses) {
                            javaSentence += words[i];
                        }
                        else {
                            javaSentence += " " + words[i];
                        }
                    }

                    // flags
                    //
                    if (words[i].equalsIgnoreCase("not")) {
                        previousWasNot = true;
                    }
                    else {
                        previousWasNot = false;
                    }

                    if (words[i].charAt(words[i].length()-1) == '(') {
                        previousWasParentheses = true;
                    }
                    else {
                        previousWasParentheses = false;
                    }
                }
                else {
                    if (isFirstWord) {
                        javaSentence += words[i];
                        isFirstWord = false;
                    }
                    else {
                        javaSentence += " " + words[i];
                    }
                }
            }
        }
        return javaSentence + comments + newline;
    }
    
    private String replaceStringComparison(String strLine, String operator) {
        boolean equalsFound = false;
        boolean innerEqualFound = false;
        int openParentheses = 0;
        String innerParentheses = "";
        String firstOperand = "";
        String secondOperand = "";
        String rtn = "";
        String[] words = getWordsFromSentence(strLine);

        // first we have to serch for the operands
        //
        for (int i = 0; i < words.length; i++) {
            if (equalsFound) {
                // if we are sourronded by parentheses
                //
                if (openParentheses > 0) {
                    if (words[i].equals(")")) {
                        openParentheses--;
                        if (openParentheses == 0) {
                            if (innerEqualFound) {
                                innerEqualFound = false;
                                innerParentheses = replaceStringComparison(
                                                        innerParentheses,
                                                        operator);
                            }
                            secondOperand += "(" + G.ltrim(innerParentheses) + ")";
                            innerParentheses = "";
                        }
                        else
                            innerParentheses += ")";
                    }
                    else {
                        if (words[i].equals("(")) {
                            openParentheses++;
                        } else if (words[i].equals(operator)) {
                            innerEqualFound = true;
                        }
                        innerParentheses += words[i];
                    }
                }
                // there isn't any open parentheses
                //
                else {
                    if (words[i].equals("(")) {
                        openParentheses = 1;
                        innerParentheses = "";
                    }
                    else if (words[i].equals("&&")) {
                        rtn += processEqualsSentence(
                                    firstOperand,
                                    secondOperand,
                                    operator) + " &&";
                        equalsFound = false;
                        firstOperand = "";
                        secondOperand = "";
                    }
                    else if (words[i].equals("||")) {
                        rtn += processEqualsSentence(
                                    firstOperand,
                                    secondOperand,
                                    operator) + " ||";
                        equalsFound = false;
                        firstOperand = "";
                        secondOperand = "";
                    }
                    else
                        secondOperand += words[i];
                }
            }
            else {
                // if we are sourronded by parentheses
                //
                if (openParentheses > 0) {
                    if (words[i].equals(")")) {
                        openParentheses--;
                        if (openParentheses == 0) {
                            if (innerEqualFound) {
                                innerEqualFound = false;
                                innerParentheses = replaceStringComparison(
                                                        innerParentheses,
                                                        operator);
                            }
                            firstOperand += "(" + G.ltrim(innerParentheses) + ")";
                            innerParentheses = "";
                        }
                        else
                            innerParentheses += ")";
                    }
                    else {
                        if (words[i].equals("(")) {
                            openParentheses++;
                        } else if (words[i].equals(operator)) {
                            innerEqualFound = true;
                        }
                        innerParentheses += words[i];
                    }
                }

                // there isn't any open parentheses
                //
                else {
                    // if we found an left (open) parentheses
                    //
                    if (words[i].equals("(")) {
                        openParentheses = 1;
                        innerParentheses = "";
                    }
                    else if (words[i].equals(operator)) {
                        equalsFound = true;
                        innerParentheses = "";
                    }
                    else if (words[i].equals("&&")) {
                        rtn += firstOperand + "&&";
                        firstOperand = "";
                    }
                    else if (words[i].equals("||")) {
                        rtn += firstOperand + "||";
                        firstOperand = "";
                    }
                    else if (words[i].equals("=")) {
                        rtn += firstOperand + "=";
                        firstOperand = "";
                    }
                    else
                        firstOperand += words[i];
                }
            }
        }

        if (!firstOperand.isEmpty()) {
            if (!secondOperand.isEmpty())
                rtn += processEqualsSentence(
                            firstOperand,
                            secondOperand,
                            operator);
            else
                rtn += firstOperand;
        }
        return rtn;
    }
    
    private String replaceStringCompareNullString(String expression) {
        return expression.replace(".equals(\"\")", ".isEmpty()");
    }

    private String replaceTypeOfSentence(String expression) {

        expression = G.ltrimTab(expression);
        
        if (containsTypeOf(expression)) {

            boolean typeOfFound = false;
            boolean firstSpaceFound = false;
            boolean secondSpaceFound = false;
            int openParentheses = 0;
            String[] words = G.split(expression);
            String params = "";
            expression = "";           

            for (int i = 0; i < words.length; i++) {
                if (typeOfFound) {
                    if (words[i].equals("(")) {
                        openParentheses++;
                        params += words[i];
                    }
                    // look for a close parentheses without an open parentheses
                    else if (words[i].equals(")")) {
                        openParentheses--;
                        params += words[i];
                    }
                    else if (openParentheses == 0) {
                        if (firstSpaceFound) {
                            if (secondSpaceFound) {
                                if (words[i].equalsIgnoreCase("is")) {
                                    if (m_translateToJava)
                                        expression += params + " instanceOf ";
                                    else
                                        expression += params + ".GetType() ==";
                                    typeOfFound = false;
                                    params = "";
                                }
                                else {
                                    params = params.trim() + words[i];
                                }
                            }
                            else if (words[i].equals(" ")) {
                                secondSpaceFound = true;
                            }
                            else {
                                params += words[i];
                            }
                        }
                        else if (words[i].equals(" ")) {
                            firstSpaceFound = true;
                        }
                        else {
                            params += words[i];
                        }
                    }
                    else {
                        params += words[i];
                    }
                }
                else {
                    if (words[i].equalsIgnoreCase("typeof")) {
                        typeOfFound = true;
                    }
                    else {
                        expression += words[i];
                    }
                }
            }
        }
        return expression.trim();
    }
    
    private String replaceMidSentence(String expression) {

        expression = G.ltrimTab(expression);

        if (containsMid(expression)) {

            boolean midFound = false;
            int openParentheses = 0;
            String[] words = G.split(expression);
            String params = "";
            expression = "";

            for (int i = 0; i < words.length; i++) {
                if (midFound) {
                    if (words[i].equals("(")) {
                        openParentheses++;
                        if (openParentheses > 1) {
                            params += words[i];
                        }
                    }
                    // look for a close parentheses without an open parentheses
                    else if (words[i].equals(")")) {
                        openParentheses--;
                        if (openParentheses == 0) {
                            if (containsMid(params)) {
                                params = replaceMidSentence(params);
                            }
                            String[] vparams = G.split(params);
                            String identifier = "";
                            String start = "";
                            String end = "";

                            int colons = 0;
                            identifier = "";
                            for (int t = 0; t < vparams.length; t++) {
                                if (vparams[t].equals(",")) {
                                    colons++;
                                }
                                else {

                                    if (colons == 0) {
                                        identifier += vparams[t];
                                    }
                                    else if (colons == 1) {
                                        start += vparams[t];
                                    }
                                    else if (colons == 2) {
                                        end += vparams[t];
                                    }
                                    else {
                                        showError("Unexpected colon found in Mid function's params: " + params);
                                    }
                                }
                            }
                            // identifier can be a complex expresion
                            // like ' "an string plus" + a_var '
                            //
                            if (G.contains(identifier, " ")) {
                                identifier = "(" + identifier + ")";
                            }
                            if (m_translateToJava)
                                expression += identifier + ".substring(" + start.trim();
                            else
                                expression += identifier + ".Substring(" + start.trim();
                            if (!end.isEmpty()) {
                                expression += ", " + end.trim() + ")";
                            }
                            else {
                                expression += ")";
                            }
                            midFound = false;
                            params = "";
                        }
                        else {
                            params = params.trim() + words[i];
                        }
                    }
                    else {
                        params += words[i];
                    }
                }
                else {
                    if (words[i].equalsIgnoreCase("mid")) {
                        midFound = true;
                    }
                    else if (words[i].equalsIgnoreCase("mid$")) {
                        midFound = true;
                    }
                    else if (G.beginLike(words[i],"mid(")) {
                        expression += replaceMidSentence(words[i]);
                    }
                    else if (G.beginLike(words[i],"mid$(")) {
                        expression += replaceMidSentence(words[i]);
                    }
                    else if (containsMid(words[i])) {
                        expression += replaceMidSentence(words[i]);
                    }
                    else {
                        expression += words[i];
                    }
                }
            }
        }
        return expression.trim();
    }

    private String replaceLeftSentence(String expression) {

        expression = G.ltrimTab(expression);

        if (containsLeft(expression)) {

            boolean leftFound = false;
            int openParentheses = 0;
            String[] words = G.split(expression);
            String params = "";
            expression = "";

            for (int i = 0; i < words.length; i++) {
                if (leftFound) {
                    if (words[i].equals("(")) {
                        openParentheses++;
                        if (openParentheses > 1) {
                            params += words[i];
                        }
                    }
                    // look for a close parentheses without an open parentheses
                    else if (words[i].equals(")")) {
                        openParentheses--;
                        if (openParentheses == 0) {
                            if (containsLeft(params)) {
                                params = replaceLeftSentence(params);
                            }
                            String[] vparams = G.split(params);
                            String identifier = "";
                            String length = "";

                            int colons = 0;
                            identifier = "";
                            for (int t = 0; t < vparams.length; t++) {
                                if (vparams[t].equals(",")) {
                                    colons++;
                                }
                                else {

                                    if (colons == 0) {
                                        identifier += vparams[t];
                                    }
                                    else if (colons == 1) {
                                        length += vparams[t];
                                    }
                                    else {
                                        showError("Unexpected colon found in Left function's params: " + params);
                                    }
                                }
                            }
                            // identifier can be a complex expresion
                            // like ' "an string plus" + a_var '
                            //
                            if (G.contains(identifier, " ")) {
                                identifier = "(" + identifier + ")";
                            }
                            if (m_translateToJava)
                                expression += identifier
                                                + ".substring(0, " + length.trim()+ ")";
                            else
                                expression += identifier
                                                + ".Substring(0, " + length.trim()+ ")";
                            leftFound = false;
                            params = "";
                        }
                        else {
                            params = params.trim() + words[i];
                        }
                    }
                    else {
                        params += words[i];
                    }
                }
                else {
                    if (words[i].equalsIgnoreCase("left")) {
                        leftFound = true;
                    }
                    else if (words[i].equalsIgnoreCase("left$")) {
                        leftFound = true;
                    }
                    else if (G.beginLike(words[i],"left(")) {
                        expression += replaceLeftSentence(words[i]);
                    }
                    else if (G.beginLike(words[i],"left$(")) {
                        expression += replaceLeftSentence(words[i]);
                    }
                    else if (containsLeft(words[i])) {
                        expression += replaceLeftSentence(words[i]);
                    }
                    else {
                        expression += words[i];
                    }
                }
            }
        }
        return expression.trim();
    }

    private String replaceRightSentence(String expression) {

        expression = G.ltrimTab(expression);

        if (containsRight(expression)) {

            boolean rightFound = false;
            int openParentheses = 0;
            String[] words = G.split(expression);
            String params = "";
            expression = "";

            for (int i = 0; i < words.length; i++) {
                if (rightFound) {
                    if (words[i].equals("(")) {
                        openParentheses++;
                        if (openParentheses > 1) {
                            params += words[i];
                        }
                    }
                    // look for a close parentheses without an open parentheses
                    else if (words[i].equals(")")) {
                        openParentheses--;
                        if (openParentheses == 0) {
                            if (containsRight(params)) {
                                params = replaceRightSentence(params);
                            }
                            String[] vparams = G.split(params);
                            String identifier = "";
                            String lenght = "";

                            int colons = 0;
                            identifier = "";
                            for (int t = 0; t < vparams.length; t++) {
                                if (vparams[t].equals(",")) {
                                    colons++;
                                }
                                else {

                                    if (colons == 0) {
                                        identifier += vparams[t];
                                    }
                                    else if (colons == 1) {
                                        lenght += vparams[t];
                                    }
                                    else {
                                        showError("Unexpected colon found in Right function's params: " + params);
                                    }
                                }
                            }
                            // identifier can be a complex expresion
                            // like ' "an string plus" + a_var '
                            //
                            if (G.contains(identifier, " ")) {
                                identifier = "(" + identifier + ")";
                            }
                            if (m_translateToJava)
                                expression += identifier
                                                + ".substring(" + identifier
                                                + ".length() - " + lenght.trim() + ")";
                            else
                                expression += identifier
                                                + ".Substring(" + identifier
                                                + ".Length - " + lenght.trim() + ")";
                            rightFound = false;
                            params = "";
                        }
                        else {
                            params = params.trim() + words[i];
                        }
                    }
                    else {
                        params += words[i];
                    }
                }
                else {
                    if (words[i].equalsIgnoreCase("right")) {
                        rightFound = true;
                    }
                    else if (words[i].equalsIgnoreCase("right$")) {
                        rightFound = true;
                    }
                    else if (G.beginLike(words[i],"right(")) {
                        expression += replaceRightSentence(words[i]);
                    }
                    else if (G.beginLike(words[i],"right$(")) {
                        expression += replaceRightSentence(words[i]);
                    }
                    else if (containsLeft(words[i])) {
                        expression += replaceRightSentence(words[i]);
                    }
                    else {
                        expression += words[i];
                    }
                }
            }
        }
        return expression.trim();
    }

    private String replaceLCaseSentence(String expression) {

        expression = G.ltrimTab(expression);

        if (containsLCase(expression)) {

            boolean lcaseFound = false;
            int openParentheses = 0;
            String[] words = G.split(expression);
            String params = "";
            expression = "";
            lcaseFound = false;

            for (int i = 0; i < words.length; i++) {
                if (lcaseFound) {
                    if (words[i].equals("(")) {
                        openParentheses++;
                        if (openParentheses > 1) {
                            params += words[i];
                        }
                    }
                    // look for a close parentheses without an open parentheses
                    else if (words[i].equals(")")) {
                        openParentheses--;
                        if (openParentheses == 0) {
                            if (containsLCase(params)) {
                                params = replaceLCaseSentence(params);
                            }
                            String[] vparams = G.split(params);
                            String identifier = "";

                            int colons = 0;
                            identifier = "";
                            for (int t = 0; t < vparams.length; t++) {
                                if (vparams[t].equals(",")) {
                                    colons++;
                                }
                                else {

                                    if (colons == 0) {
                                        identifier += vparams[t];
                                    }
                                    else {
                                        showError("Unexpected colon found in LCase function's params: " + params);
                                    }
                                }
                            }
                            // identifier can be a complex expresion
                            // like ' "an string plus" + a_var '
                            //
                            if (G.contains(identifier, " ")) {
                                identifier = "(" + identifier + ")";
                            }
                            if (m_translateToJava)
                                expression += identifier
                                                + ".toLowerCase()";
                            else
                                expression += identifier
                                                + ".ToLower()";                                
                            lcaseFound = false;
                            params = "";
                        }
                        else {
                            params = params.trim() + words[i];
                        }
                    }
                    else {
                        params += words[i];
                    }
                }
                else {
                    if (words[i].equalsIgnoreCase("lcase")) {
                        lcaseFound = true;
                    }
                    else if (words[i].equalsIgnoreCase("lcase$")) {
                        lcaseFound = true;
                    }
                    else if (G.beginLike(words[i],"lcase(")) {
                        expression += replaceLCaseSentence(words[i]);
                    }
                    else if (G.beginLike(words[i],"lcase$(")) {
                        expression += replaceLCaseSentence(words[i]);
                    }
                    else if (containsLCase(words[i])) {
                        expression += replaceLCaseSentence(words[i]);
                    }
                    else {
                        expression += words[i];
                    }
                }
            }
        }
        return expression.trim();
    }

    private String replaceUCaseSentence(String expression) {

        expression = G.ltrimTab(expression);

        if (containsUCase(expression)) {

            boolean ucaseFound = false;
            int openParentheses = 0;
            String[] words = G.split(expression);
            String params = "";
            expression = "";

            for (int i = 0; i < words.length; i++) {
                if (ucaseFound) {
                    if (words[i].equals("(")) {
                        openParentheses++;
                        if (openParentheses > 1) {
                            params += words[i];
                        }
                    }
                    // look for a close parentheses without an open parentheses
                    else if (words[i].equals(")")) {
                        openParentheses--;
                        if (openParentheses == 0) {
                            if (containsUCase(params)) {
                                params = replaceUCaseSentence(params);
                            }
                            String[] vparams = G.split(params);
                            String identifier = "";

                            int colons = 0;
                            identifier = "";
                            for (int t = 0; t < vparams.length; t++) {
                                if (vparams[t].equals(",")) {
                                    colons++;
                                }
                                else {

                                    if (colons == 0) {
                                        identifier += vparams[t];
                                    }
                                    else {
                                        showError("Unexpected colon found in UCase function's params: " + params);
                                    }
                                }
                            }
                            // identifier can be a complex expresion
                            // like ' "an string plus" + a_var '
                            //
                            if (G.contains(identifier, " ")) {
                                identifier = "(" + identifier + ")";
                            }
                            if (m_translateToJava)
                                expression += identifier
                                                + ".toUpperCase()";
                            else
                                expression += identifier
                                                + ".ToUpper()";
                            ucaseFound = false;
                            params = "";
                        }
                        else {
                            params = params.trim() + words[i];
                        }
                    }
                    else {
                        params += words[i];
                    }
                }
                else {
                    if (words[i].equalsIgnoreCase("ucase")) {
                        ucaseFound = true;
                    }
                    else if (words[i].equalsIgnoreCase("ucase$")) {
                        ucaseFound = true;
                    }
                    else if (G.beginLike(words[i],"ucase(")) {
                        expression += replaceUCaseSentence(words[i]);
                    }
                    else if (G.beginLike(words[i],"ucase$(")) {
                        expression += replaceUCaseSentence(words[i]);
                    }
                    else if (containsUCase(words[i])) {
                        expression += replaceUCaseSentence(words[i]);
                    }
                    else {
                        expression += words[i];
                    }
                }
            }
        }
        return expression.trim();
    }

    private String replaceLenSentence(String expression) {

        expression = G.ltrimTab(expression);

        if (containsLen(expression)) {

            boolean lenFound = false;
            int openParentheses = 0;
            String[] words = G.split(expression);
            String params = "";
            expression = "";

            for (int i = 0; i < words.length; i++) {
                if (lenFound) {
                    if (words[i].equals("(")) {
                        openParentheses++;
                        if (openParentheses > 1) {
                            params += words[i];
                        }
                    }
                    // look for a close parentheses without an open parentheses
                    else if (words[i].equals(")")) {
                        openParentheses--;
                        if (openParentheses == 0) {
                            if (containsLen(params)) {
                                params = replaceLenSentence(params);
                            }
                            String[] vparams = G.split(params);
                            String identifier = "";

                            int colons = 0;
                            identifier = "";
                            for (int t = 0; t < vparams.length; t++) {
                                if (vparams[t].equals(",")) {
                                    colons++;
                                }
                                else {

                                    if (colons == 0) {
                                        identifier += vparams[t];
                                    }
                                    else {
                                        showError("Unexpected colon found in Len function's params: " + params);
                                    }
                                }
                            }
                            // identifier can be a complex expresion
                            // like ' "an string plus" + a_var '
                            //
                            if (G.contains(identifier, " ")) {
                                identifier = "(" + identifier + ")";
                            }
                            if (m_translateToJava)
                                expression += identifier + ".length()";
                            else
                                expression += identifier + ".Length";
                            lenFound = false;
                            params = "";
                        }
                        else {
                            params = params.trim() + words[i];
                        }
                    }
                    else {
                        params += words[i];
                    }
                }
                else {
                    if (words[i].equalsIgnoreCase("len")) {
                        lenFound = true;
                    }
                    else if (G.beginLike(words[i],"len(")) {
                        expression += replaceLenSentence(words[i]);
                    }
                    else if (words[i].toLowerCase().contains(" len(")) {
                        expression += replaceLenSentence(words[i]);
                    }
                    else if (words[i].toLowerCase().contains("(len(")) {
                        expression += replaceLenSentence(words[i]);
                    }
                    else {
                        expression += words[i];
                    }
                }
            }
        }
        return expression.trim();
    }

    private String replaceInStrSentence(String expression) {

        expression = G.ltrimTab(expression);

        if (containsInStr(expression)) {

            boolean inStrFound = false;
            int openParentheses = 0;
            String[] words = G.split(expression);
            String params = "";
            expression = "";

            for (int i = 0; i < words.length; i++) {
                if (inStrFound) {
                    if (words[i].equals("(")) {
                        openParentheses++;
                        if (openParentheses > 1) {
                            params += words[i];
                        }
                    }
                    // look for a close parentheses without an open parentheses
                    else if (words[i].equals(")")) {
                        openParentheses--;
                        if (openParentheses == 0) {
                            if (containsInStr(params)) {
                                params = replaceInStrSentence(params);
                            }
                            String[] vparams = G.split(params);

                            String start = "";
                            String source = "";
                            String toSearch = "";
                            String compareType = "";

                            String param1 = "";
                            String param2 = "";
                            String param3 = "";
                            String param4 = "";

                            int colons = 0;
                            source = "";
                            for (int t = 0; t < vparams.length; t++) {
                                if (vparams[t].equals(",")) {
                                    colons++;
                                }
                                else {

                                    if (colons == 0) {
                                        param1 += vparams[t];
                                    }
                                    else if (colons == 1) {
                                        param2 += vparams[t];
                                    }
                                    else if (colons == 2) {
                                        param3 += vparams[t];
                                    }
                                    else if (colons == 3) {
                                        param4 += vparams[t];
                                    }
                                    else {
                                        showError("Unexpected colon found in InStr function's params: " + params);
                                    }
                                }
                            }
                            // there are 4 param combinations
                            // 1 if we have 4 params there are: start, source, toSearch, compareType
                            // 2 if we have 3 params it could be:
                            //        2.a if param1 is numeric: start, source, toSearch
                            //        2.b if params isn't numeric: source, toSearch, compareType
                            // 3 if we have 2 params there are: source, toSearch
                            // 4 if we have less than 2 params there is an error :P
                            //

                            // 4 if we have less than 2 params there is an error :P
                            //
                            param1 = param1.trim();
                            param2 = param2.trim();
                            param3 = param3.trim();
                            param4 = param4.trim();
                            if (param2.isEmpty()) {
                                G.showInfo("Wrong number of params in InStr function call. At least there must be two params: " + params);
                            }
                            else {
                                // 1 if we have 4 params there are: start, source, toSearch, compareType
                                //
                                if (!param4.isEmpty()) {
                                    start = param1;
                                    source = param2;
                                    toSearch = param3;
                                    compareType = param4;
                                    if (compareType.equals("0")
                                            || compareType.equals("vbBinaryCompare")) {
                                        expression += getSource(source)
                                                        + ".indexOf(" + toSearch
                                                        + ", " + start.trim() + ")";
                                    }
                                    else { // 1 or vbTextCompare
                                        if (isStringIdentifier(toSearch)) {
                                            if (m_translateToJava)
                                                expression += getSource(source)
                                                            + ".toLowerCase().indexOf("
                                                            + toSearch + ".toLowerCase(), "
                                                            + start.trim() + ")";
                                            else
                                                expression += getSource(source)
                                                            + ".ToLower().IndexOf("
                                                            + toSearch + ".ToLower(), "
                                                            + start.trim() + ")";                                                
                                        }
                                        else {
                                            if (m_translateToJava)
                                                expression += getSource(source)
                                                            + ".toLowerCase().indexOf(String.valueOf("
                                                            + toSearch + ").toLowerCase(), "
                                                            + start.trim() + ")";
                                            else
                                                expression += getSource(source)
                                                            + ".ToLower().IndexOf(("
                                                            + toSearch + ").ToString().ToLower(), "
                                                            + start.trim() + ")";
                                        }
                                    }
                                }
                                // 2 if we have 3 params it could be:
                                //        2.a if param1 is numeric: start, source, toSearch
                                //        2.b if params isn't numeric: source, toSearch, compareType
                                //
                                else if(!param3.isEmpty()) {
                                    // 2.a if param1 is numeric: start, source, toSearch
                                    //
                                    if (isNumericIdentifier(param1)) {
                                        start = param1;
                                        source = param2;
                                        toSearch = param3;
                                        expression += getSource(source)
                                                    + ".indexOf("
                                                    + toSearch + ", "
                                                    + start.trim() + ")";
                                    }
                                    else {
                                        source = param1;
                                        toSearch = param2;
                                        compareType = param3;
                                        if (compareType.equals("0")
                                                || compareType.equals("vbBinaryCompare")) {
                                            expression += getSource(source)
                                                            + ".indexOf(" + toSearch + ")";
                                        }
                                        else { // 1 or vbTextCompare
                                            if (isStringIdentifier(toSearch)) {
                                                if (m_translateToJava)
                                                    expression += getSource(source)
                                                                + ".toLowerCase().indexOf("
                                                                + toSearch + ".toLowerCase())";
                                                else
                                                    expression += getSource(source)
                                                                + ".ToLower().IndexOf("
                                                                + toSearch + ".ToLower())";                                                    
                                            }
                                            else {
                                                if (m_translateToJava)
                                                    expression += getSource(source)
                                                                + ".toLowerCase().indexOf(String.valueOf("
                                                                + toSearch + ").toLowerCase())";
                                                else
                                                    expression += getSource(source)
                                                                + ".ToLower().IndexOf(("
                                                                + toSearch + ").ToString().ToLower())";
                                            }
                                        }
                                    }
                                }
                                // 3 if we have 2 params there are: source, toSearch
                                //
                                else {
                                    source = param1;
                                    toSearch = param2;
                                    expression += getSource(source)
                                                + ".indexOf("
                                                + toSearch + ")";
                                }
                            }
                            inStrFound = false;
                            params = "";
                        }
                        else {
                            params = params.trim() + words[i];
                        }
                    }
                    else {
                        params += words[i];
                    }
                }
                else {
                    if (words[i].equalsIgnoreCase("instr")) {
                        inStrFound = true;
                    }
                    else if (G.beginLike(words[i],"instr(")) {
                        expression += replaceInStrSentence(words[i]);
                    }
                    else if (containsInStr(words[i])) {
                        expression += replaceInStrSentence(words[i]);
                    }
                    else {
                        expression += words[i];
                    }
                }
            }
        }
        return expression.trim();
    }
    
    private String getSource(String source) {
        if (isComplexExpression(source))
            return "(" + source + ")";
        else
            return source;
    }

    private String replaceReplaceSentence(String expression) {

        expression = G.ltrimTab(expression);

        if (containsReplace(expression)) {

            boolean replaceFound = false;
            int openParentheses = 0;
            String[] words = G.split(expression);
            String params = "";
            expression = "";

            for (int i = 0; i < words.length; i++) {
                if (replaceFound) {
                    if (words[i].equals("(")) {
                        openParentheses++;
                        if (openParentheses > 1) {
                            params += words[i];
                        }
                    }
                    // look for a close parentheses without an open parentheses
                    else if (words[i].equals(")")) {
                        openParentheses--;
                        if (openParentheses == 0) {
                            if (containsReplace(params)) {
                                params = replaceReplaceSentence(params);
                            }
                            String[] vparams = G.split(params);
                            String identifier = "";
                            String toSearch = "";
                            String newValue = "";

                            int colons = 0;
                            identifier = "";
                            for (int t = 0; t < vparams.length; t++) {
                                if (vparams[t].equals(",")) {
                                    colons++;
                                }
                                else {

                                    if (colons == 0) {
                                        identifier += vparams[t];
                                    }
                                    else if (colons == 1) {
                                        toSearch += vparams[t];
                                    }
                                    else if (colons == 2) {
                                        newValue += vparams[t];
                                    }
                                    else {
                                        showError("Unexpected colon found in Replace function's params: " + params);
                                    }
                                }
                            }
                            if (toSearch.isEmpty())
                                showError("Missing parameter in Replace function");
                            if (newValue.isEmpty())
                                showError("Missing parameter in Replace function");
                            // identifier can be a complex expresion
                            // like ' "an string plus" + a_var '
                            //
                            if (G.contains(identifier, " ")) {
                                identifier = "(" + identifier + ")";
                            }
                            if (m_translateToJava)
                                expression += identifier 
                                                + ".replace(" + toSearch.trim()
                                                + ", " + newValue.trim() + ")";
                            else
                                expression += identifier 
                                                + ".Replace(" + toSearch.trim()
                                                + ", " + newValue.trim() + ")";
                            replaceFound = false;
                            params = "";
                        }
                        else {
                            params = params.trim() + words[i];
                        }
                    }
                    else {
                        params += words[i];
                    }
                }
                else {
                    if (words[i].equalsIgnoreCase("replace")) {
                        replaceFound = true;
                    }
                    else if (words[i].equalsIgnoreCase("replace$")) {
                        replaceFound = true;
                    }
                    else if (G.beginLike(words[i],"replace(")) {
                        expression += replaceReplaceSentence(words[i]);
                    }
                    else if (G.beginLike(words[i],"replace$(")) {
                        expression += replaceReplaceSentence(words[i]);
                    }
                    else if (containsReplace(words[i])) {
                        expression += replaceReplaceSentence(words[i]);
                    }
                    else {
                        expression += words[i];
                    }
                }
            }
        }
        return expression.trim();
    }

    private String replaceIifSentence(String expression) {

        expression = G.ltrimTab(expression);

        if (containsIif(expression)) {

            boolean iifFound = false;
            int openParentheses = 0;
            String[] words = G.split(expression);
            String params = "";
            expression = "";

            for (int i = 0; i < words.length; i++) {
                if (iifFound) {
                    if (words[i].equals("(")) {
                        openParentheses++;
                        if (openParentheses > 1) {
                            params += words[i];
                        }
                    }
                    // look for a close parentheses without an open parentheses
                    else if (words[i].equals(")")) {
                        openParentheses--;
                        if (openParentheses == 0) {
                            if (containsIif(params)) {
                                params = replaceIifSentence(params);
                            }
                            String[] vparams = G.split(params);
                            String identifier = "";
                            String trueValue = "";
                            String falseValue = "";

                            int colons = 0;
                            identifier = "";
                            for (int t = 0; t < vparams.length; t++) {
                                if (vparams[t].equals(",")) {
                                    colons++;
                                }
                                else {

                                    if (colons == 0) {
                                        identifier += vparams[t];
                                    }
                                    else if (colons == 1) {
                                        trueValue += vparams[t];
                                    }
                                    else if (colons == 2) {
                                        falseValue += vparams[t];
                                    }
                                    else {
                                        showError("Unexpected colon found in IIf function's params: " + params);
                                    }
                                }
                            }
                            if (trueValue.isEmpty()) {
                                showError("trueValue was missing in IIf function's params : " + params);
                            }
                            if (falseValue.isEmpty()) {
                                showError("falseValue was missing in IIf function's params : " + params);
                            }
                            // identifier can be a complex expresion
                            // like ' "an string plus" + a_var '
                            //
                            if (G.contains(identifier, " ")) {
                                identifier = "(" + identifier + ")";
                            }
                            expression += identifier + " ? " 
                                            + trueValue.trim() 
                                            +" : " 
                                            + falseValue.trim() + ")";
                            iifFound = false;
                            params = "";
                        }
                        else {
                            params = params.trim() + words[i];
                        }
                    }
                    else {
                        params += words[i];
                    }
                }
                else {
                    if (words[i].equalsIgnoreCase("iif")) {
                        iifFound = true;
                    }
                    else if (G.beginLike(words[i],"iif(")) {
                        expression += replaceIifSentence(words[i]);
                    }
                    else if (containsIif(words[i])) {
                        expression += replaceIifSentence(words[i]);
                    }
                    else {
                        expression += words[i];
                    }
                }
            }
        }
        return expression.trim();
    }
    
    private String replaceTrimSentence(String expression) {

        expression = G.ltrimTab(expression);

        if (containsTrim(expression)) {

            boolean trimFound = false;
            int openParentheses = 0;
            String[] words = G.split(expression);
            String params = "";
            expression = "";

            for (int i = 0; i < words.length; i++) {
                if (trimFound) {
                    if (words[i].equals("(")) {
                        openParentheses++;
                        if (openParentheses > 1) {
                            params += words[i];
                        }
                    }
                    // look for a close parentheses without an open parentheses
                    else if (words[i].equals(")")) {
                        openParentheses--;
                        if (openParentheses == 0) {
                            if (containsTrim(params)) {
                                params = replaceTrimSentence(params);
                            }
                            String[] vparams = G.split(params);
                            String identifier = "";

                            int colons = 0;
                            identifier = "";
                            for (int t = 0; t < vparams.length; t++) {
                                if (vparams[t].equals(",")) {
                                    colons++;
                                }
                                else {

                                    if (colons == 0) {
                                        identifier += vparams[t];
                                    }
                                    else {
                                        showError("Unexpected colon found in Trim function's params: " + params);
                                    }
                                }
                            }
                            // identifier can be a complex expresion
                            // like ' "an string plus" + a_var '
                            //
                            if (G.contains(identifier, " ")) {
                                identifier = "(" + identifier + ")";
                            }
                            if (m_translateToJava)
                                expression += identifier + ".trim()";
                            else
                                expression += identifier + ".Trim()";
                            trimFound = false;
                            params = "";
                        }
                        else {
                            params = params.trim() + words[i];
                        }
                    }
                    else {
                        params += words[i];
                    }
                }
                else {
                    if (words[i].equalsIgnoreCase("trim")) {
                        trimFound = true;
                    }
                    else if (words[i].equalsIgnoreCase("trim$")) {
                        trimFound = true;
                    }
                    else if (G.beginLike(words[i],"trim(")) {
                        expression += replaceTrimSentence(words[i]);
                    }
                    else if (G.beginLike(words[i],"trim$(")) {
                        expression += replaceTrimSentence(words[i]);
                    }
                    else if (containsMid(words[i])) {
                        expression += replaceTrimSentence(words[i]);
                    }
                    else {
                        expression += words[i];
                    }
                    
                }
            }
        }
        return expression.trim();
    }

    private String replaceIsNumericSentence(String expression) {
        if (containsFunction(expression, "IsNumeric")) {
            if (m_AddAuxFunctionsToClass) {
                m_addIsNumericAuxFunction = true;
                return replaceOneParamFunction(expression, "IsNumeric", "isNumeric");
            }
            // when preference are setting to use G class or CSUtils
            // it is translated using G.{auxfunction}
            //
            else {
                m_addIsNumericAuxFunctionToG = m_UseGAuxFunctions;
                return replaceOneParamFunction(expression, "IsNumeric", "G.isNumeric");
            }
        }
        else
            return expression;
    }

    private String replaceCDblSentence(String expression) {
        if (containsFunction(expression, "CDbl")) {
            return replaceOneParamFunction(expression, "CDbl", "Double.parseDouble");
        }
        else
            return expression;
    }

    private String replaceCIntSentence(String expression) {
        if (containsFunction(expression, "CInt")) {
            return replaceOneParamFunction(expression, "CInt", "Integer.parseInt");
        }
        else
            return expression;
    }

    private String replaceCLngSentence(String expression) {
        if (containsFunction(expression, "CLng")) {
            return replaceOneParamFunction(expression, "CLng", "Long.parseLong");
        }
        else
            return expression;
    }

    private String replaceCSngSentence(String expression) {
        if (containsFunction(expression, "CSng")) {
            return replaceOneParamFunction(expression, "CSng", "Double.parseDouble");
        }
        else
            return expression;
    }

    private String replaceCCurSentence(String expression) {
        if (containsFunction(expression, "CCur")) {
            return replaceOneParamFunction(expression, "CCur", "Double.parseDouble");
        }
        else
            return expression;
    }

    private String replaceCDateSentence(String expression) {
        if (containsFunction(expression, "CDate")) {
            if (m_AddAuxFunctionsToClass) {
                m_addParseDateAuxFunction = true;
                return replaceOneParamFunction(expression, "CDate", "parseDate");
            }
            // when preference are setting to use G class or CSUtils
            // it is translated using G.{auxfunction}
            //
            else {
                m_addParseDateAuxFunctionToG = m_UseGAuxFunctions;
                return replaceOneParamFunction(expression, "CDate", "G.parseDate");
            }
        }
        else
            return expression;
    }

    private String replaceOneParamFunction(String expression, String function, String javaFunction) {
        
        expression = G.ltrimTab(expression);

        if (containsFunction(expression, function)) {

            boolean functionFound = false;
            int openParentheses = 0;
            String[] words = G.split(expression);
            String params = "";
            expression = "";

            for (int i = 0; i < words.length; i++) {
                if (functionFound) {
                    if (words[i].equals("(")) {
                        openParentheses++;
                        if (openParentheses > 1) {
                            params += words[i];
                        }
                    }
                    // look for a close parentheses without an open parentheses
                    else if (words[i].equals(")")) {
                        openParentheses--;
                        if (openParentheses == 0) {
                            if (containsFunction(params, function)) {
                                params = replaceOneParamFunction(params, function, javaFunction);
                            }
                            String[] vparams = G.split(params);
                            String identifier = "";

                            int colons = 0;
                            identifier = "";
                            for (int t = 0; t < vparams.length; t++) {
                                if (vparams[t].equals(",")) {
                                    colons++;
                                }
                                else {

                                    if (colons == 0) {
                                        identifier += vparams[t];
                                    }
                                    else {
                                        showError("Unexpected colon found in "
                                                + function
                                                + " function's params: " + params);
                                    }
                                }
                            }
                            // identifier can be a complex expresion
                            // like ' "an string plus" + a_var '
                            //
                            if (G.contains(identifier, " ")) {
                                identifier = "(" + identifier + ")";
                            }
                            expression += javaFunction + "(" + identifier + ")";
                            functionFound = false;
                            params = "";
                        }
                        else {
                            params = params.trim() + words[i];
                        }
                    }
                    else {
                        params += words[i];
                    }
                }
                else {
                    if (words[i].equalsIgnoreCase(function)) {
                        functionFound = true;
                    }
                    else if (G.beginLike(words[i], function + "(")) {
                        expression += replaceOneParamFunction(words[i], function, javaFunction);
                    }
                    else if (words[i].toLowerCase().contains(" " + function + "(")) {
                        expression += replaceOneParamFunction(words[i], function, javaFunction);
                    }
                    else if (words[i].toLowerCase().contains("(" + function + "(")) {
                        expression += replaceOneParamFunction(words[i], function, javaFunction);
                    }
                    else {
                        expression += words[i];
                    }
                }
            }
        }
        return expression.trim();
    }

    private boolean containsTypeOf(String expression) {
        if (G.beginLike(expression, "typeof ")) {
            return true;
        }
        else if (expression.toLowerCase().contains(" typeof ")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(typeof ")) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean containsMid(String expression) {
        if (expression.toLowerCase().contains(" mid(")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(mid(")) {
            return true;
        }
        else if (expression.toLowerCase().contains(" mid$(")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(mid$(")) {
            return true;
        } 
        else if (G.beginLike(expression,"mid(")) {
            return true;
        }
        else if (G.beginLike(expression,"mid$(")) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean containsLeft(String expression) {
        if (expression.toLowerCase().contains(" left(")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(left(")) {
            return true;
        }
        else if (expression.toLowerCase().contains(" left$(")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(left$(")) {
            return true;
        }
        else if (G.beginLike(expression,"left(")) {
            return true;
        }
        else if (G.beginLike(expression,"left$(")) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean containsRight(String expression) {
        if (expression.toLowerCase().contains(" right(")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(right(")) {
            return true;
        }
        else if (expression.toLowerCase().contains(" right$(")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(right$(")) {
            return true;
        }
        else if (G.beginLike(expression,"right(")) {
            return true;
        }
        else if (G.beginLike(expression,"right$(")) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean containsLCase(String expression) {
        if (expression.toLowerCase().contains(" lcase(")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(lcase(")) {
            return true;
        }
        else if (expression.toLowerCase().contains(" lcase$(")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(lcase$(")) {
            return true;
        }
        else if (G.beginLike(expression,"lcase(")) {
            return true;
        }
        else if (G.beginLike(expression,"lcase$(")) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean containsUCase(String expression) {
        if (expression.toLowerCase().contains(" ucase(")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(ucase(")) {
            return true;
        }
        else if (expression.toLowerCase().contains(" ucase$(")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(ucase$(")) {
            return true;
        }
        else if (G.beginLike(expression,"ucase(")) {
            return true;
        }
        else if (G.beginLike(expression,"ucase$(")) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean containsLen(String expression) {
        return containsFunction(expression, "len");
    }

    private boolean containsTrim(String expression) {
        if (expression.toLowerCase().contains(" trim(")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(trim(")) {
            return true;
        }
        else if (expression.toLowerCase().contains(" trim$(")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(trim$(")) {
            return true;
        }
        else if (G.beginLike(expression,"trim(")) {
            return true;
        }
        else if (G.beginLike(expression,"trim$(")) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean containsInStr(String expression) {
        return containsFunction(expression, "instr");
    }

    private boolean containsReplace(String expression) {
        if (expression.toLowerCase().contains(" replace(")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(replace(")) {
            return true;
        }
        else if (expression.toLowerCase().contains(" replace$(")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(replace$(")) {
            return true;
        } 
        else if (G.beginLike(expression,"replace(")) {
            return true;
        }
        else if (G.beginLike(expression,"replace$(")) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean containsIif(String expression) {
        if (expression.toLowerCase().contains(" iif(")) {
            return true;
        }
        else if (expression.toLowerCase().contains("(iif(")) {
            return true;
        }
        else if (G.beginLike(expression,"iif(")) {
            return true;
        }
        else {
            return false;
        }
    }
    
    private boolean containsFunction(String expression, String function) {
        function = function.toLowerCase();
        expression = expression.toLowerCase();
        if (expression.contains(" " + function + "(")) {
            return true;
        }
        else if (expression.contains("(" + function + "(")) {
            return true;
        }
        else if (G.beginLike(expression,function + "(")) {
            return true;
        }
        else {
            return false;
        }
    }

    private String replaceVbWords(String expression) {
        expression = G.ltrimTab(expression);
        String[] words = G.split2(expression);
        expression = "";

        // Public
        //
        for (int i = 0; i < words.length; i++) {
            if (words[i].equalsIgnoreCase("vbNullString")) {
                words[i] = "\"\"";
            }
            else if (words[i].equalsIgnoreCase("False")) {
                words[i] = "false";
            }
            else if (words[i].equalsIgnoreCase("True")) {
                words[i] = "true";
            }
            else if (words[i].equalsIgnoreCase("vbCrLf")) {
                words[i] = "\"\\r\\n\"";
            }
            else if (words[i].equalsIgnoreCase("vbCr")) {
                words[i] = "\"\\n\"";
            }
            else if (words[i].equalsIgnoreCase("vbLf")) {
                words[i] = "\"\\r\"";
            }
            else if (words[i].equalsIgnoreCase("me")) {
                words[i] = "this";
            }
            expression += words[i];// + " ";
        }
        return expression.trim();
    }

    private String processEqualsSentence(String firstOperand, String secondOperand, String operator) {
        boolean isNotEquals = operator.equals("!=");
        if (isStringExpression(firstOperand)) {
            if (isNotEquals) {
                String firstSpace = "";
                if (firstOperand.startsWith(" "))
                    firstSpace = " ";
                return firstSpace 
                        +"!("
                        + firstOperand.trim() + ".equals(" + secondOperand.trim() + ")"
                        +")";
            }
            else
                return G.rtrim(firstOperand) + ".equals(" + secondOperand.trim() + ")";
        }
        else if (isStringExpression(secondOperand)) {
            if (isNotEquals) {
                String firstSpace = "";
                if (secondOperand.startsWith(" "))
                    firstSpace = " ";
                return firstSpace
                        +"!("
                        + secondOperand.trim() + ".equals(" + firstOperand.trim() + ")"
                        + ")";
            }
            else
                return G.rtrim(secondOperand) + ".equals(" + firstOperand.trim() + ")";
        }
        else {
            if (isNotEquals)
                return firstOperand + "!=" + secondOperand;
            else
                return firstOperand + "==" + secondOperand;
        }
    }

    private boolean isStringExpression(String expression) {
        IdentifierInfo info = null;
        String type = "";
        String parent = "";
        expression = expression.trim();
        if (expression.length() == 0)
            return false;
        if (expression.charAt(0) == '.') {
            if (expression.length() > 1) {
                expression = expression.substring(1);
            }
            else {
                expression = "";
            }
        }
        String[] words = G.split3(expression,".");
        if (m_collWiths.size() > 0) {
            parent = m_collWiths.get(m_collWiths.size()-1).dataType;
        }
        for (int i = 0; i < words.length; i++) {
            info = getIdentifierInfo(words[i], parent, !parent.isEmpty());
            if (info == null)
                type = "";
            else if (info.isFunction)
                type = info.function.getReturnType().dataType;
            else
                type = info.variable.dataType;
            parent = type;
        }

        if (info == null) {
            if (expression.charAt(0) == '"') {
                if (expression.charAt(expression.length()-1) == '"') {
                    return true;
                }
            }
            return false;
        }
        else {
            if (info.isFunction) {
                return info.function.getReturnType().isString;
            }
            else {
                return info.variable.isString;
            }
        }
    }

    private Function getFunction(String expression, String className) {
        String functionName = "";

        if (expression.contains("(")) {
            int i = expression.indexOf("(");
            if (i > 0) {
                functionName = expression.substring(0, i);
            }
        }
        else {
            functionName = expression;
        }
        if (functionName.isEmpty()) {
            return null;
        }

        Iterator itrFile = null;

        // first we search in private functions
        //
        if (className.isEmpty()
                || className.toLowerCase().equals("this")
                || className.toLowerCase().equals("me")) {
            itrFile = m_collFiles.iterator();
            while(itrFile.hasNext()) {
                SourceFile source = (SourceFile)itrFile.next();
                if (source.getJavaName().equals(m_javaClassName)) {
                    Iterator itrPrivateFunctions = source.getPrivateFunctions().iterator();
                    while (itrPrivateFunctions.hasNext()) {
                        Function privateFunction = (Function)itrPrivateFunctions.next();
                        if (privateFunction.getJavaName().equals(functionName))
                            return privateFunction;
                        else if (privateFunction.getVbName().equals(functionName))
                            return privateFunction;
                    }
                    break;
                }
            }
        }

        // to search in public memebers we assign the real name
        // of this class to the parameter className when we
        // have the sinonyms 'this' or 'me'
        //
        if (className.toLowerCase().equals("this")
                || className.toLowerCase().equals("me")) {
            className = m_javaClassName;
        }

        // here we search for public functions, public properties
        //
        itrFile = m_collFiles.iterator();
        while(itrFile.hasNext()) {
            SourceFile source = (SourceFile)itrFile.next();
            if (className.isEmpty()
                    || source.getJavaName().equals(className)
                    || source.getVbName().equals(className)) {
                Iterator itrPublicFunctions = source.getPublicFunctions().iterator();
                while (itrPublicFunctions.hasNext()) {
                    Function publicFunction = (Function)itrPublicFunctions.next();
                    if (publicFunction.getJavaName().equals(functionName))
                        return publicFunction;
                    else if (publicFunction.getVbName().equals(functionName))
                        return publicFunction;
                }
                if (!className.isEmpty())
                    break;
            }
        }

        // here we search for public functions in java
        //
        itrFile = m_collJavaClassess.iterator();
        while(itrFile.hasNext()) {
            SourceFile source = (SourceFile)itrFile.next();
            if (className.isEmpty()
                    || source.getJavaName().equals(className)) {
                Iterator itrPublicFunctions = source.getPublicFunctions().iterator();
                while (itrPublicFunctions.hasNext()) {
                    Function publicFunction = (Function)itrPublicFunctions.next();
                    if (publicFunction.getJavaName().equals(functionName))
                        return publicFunction;
                }
                break;
            }
        }

        // if we are here, we must look in the database
        //
        Function publicFunction = FunctionObject.getFunctionFromName(
                                                    functionName,
                                                    className,
                                                    m_references);

        return publicFunction;
    }

    private String getCastToString(String identifier) {
        
        // if the caller by mistake put parentheses in a return sentences
        // we fixed it here
        //
        if (identifier.startsWith("(return") && identifier.endsWith(")")) {
            identifier = identifier.substring(1,identifier.length()-1);
        }

        String returnPrefix = "";
        identifier = identifier.trim();
        if (G.beginLike(identifier.trim(), "return")) {
            int i = identifier.indexOf("return") + 6;
            returnPrefix = identifier.substring(0, i);
            identifier = identifier.substring(i);
        }

        Variable var = getVariable(identifier);        
        
        if (var == null) {
            IdentifierInfo info = getIdentifierInfo(identifier);
            if (info != null) {
                if (!info.isFunction) 
                    var = info.variable;
                else
                    var = info.function.getReturnType();
            }
        }
        if (var != null) {
            if (var.isString)
                return returnPrefix + identifier;
                    
            else if (var.isLong)
                if (m_translateToJava)
                    return returnPrefix + "((Long) " + identifier + ").toString()";
                else
                    return returnPrefix + identifier + ".toString()";
                    
            else if (var.isInt)
                if (m_translateToJava)
                    return returnPrefix + "((Integer) " + identifier + ").toString()";
                else
                    return returnPrefix + identifier + ".toString()";
                    
            else if (var.isBoolean)
                if (m_translateToJava)
                    return returnPrefix + "((Boolean) " + identifier + ").toString()";
                else
                    return returnPrefix + identifier + ".toString()";
                    
            else
                return returnPrefix + identifier;
        }
        else if (isComplexExpression(identifier)) {
            if (isStringExpression(identifier)) 
                return returnPrefix + identifier;
            else if (identifier.startsWith("(")) {
                if (m_translateToJava)
                    return returnPrefix + "String.valueOf" + identifier;
                else
                    return returnPrefix + identifier + ".ToString()";
            }
            else {
                if (m_translateToJava)
                    return returnPrefix + "String.valueOf(" + identifier + ")";
                else
                    return returnPrefix + "(" + identifier + ").ToString()";
            } 
        }
        else
            return returnPrefix + identifier;
    }
    
    private IdentifierInfo getIdentifierInfo(String expression) {
        IdentifierInfo info = null;
        String type = "";
        String parent = "";
        String[] words = G.split2(expression, ".()[]");
        String[] parents = new String[30]; // why 30? who nows :P, 30 should be enough :)
        int openParentheses = 0;

        for (int i = 0; i < words.length; i++) {
            if (!(".()[]".contains(words[i]))) {
                if (openParentheses == 0) {
                    info = getIdentifierInfo(words[i], parent, !parent.isEmpty());
                    if (info == null)
                        type = "";
                    else if (info.isFunction) {
                        type = info.function.getReturnType().dataType;
                    }
                    else {
                        type = info.variable.dataType;
                    }
                    parent = type;
                }
            }
            else if (words[i].equals("(")) {
                parents[openParentheses] = parent;
                parent = "";
                openParentheses++;
            }
            else if (words[i].equals(")")) {
                openParentheses--;
                parent = parents[openParentheses];
            }
            else if (words[i].equals("[")) {
                parents[openParentheses] = parent;
                parent = "";
                openParentheses++;
            }
            else if (words[i].equals("]")) {
                openParentheses--;
                parent = parents[openParentheses];
            }
        }    
        return info;
    }

    private boolean isComplexExpression(String expression) {
        if (expression.contains("+"))
            return true;
        if (expression.contains("-"))
            return true;
        if (expression.contains("*"))
            return true;
        if (expression.contains("/"))
            return true;
        else
            return false;
    }

    private Variable getVariable(String identifier) {
        return getVariable(identifier, "", false);
    }

    private Variable getVariable(String expression, String className, boolean isField) {
        String identifier = "";

        // in vb arrays use parentheses to define the index
        // eg: m_vGroups(i)
        //
        if (expression.contains("(")) {
            int i = expression.indexOf("(");
            if (i > 0) {
                identifier = expression.substring(0, i);
            }
        }
        else if (expression.contains("[")) {
            int i = expression.indexOf("[");
            if (i > 0) {
                identifier = expression.substring(0, i);
            }
        }
        else {
            identifier = expression;
        }
        if (identifier.isEmpty()) {
            return null;
        }

        if (!isField) {
            for (int i = 0; i < m_functionVariables.size(); i++) {
                if (identifier.equals(m_functionVariables.get(i).getVbName())) {
                    return m_functionVariables.get(i);
                }
                if (identifier.equals(m_functionVariables.get(i).getJavaName())) {
                    return m_functionVariables.get(i);
                }
            }
            for (int i = 0; i < m_memberVariables.size(); i++) {
                if (identifier.equals(m_memberVariables.get(i).getVbName())) {
                    return m_memberVariables.get(i);
                }
                if (identifier.equals(m_memberVariables.get(i).getJavaName())) {
                    return m_memberVariables.get(i);
                }
            }
        }

        // now we search in private types and public types
        // declared in this class
        //
        Iterator itrTypes = null;

        if (!className.isEmpty()) {
            itrTypes = m_types.iterator();
            while(itrTypes.hasNext()) {
                Type type = (Type)itrTypes.next();
                if (type.javaName.equals(className)
                        || type.vbName.equals(className)) {
                    Iterator itrMembers = type.getMembersVariables().iterator();
                    while (itrMembers.hasNext()) {
                        Variable member = (Variable)itrMembers.next();
                        if (member.getJavaName().equals(identifier))
                            return member;
                        else if (member.getVbName().equals(identifier))
                            return member;
                    }
                    break;
                }
            }
        }

        // here we search for public variables declared in bas files
        //
        Iterator itrFile = m_collFiles.iterator();
        while(itrFile.hasNext()) {
            SourceFile source = (SourceFile)itrFile.next();
            if (className.isEmpty()
                    || source.getJavaName().equals(className)
                    || source.getVbName().equals(className)) {
                Iterator itrPublicVar = source.getPublicVariables().iterator();
                while (itrPublicVar.hasNext()) {
                    Variable publicVar = (Variable)itrPublicVar.next();
                    if (publicVar.getJavaName().equals(identifier))
                        return publicVar;
                    else if (publicVar.getVbName().equals(identifier))
                        return publicVar;
                }
                if (!className.isEmpty())
                    break;
            }
        }
        
        // if we are here, we must look in the database
        //
        Variable publicVariable = VariableObject.getVariableFromName(
                                                    identifier,
                                                    className,
                                                    m_references);

        return publicVariable;
    }

    private Variable getMemberVariable(String expression) {
        String identifier = "";

        // in vb arrays use parentheses to define the index
        // eg: m_vGroups(i)
        //
        if (expression.contains("(")) {
            int i = expression.indexOf("(");
            if (i > 0) {
                identifier = expression.substring(0, i);
            }
        }
        else {
            identifier = expression;
        }
        if (identifier.isEmpty()) {
            return null;
        }

        for (int i = 0; i < m_memberVariables.size(); i++) {
            if (identifier.equals(m_memberVariables.get(i).getVbName())) {
                return m_memberVariables.get(i);
            }
            if (identifier.equals(m_memberVariables.get(i).getJavaName())) {
                return m_memberVariables.get(i);
            }
        }
        return null;
    }

    private String[] getWordsFromSentence(String strLine) {
        boolean literalFlag = false;
        String[] words = new String[500];
        String word = "";
        int j = 0;
        for (int i = 0; i < strLine.length(); i++) {
            if (strLine.charAt(i) == '"') {
                literalFlag = !literalFlag;
            }
            if (!literalFlag) {
                if (C_SYMBOLS.contains(String.valueOf(strLine.charAt(i)))) {
                    if (!word.isEmpty()) {
                        /*if (j >= words.length) {
                            int dummy = 0;
                        }*/
                        words[j] = word;
                        word = "";
                        j++;
                    }
                    /*if (j >= words.length) {
                        int dummy = 0;
                    }*/
                    words[j] = String.valueOf(strLine.charAt(i));
                    j++;
                }
                else {
                    word += String.valueOf(strLine.charAt(i));
                }
            }
            else {
                word += String.valueOf(strLine.charAt(i));
            }
        }
        if (!word.isEmpty()) {
            words[j] = word;
            j++;
        }
        String[] rtn = new String[j];
        System.arraycopy(words, 0, rtn, 0, j);
        return rtn;
    }

    private boolean isFunctionDeclaration(String strLine) {
        if (G.beginLike(strLine, "Public Function "))
            return true;
        else if (G.beginLike(strLine, "Private Function "))
            return true;
        else if (G.beginLike(strLine, "Friend Function "))
            return true;
        else if (G.beginLike(strLine, "Public Sub "))
            return true;
        else if (G.beginLike(strLine, "Private Sub "))
            return true;
        else if (G.beginLike(strLine, "Friend Sub "))
            return true;
        else if (G.beginLike(strLine, "Public Property "))
            return true;
        else if (G.beginLike(strLine, "Private Property "))
            return true;
        else if (G.beginLike(strLine, "Friend Property "))
            return true;
        else if (G.beginLike(strLine, "Function "))
            return true;
        else if (G.beginLike(strLine, "Sub "))
            return true;
        else if (G.beginLike(strLine, "Property "))
            return true;
        else
            return false;
    }

    private String translateFunctionDeclaration(String strLine) {
        // On Error flag is reset in every function
        //
        m_onErrorFound = false;
        m_onErrorResumeNext = false;
        m_onErrorLabel = "";
        m_onCatchBlock = false;

        String functionName = "";
        String functionType = "";
        String functionScope = "";

        m_vbFunctionName = "";
        m_functionVariables.removeAll(m_functionVariables);

        strLine = G.ltrimTab(strLine);
        String[] words = G.splitSpace(strLine);//strLine.split("\\s+");

        // Public
        //
        if (words[0].equalsIgnoreCase("public")) {
            functionScope = "public";
            functionName = words[2];
            m_vbFunctionName = words[2];
            if (words[1].equalsIgnoreCase("sub")) {
                functionType = "void";
            }
            else if (words[1].equalsIgnoreCase("function")) {
                functionType = getFunctionType(strLine);
            }
            // properties
            //
            else {
                if (functionName.equalsIgnoreCase("get")) {
                    functionName = "get" 
                                    + words[3].substring(0, 1).toUpperCase()
                                    + words[3].substring(1);
                    m_vbFunctionName = words[3];
                    functionType = getPropertyType(strLine);
                }
                else {
                    functionName = "set" 
                                    + words[3].substring(0, 1).toUpperCase()
                                    + words[3].substring(1);
                    m_vbFunctionName = words[3];
                    functionType = "void";
                }
            }
        }
        // Private
        //
        else if (words[0].equalsIgnoreCase("private")) {
            functionScope = "private";
            functionName = words[2];
            m_vbFunctionName = words[2];
            if (words[1].equalsIgnoreCase("sub")) {
                functionType = "void";
            }
            else if (words[1].equalsIgnoreCase("function")) {
                functionType = getFunctionType(strLine);
            }
            // properties
            //
            else {
                if (functionName.equalsIgnoreCase("get")) {
                    functionName = "get" 
                                    + words[3].substring(0, 1).toUpperCase()
                                    + words[3].substring(1);
                    m_vbFunctionName = words[3];
                    functionType = getPropertyType(strLine);
                }
                else {
                    functionName = "set" 
                                    + words[3].substring(0, 1).toUpperCase()
                                    + words[3].substring(1);
                    m_vbFunctionName = words[3];
                    functionType = "void";
                }
            }
        }
        // Friend
        //
        else if (words[0].equalsIgnoreCase("friend")) {
            functionScope = "public";
            functionName = words[2];
            m_vbFunctionName = words[2];
            if (words[1].equalsIgnoreCase("sub")) {
                functionType = "void";
            }
            else if (words[1].equalsIgnoreCase("function")) {
                functionType = getFunctionType(strLine);
            }
            // properties
            //
            else {
                if (functionName.equalsIgnoreCase("get")) {
                    functionName = "get" 
                                    + words[3].substring(0, 1).toUpperCase()
                                    + words[3].substring(1);
                    m_vbFunctionName = words[3];
                    functionType = getPropertyType(strLine);
                }
                else {
                    functionName = "set" 
                                    + words[3].substring(0, 1).toUpperCase()
                                    + words[3].substring(1);
                    m_vbFunctionName = words[3];
                    functionType = "void";
                }
            }
        }
        else if (words[0].equalsIgnoreCase("function")) {
            functionScope = "private";
            functionName = words[1];
            m_vbFunctionName = words[1];
            functionType = getFunctionType(strLine);
        }
        else if (words[0].equalsIgnoreCase("sub")) {
            functionScope = "private";
            functionName = words[1];
            m_vbFunctionName = words[1];
            functionType = "void";
        }
        else if (words[0].equalsIgnoreCase("property")) {
            functionScope = "private";
            functionName = words[1];
            m_vbFunctionName = words[1];
            if (functionName.equalsIgnoreCase("get")) {
                functionName = "get" 
                                + words[2].substring(0, 1).toUpperCase()
                                + words[2].substring(1);
                m_vbFunctionName = words[2];
                functionType = getPropertyType(strLine);
            }
            else {
                functionName = "set" 
                                + words[2].substring(0, 0).toUpperCase()
                                + words[2].substring(1);
                m_vbFunctionName = words[2];
                functionType = "void";
            }
        }

        if (functionName.contains("(")) {
            functionName = functionName.substring(0,functionName.indexOf("("));
            m_vbFunctionName = m_vbFunctionName.substring(0, m_vbFunctionName.indexOf("("));
        }

        if (functionName.length() < 1) {
            functionName = "";
            m_vbFunctionName = "";
        }

        functionName = functionName.substring(0, 1).toLowerCase()
                        + functionName.substring(1);

        if (!functionName.isEmpty() && functionScope.equals("public"))
            saveFunction(m_vbFunctionName, functionName, functionType);

        m_returnValue = getDefaultForReturnType(functionType);

        String modifiers = "";
        
        if (m_isBasFile)
            modifiers = "static ";
        
        modifiers += getIfNeedToBeSyncrhonized();
        
        String todoByRef = "";
        if (strLine.contains("ByRef "))
            todoByRef = " // TODO: Use of ByRef founded " + strLine;

        return functionScope + " "
                + modifiers
                + functionType + " "
                + functionName + "("
                + translateParameters(strLine)
                + ") {"
                + todoByRef
                + newline;
    }

    private String translateFunctionReturnVariable(String strLine) {
        m_function = getFunction(m_vbFunctionName, "me");
        if (m_function != null) {
            if (m_function.getNeedReturnVariable()) {
                strLine += getTabs()
                        + C_TAB + m_function.getReturnType().dataType
                        + " _rtn = " + m_returnValue + ";" + newline;
            }
        }
        /*else {
            m_function = getFunction(m_vbFunctionName, "me");
        }*/
        return strLine;
    }

    private String getReturnLine() {
        String returnLine = "";
        if (m_function.getNeedReturnVariable()) {
            returnLine = "    return _rtn;" + newline + getTabs();
        }
        return returnLine;
    }

    private String replaceNotSentence(String strLine) {
        if (strLine.contains(" Not "))
            strLine = strLine.replace(" Not ", " !");
        else if (strLine.contains(" Not("))
            strLine = strLine.replace(strLine, " !(");
        return strLine;
    }

    private String replaceResumeSentence(String strLine) {
        if (strLine.toLowerCase().contains("resume("))
            return "/**TODO:** resume found: " + strLine + "*/";
        else
            return strLine;
    }

    private String replaceGotoSentence(String strLine) {
        if (G.beginLike(strLine.trim(), "GoTo "))
            return "//*TODO:** goto found: " + strLine;
        else
            return strLine;
    }

    private String replaceLabelSentence(String strLine) {
        if (strLine.trim().endsWith(":"))
            return "//*TODO:** label found: " + strLine;
        else
            return strLine;
    }

    private String replaceSetReturnValueSentence(String strLine) {
        if (m_inFunction) {
            if (m_function != null) {
                String toSearch = m_function.getVbName() + " = ";
                if (strLine.startsWith(toSearch)) {
                    if (m_function.getNeedReturnVariable())
                        strLine = "_rtn = " + strLine.substring(toSearch.length());
                    else
                        strLine = "return " + strLine.substring(toSearch.length());
                }
                toSearch = " " + toSearch;
                if (strLine.contains(toSearch)) {
                    if (m_function.getNeedReturnVariable())
                        strLine = strLine.replace(toSearch, " _rtn = ");
                    else
                        strLine = strLine.replace(toSearch, " return ");
                }
                else {
                    toSearch = m_function.getJavaName() + " = ";
                    if (strLine.startsWith(toSearch)) {
                        if (m_function.getNeedReturnVariable())
                            strLine = "_rtn = " + strLine.substring(toSearch.length());
                        else
                            strLine = "return " + strLine.substring(toSearch.length());
                    }
                    toSearch = " " + toSearch;
                    if (strLine.contains(toSearch)) {
                        if (m_function.getNeedReturnVariable())
                            strLine = strLine.replace(toSearch, " _rtn = ");
                        else
                            strLine = strLine.replace(toSearch, " return ");
                    }
                }
            }
            /*else {
                m_function = getFunction(m_vbFunctionName, "me");
            }*/
        }
        return strLine;
    }

    private String getIfNeedToBeSyncrhonized() {
        Iterator function = m_raiseEventFunctions.iterator();
        while (function.hasNext()) {
            if (((String)function.next()).equals(m_vbFunctionName)) {
                return "synchronized ";
            }
        }
        return "";
    }

    private String translateEventDeclaration(String strLine) {
        String eventName = "";
        String eventScope = "";

        strLine = G.ltrimTab(strLine);
        String[] words = G.splitSpace(strLine);//strLine.split("\\s+");

        // Public
        //
        if (words[0].equalsIgnoreCase("public")) {
            eventScope = "public";
            eventName = words[2];
        }
        // Private
        //
        else if (words[0].equalsIgnoreCase("private")) {
            eventScope = "private";
            eventName = words[2];
        }
        // Friend
        //
        else if (words[0].equalsIgnoreCase("friend")) {
            eventScope = "public";
            eventName = words[2];
        }
        else if (words[0].equalsIgnoreCase("event")) {
            eventScope = "public";
            eventName = words[1];
        }

        if (eventName.contains("(")) {
            eventName = eventName.substring(0,eventName.indexOf("("));
        }

        if (eventName.length() < 1) {
            eventName = "";
        }

        if (eventName.isEmpty()) {
            return "//*TODO:** the event declaration couldn't be translated. "
                    + newline
                    + strLine;
        }
        else {

            String todoByRef = "";
            if (strLine.contains(todoByRef))
                todoByRef = " // TODO: Use of ByRef founded " + strLine;
            
            m_listenerInterface += C_TAB
                                    + eventScope + " "
                                    + "void "
                                    + eventName.substring(0, 1).toLowerCase()
                                    + eventName.substring(1) + "("
                                    + translateParameters(strLine)
                                    + ");"
                                    + todoByRef
                                    + newline;
            m_adapterClass += C_TAB
                                    + eventScope + " "
                                    + "void "
                                    + eventName.substring(0, 1).toLowerCase()
                                    + eventName.substring(1) + "("
                                    + translateParameters(strLine)
                                    + ") {};"
                                    + todoByRef
                                    + newline;
            m_raiseEvents = true;
            return "";
        }
    }

    private String getPropertyType(String strLine) {
        return getFunctionType(strLine);
    }

    private String getFunctionType(String strLine) {
        int endParams = getEndParams(strLine);
        if (endParams < 0) {
            return "";
        }
        else {
            if (strLine.length() >= endParams + 2) {
                strLine = strLine.substring(endParams + 2);
                String[] words = G.splitSpace(strLine);//strLine.split("\\s+");
                if (words.length >=2) {
                    return getDataType(words[1]);
                }
                else
                    return getObjectTypeName();
            }
            else
                return getObjectTypeName();
        }
    }

    private String translateParameters(String strLine) {
        int startParams = getStartParams(strLine);
        int endParams = getEndParams(strLine);

        if (endParams - startParams > 0) {
            String params = strLine.substring(startParams + 1, endParams);
            String[] words = G.split3(params, ",");
            params = "";
            for (int i = 0; i < words.length; i++) {
                params += getParam(words[i]) + ", ";
            }
            if (params.isEmpty())
                return params.trim();
            else
                return params.substring(0,params.length()-2).trim();
        }
        else
            return "";
    }

    private String getParam(String strParam) {
        String paramName = "";
        String vbParamName = "";
        String dataType = getObjectTypeName();
        String[] words = G.splitSpace(strParam);//strParam.split("\\s+");

        // empty string
        //
        if (words.length == 0) {
            return "";
        }
        // param_name
        //
        else if (words.length == 1) {
            vbParamName = words[0];
            paramName = getParamName(words[0]);
        }
        // byval param_name
        // byref param_name
        // optional param_name
        //
        else if (words.length == 2) {
            // byval param_name
            if (words[0].equalsIgnoreCase("ByVal")) {
                vbParamName = words[1];
                paramName = getParamName(words[1]);
            }
            // byref param_name
            else if (words[0].equalsIgnoreCase("ByRef")) {
                vbParamName = words[1];
                paramName = getParamName(words[1]);
            }
            // optional param_name
            else if (words[0].equalsIgnoreCase("Optional")) {
                vbParamName = words[1];
                paramName = getParamName(words[1]);
            }
            else
                return "[*TODO:** param declaration unsuported -> "
                        + words[0]+ " " + words[1] + "]";
        }
        // param_name As param_type
        // optional byval param_name
        // optional byref param_name
        //
        else if (words.length == 3) {
            // param_name As param_type
            if (words[1].equalsIgnoreCase("as")) {
                dataType = getDataType(words[2]);
                vbParamName = words[0];
                paramName = getParamName(words[0]);
            }
            // optional byval param_name
            // optional byref param_name
            else {
                vbParamName = words[2];
                paramName = getParamName(words[2]);
            }
        }
        // byval param_name As param_type
        // byref param_name As param_type
        // optional param_name As param_type
        // optional param_name = default_value
        //
        else if (words.length == 4) {
            // byval param_name As param_type
            // byref param_name As param_type
            // optional param_name As param_type
            if (words[2].equalsIgnoreCase("as")) {
                dataType = getDataType(words[3]);
                vbParamName = words[1];
                paramName = getParamName(words[1]);
            }
            // optional param_name = default_value
            else if (words[2].equalsIgnoreCase("=")) {
                vbParamName = words[1];
                paramName = getParamName(words[1]);
            }
            else
                return "[*TODO:** param declaration unsuported -> "
                        + words[0]+ " " + words[1]
                        + words[2]+ " " + words[3] + "]";
        }
        // byval optional param_name As data_type
        // byref optional param_name As data_type
        // byval optional param_name = default_value
        // byref optional param_name = default_value
        //
        else if (words.length == 5) {
            // byval optional param_name As data_type
            // byref optional param_name As data_type
            if (words[3].equalsIgnoreCase("as")) {
                dataType = getDataType(words[4]);
                vbParamName = words[2];
                paramName = getParamName(words[2]);
            }
            // byval optional param_name = default_value
            // byref optional param_name = default_value
            else if (words[3].equalsIgnoreCase("=")) {
                vbParamName = words[2];
                paramName = getParamName(words[2]);
            }
            else
                return "[*TODO:** param declaration unsuported -> "
                        + words[0]+ " " + words[1]
                        + words[2]+ " " + words[3]
                        + words[4]+ "]";
        }
        // optional param_name As data_type = default_value
        //
        else if (words.length == 6) {
            // byval optional param_name As data_type
            // byref optional param_name As data_type
            if (words[2].equalsIgnoreCase("as")) {
                dataType = getDataType(words[3]);
                vbParamName = words[1];
                paramName = getParamName(words[1]);
            }
            else
                return "[*TODO:** param declaration unsuported -> "
                        + words[0]+ " " + words[1] 
                        + words[2]+ " " + words[3]
                        + words[4]+ " " + words[5] + "]";
        }
        // optional byval param_name As data_type = default_value
        // optional byref param_name As data_type = default_value
        //
        else if (words.length == 7) {
            if (words[3].equalsIgnoreCase("as")) {
                dataType = getDataType(words[4]);
                vbParamName = words[2];
                paramName = getParamName(words[2]);
            }
            else
                return "[*TODO:** param declaration unsuported -> "
                        + words[0]+ " " + words[1]
                        + words[2]+ " " + words[3]
                        + words[4]+ " " + words[5]
                        + words[6]+ "]";
        }
        else
            return "[*TODO:** param declaration unsuported -> "
                    + words[0]+ " " + words[1]
                    + words[2]+ " " + words[3]
                    + words[4]+ " " + words[5] + "]";

        if (G.endLike(paramName,"()")) {
            dataType += "[]";
            paramName = paramName.substring(0, paramName.length()-2);
        }
        if (G.endLike(vbParamName, "()")) {
            vbParamName = vbParamName.substring(0, vbParamName.length()-2);
        }

        Variable var = new Variable();
        var.setJavaName(paramName);
        var.setVbName(vbParamName);
        var.setType(dataType);
        m_functionVariables.add(var);

        saveParam(vbParamName, paramName, dataType);

        return dataType + " " + paramName;
    }

    private String getParamName(String paramName) {
        return unCapitalize(paramName);
    }

    private String getVariableName(String paramName) {
        return unCapitalize(paramName);
    }

    private String unCapitalize(String word) {
        if (word.length() > 0) {
            if (word.length() > 1) {
                word = word.substring(0, 1).toLowerCase() + word.substring(1);
            }
            else {
                word = word.toLowerCase();
            }
        }
        return word;
    }

    private String getDataType(String dataType) {
        if (dataType.equalsIgnoreCase("byte")) {
            dataType = "byte";
        }
        else if (dataType.equalsIgnoreCase("boolean")) {
            if (m_translateToJava)
                dataType = "boolean";
           else
                dataType = "bool";
        }
        else if (dataType.equalsIgnoreCase("double")) {
            dataType = "double";
        }
        else if (dataType.equalsIgnoreCase("integer")) {
            dataType = "int";
        }
        else if (dataType.equalsIgnoreCase("long")) {
            // the vb6 long is 32 bit so in java it is an int
            // if we don't do this, we get a lot of errors
            // because most of the functions use int as the
            // type for their parameters and the compiler
            // complaints every time we send a long as a parameter
            // to a function which expects an integer
            //
            dataType = "int"; 
        }
        else if (dataType.equalsIgnoreCase("single")) {
            dataType = "float";
        }
        else if (dataType.equalsIgnoreCase("date")) {
            addToImportList("import java.util.Date;");
            dataType = "Date";
        }
        else if (dataType.equalsIgnoreCase("string")) {
            dataType = "String";
        }
        else if (dataType.equalsIgnoreCase("variant")) {
            dataType = getObjectTypeName();
        }
        else if (dataType.equalsIgnoreCase("object")) {
            dataType = getObjectTypeName();
        }
        else if (isADODBType(dataType)) {
            dataType = translateADODBType(dataType);
        }
        else if (isVBStandarObject(dataType)) {
            dataType = translateVBStandarObject(dataType);
        }

        // else: if is not one of the above list we return
        // the same value we received
        //
        return dataType;
    }

    private String getDefaultForReturnType(String dataType) {
        if (dataType.equalsIgnoreCase("byte")) {
            return "0";
        }
        else if (dataType.equalsIgnoreCase("boolean")) {
            return "false";
        }
        else if (dataType.equalsIgnoreCase("double")) {
            return "0";
        }
        else if (dataType.equalsIgnoreCase("int")) {
            return "0";
        }
        else if (dataType.equalsIgnoreCase("long")) {
            return "0";
        }
        else if (dataType.equalsIgnoreCase("float")) {
            return "0";
        }
        else if (dataType.equalsIgnoreCase("Date")) {
            return "null";
        }
        else if (dataType.equalsIgnoreCase("String")) {
            return "\"\"";
        }
        else if (dataType.equalsIgnoreCase("Object")) {
            return "null";
        }
        // else: if is not one of the above list we return
        // a null string
        //
        return "";
    }

    private String getZeroValueForDataType(String dataType) {
        if (dataType.equalsIgnoreCase("byte")) {
            return "0";
        }
        else if (dataType.equalsIgnoreCase("boolean")) {
            return "false";
        }
        else if (dataType.equalsIgnoreCase("double")) {
            return "0";
        }
        else if (dataType.equalsIgnoreCase("int")) {
            return "0";
        }
        else if (dataType.equalsIgnoreCase("long")) {
            return "0";
        }
        else if (dataType.equalsIgnoreCase("float")) {
            return "0";
        }
        else if (dataType.equalsIgnoreCase("Date")) {
            return "null";
        }
        else if (dataType.equalsIgnoreCase("String")) {
            return "\"\"";
        }
        else if (dataType.equalsIgnoreCase("Object")) {
            return "null";
        }
        else {
            return "null";
        }
    }

    private int getStartParams(String strLine) {
        boolean literalFlag = false;
        for (int i = 0; i < strLine.length(); i++) {
            if (strLine.charAt(i) == '"') {
                literalFlag = !literalFlag;
            }
            else if (strLine.charAt(i) == '(') {
                if (!literalFlag) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getEndParams(String strLine) {
        boolean literalFlag = false;
        int openParentheses = 0;
        for (int i = 0; i < strLine.length(); i++) {
            if (strLine.charAt(i) == '"') {
                literalFlag = !literalFlag;
            }
            if (!literalFlag) {
                if (strLine.charAt(i) == '(') {
                    openParentheses++;
                }
                else if (strLine.charAt(i) == ')') {
                    if (openParentheses > 1) {
                        openParentheses--;
                    }
                    else {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private String translatePrivateConstMember(String strLine) {
        // form is
            // private const identifier as data_type = value
            // private const identifier = value
        strLine = strLine.trim();

        int startComment = getStartComment(strLine);
        String workLine = strLine;
        String comments = "";
        if (startComment >= 0) {
            comments =  "//" + workLine.substring(startComment);
            workLine = workLine.substring(0, startComment-1);
        }

        String[] words = G.splitSpace(workLine);//workLine.split("\\s+");
        String dataType = "";
        String identifier = "";
        String constValue = "";

        // private const identifier as data_type = value
        //
        if (words.length > 5) {
            identifier = words[2];
            dataType = words[4];
            constValue = words[6];
        }
        // private const identifier = value
        //
        else if (words.length == 5) {
            identifier = words[2];
            constValue = words[4];
        }
        else {
            return "*" + strLine + newline;
        }
        if (dataType.isEmpty()) {
            if (constValue.charAt(0) == '"') {
                dataType = "String";
            }
            else if (constValue.charAt(0) == '#'){
                dataType = "Date";
            }
            else if (C_NUMBERS.contains(String.valueOf(constValue.charAt(0)))){
                dataType = "int";
            }
            else if (constValue.substring(0,2).equalsIgnoreCase("&h")) {
                dataType = "int";
                constValue = "0x" + constValue.substring(2);
            }
            else {
                IdentifierInfo info = null;
                info = getIdentifierInfo(constValue, "", false);
                if (info != null) {
                    if (info.isFunction)
                        dataType = info.function.getReturnType().dataType;
                    else
                        dataType = info.variable.dataType;
                }
                else {
                    return "*TODO:** (the data type can't be found for the value ["
                            + constValue + "])" + strLine + newline;
                }
            }
        }

        String vbIdentifier = identifier;
        identifier = identifier.toUpperCase();

        Variable var = new Variable();
        var.setVbName(vbIdentifier);
        var.setJavaName(identifier);
        var.setType(dataType);
        m_memberVariables.add(var);

        saveVariable(vbIdentifier, identifier, dataType);

        if (m_translateToJava) {
        
            return "private static final "
                    + dataType + " "
                    + identifier + " = "
                    + constValue + ";"
                    + comments + newline;
        }
        else {
        
            return "private const "
                    + dataType + " "
                    + identifier + " = "
                    + constValue + ";"
                    + comments + newline;
        }
    }

    private void saveFunction(String vbIdentifier, String identifier, String dataType) {
        m_functionObject.setClId(m_classObject.getId());
        m_functionObject.setVbName(vbIdentifier);
        m_functionObject.setJavaName(identifier);
        m_functionObject.setDataType(dataType);
        m_functionObject.getFunctionIdFromFunctionName();
        m_functionObject.saveFunction();
    }

    private void saveParam(String vbParamName, String paramName, String dataType) {
        saveVariable(vbParamName, paramName, dataType, true, false);
    }
    
    private void saveVariable(String vbIdentifier, String identifier, String dataType) {
        saveVariable(vbIdentifier, identifier, dataType, false, false);
    }

    private void saveVariable(String vbIdentifier,
                                String identifier,
                                String dataType,
                                boolean isParameter,
                                boolean isPublic) {

        m_variableObject.setClId(m_classObject.getId());
        m_variableObject.setVbName(vbIdentifier);
        m_variableObject.setJavaName(identifier);
        m_variableObject.setFunId(m_functionObject.getId());
        m_variableObject.setDataType(dataType);
        m_variableObject.setIsParameter(isParameter);
        m_variableObject.setIsPublic(isPublic);
        m_variableObject.getVariableIdFromVariableName();
        m_variableObject.saveVariable();
    }

    private void saveVariableInType(String vbIdentifier, String identifier, String dataType) {
        m_variableObject.setClId(m_typeClassObject.getId());
        m_variableObject.setVbName(vbIdentifier);
        m_variableObject.setJavaName(identifier);
        m_variableObject.setFunId(Db.CS_NO_ID);
        m_variableObject.setDataType(dataType);
        m_variableObject.setIsParameter(false);
        m_variableObject.setIsPublic(true);
        m_variableObject.getVariableIdFromVariableName();
        m_variableObject.saveVariable();
    }

    private void saveVariableInEnum(String vbIdentifier, String identifier, String dataType) {
        m_variableObject.setClId(m_enumClassObject.getId());
        m_variableObject.setVbName(vbIdentifier);
        m_variableObject.setJavaName(identifier);
        m_variableObject.setFunId(Db.CS_NO_ID);
        m_variableObject.setDataType(dataType);
        m_variableObject.setIsParameter(false);
        m_variableObject.setIsPublic(true);
        m_variableObject.getVariableIdFromVariableName();
        m_variableObject.saveVariable();
    }

    private String translatePublicConstMember(String strLine) {
        // form is
            // dim variable_name as data_type
        strLine = strLine.trim();
        String[] words = G.splitSpace(strLine);//strLine.split("\\s+");
        String dataType = "";
        String identifier = "";
        String constValue = "";
        String misc = "";

        if (words.length > 2) {
            identifier = words[2];
            if (words.length >= 4) {
                if (words[4].equals("=")) {
                    if (words.length > 4) {
                        dataType = words[4];
                    }
                    if (words.length >= 5) {
                        constValue = words[5];
                    }
                    for (int i = 6; i < words.length; i++) {
                        misc += " " + words[i];
                    }
                }
                else {
                    if (words.length >= 4) {
                        constValue = words[4];
                    }
                    for (int i = 5; i < words.length; i++) {
                        misc += " " + words[i];
                    }
                }
            }
            else {
                return "*" + strLine + newline;
            }
        }
        else {
            return "*" + strLine + newline;
        }
        if (dataType.isEmpty()) {
            if (constValue.charAt(0) == '"') {
                dataType = "String";
            }
            else if (constValue.charAt(0) == '#'){
                dataType = "Date";
            }
            else if (C_NUMBERS.contains(String.valueOf(constValue.charAt(0)))){
                dataType = "int";
            }
            else if (constValue.substring(0,2).equalsIgnoreCase("&h")) {
                dataType = "int";
                constValue = "0x" + constValue.substring(2);
            }
            else {
                IdentifierInfo info = null;
                info = getIdentifierInfo(constValue, "", false);
                if (info != null) {
                    if (info.isFunction)
                        dataType = info.function.getReturnType().dataType;
                    else
                        dataType = info.variable.dataType;
                }
                else {
                    return "*TODO:** (the data type can't be found for the value ["
                            + constValue + "])" + strLine + newline;
                }
            }
        }

        String vbIdentifier = identifier;
        identifier = identifier.toUpperCase();

        Variable var = new Variable();
        var.setVbName(vbIdentifier);
        var.setJavaName(identifier);
        var.setType(dataType);
        m_memberVariables.add(var);

        saveVariable(vbIdentifier, identifier, dataType);

        if (m_translateToJava) {

            return "public static final " + dataType + " " + identifier + " = "
                    + constValue + ";" + misc + newline;
        }
        else {

            return "public const " + dataType + " " + identifier + " = "
                    + constValue + ";" + misc + newline;
        }
    }

    private String translatePrivateMember(String strLine) {
        // form is
            // dim variable_name as data_type
        strLine = strLine.trim();
        String[] words = G.splitSpace(strLine);//strLine.split("\\s+");
        String dataType = "";
        String identifier = "";
        String vbIdentifier = "";
        String misc = "";
        boolean isEventGenerator = false;

        if (words.length > 1) {
            vbIdentifier = words[1];

            // with events eg:
            //      private withevents my_obj_with_events as CObjetWithEvents ' some comments
            //      0       1           2                 3        4          >= 5
            //
            if (vbIdentifier.equalsIgnoreCase("WithEvents")) {
                vbIdentifier = words[2];
                identifier = getIdentifier(vbIdentifier);
                if (words.length > 4) {
                    dataType = words[4];
                }
                for (int i = 5; i < words.length; i++) {
                    misc += " " + words[i] ;
                }
                isEventGenerator = true;
            }
            else {
                identifier = getIdentifier(vbIdentifier);
                if (words.length > 3) {
                    dataType = words[3];
                }
                for (int i = 4; i < words.length; i++) {
                    misc += " " + words[i] ;
                }
            }
        }
        boolean isArray = false;
        if (!identifier.isEmpty()) {
            isArray = identifier.endsWith("()");
            if (isArray) {
                identifier = identifier.substring(0, identifier.length() - 2);
            }
            Variable var = new Variable();
            var.setVbName(vbIdentifier);
            var.setJavaName(identifier);
            var.setType(dataType);
            var.isArray = isArray;
            var.isEventGenerator = isEventGenerator;
            if (isEventGenerator) {
                addToEventListeners(vbIdentifier,
                                    var.getJavaName(),
                                    dataType,
                                    getEventMacroName(var.getJavaName()));
            }
            m_memberVariables.add(var);
        }
        if (dataType.isEmpty()) {
            dataType = getObjectTypeName();
        }
        dataType = getDataType(dataType);

        saveVariable(vbIdentifier, identifier, dataType);

        String modifiers = "";
        if (m_isBasFile)
            modifiers = "static ";
        
        if (isArray)
            return "private " + modifiers + dataType + "[] " + identifier + getInitialValueForType(dataType) + ";" + misc + newline;
        else
            return "private " + modifiers + dataType + " " + identifier + getInitialValueForType(dataType) + ";" + misc + newline;
    }

    private String translatePublicMember(String strLine) {
        // form is
            // dim variable_name as data_type
        strLine = strLine.trim();
        String[] words = G.splitSpace(strLine);//strLine.split("\\s+");
        String dataType = "";
        String identifier = "";
        String vbIdentifier = "";
        String misc = "";
        boolean isEventGenerator = false;

        if (words.length > 1) {
            vbIdentifier = words[1];
            // with events eg:
            //      private withevents my_obj_with_events as CObjetWithEvents ' some comments
            //      0       1           2                 3        4          >= 5
            //
            if (vbIdentifier.equalsIgnoreCase("WithEvents")) {
                vbIdentifier = words[2];
                identifier = getIdentifier(vbIdentifier);
                if (words.length > 4) {
                    dataType = words[4];
                }
                for (int i = 5; i < words.length; i++) {
                    misc += " " + words[i] ;
                }
                isEventGenerator = true;
            }
            else {
                identifier = getIdentifier(vbIdentifier);
                if (words.length > 3) {
                    dataType = words[3];
                }
                for (int i = 4; i < words.length; i++) {
                    misc += " " + words[i] ;
                }
            }
        }
        if (!identifier.isEmpty()) {
            Variable var = new Variable();
            var.setVbName(vbIdentifier);
            var.setJavaName(identifier);
            var.setType(dataType);
            var.isEventGenerator = isEventGenerator;
            if (isEventGenerator) {
                addToEventListeners(vbIdentifier,
                                    var.getJavaName(),
                                    dataType,
                                    getEventMacroName(var.getJavaName()));
            }
            m_memberVariables.add(var);
        }
        if (dataType.isEmpty()) {
            dataType = getObjectTypeName();
        }
        dataType = getDataType(dataType);

        saveVariable(vbIdentifier, identifier, dataType, false, true);

        boolean isArray = identifier.endsWith("()");
        if (isArray) {
            identifier = identifier.substring(0, identifier.length() - 2);
        }

        Variable var = new Variable();
        var.setVbName(vbIdentifier);
        var.setJavaName(identifier);
        var.packageName = m_packageName;
        var.setType(dataType);
        var.isPublic = true;
        var.isArray = isArray;
        m_publicVariables.add(var);

        String modifiers = "";
        if (m_isBasFile)
            modifiers = "static ";
        
        if (isArray)
            return "public " + modifiers + dataType + "[] " + identifier + getInitialValueForType(dataType) + ";" + misc + newline;
        else
            return "public " + modifiers + dataType + " " + identifier + getInitialValueForType(dataType) + ";" + misc + newline;
    }

    private String getInitialValueForType(String dataType) {
        String iniValue = getDefaultForReturnType(dataType);
        if (!iniValue.isEmpty())
            iniValue = " = " + iniValue;
        return iniValue;
    }

    private String getIdentifier(String word) {
        String identifier = "";
        if (word.length() > 2 ) {
            if (word.substring(0,2).equals("m_")) {
                identifier = word.substring(0,3).toLowerCase();
                if (word.length() > 3 ) {
                    identifier += word.substring(3);
                }
            }
            else{
                identifier = word;
            }
        }
        else {
            identifier = word;
        }
        return identifier;
    }

    // we need three elements in custom events
    //  -- event class
    //  -- event listener interface
    //  -- event generator
    //
    // a) when the class which we are translating has public events
    // (private events doesn't have sense) we have to create the
    // event listener interface with a method for every public event and
    // a class which implements the event listener interface as and adapter
    // class (to free the listener to implement all the methods of the interface).
    // the listeners will extend the adapter class as an inner anonymous class.
    // the interface name will be named as the class plus the
    // postfix EventI eg: for a class named in vb6 code as cReport the interface
    // will be CReportEventI (remeber that every class will be capitalized)
    // and the adapter will be CReportEventA
    //
    // b) when the class which we are translating is the event listener
    // it has to declare an anonymous inner classes which extend
    // the adapter class (which implement the event listener
    // interface) for every variable which raises events.
    //
    // c) in visual basic 6 you need to instantiate the member variable
    // which generate events with an explicit assignment like
    //
    //      set m_event_generator = new ClassEventGenerator
    // or
    //      set m_event_generator = already_instantiated_event_generator
    //
    // we have to add after that point a call to the addListener method of the
    // event generator object.
    //
    // the problem is that we are translating in one read of the content
    // line by line from up to down and so at the point of this asignment
    // line we can't be sure that we know every event our class is
    // intrested to listen to. for this reason we need to reach the end
    // of the file to be sure we know all the code related to events of 
    // a "with events variable".
    //
    // to fix it we will add a macro to be replace after translating the class
    // with the definition of the anonymous inner class for every "with events
    // variable". this macro will be:
    //          __ADD_TO_LISTENER_name_of_the_generator_variable__
    //
    //  eg: if the generetor is m_report the macro will be
    //
    //          __ADD_TO_LISTENER_m_report__
    //
    private void addToEventListeners(String eventGeneratorVb,
            String eventGeneratorJava,
            String className,
            String eventMacro) {
        EventListener eventListener = new EventListener();
        eventListener.setGeneratorVb(eventGeneratorVb);
        eventListener.setGeneratorJava(eventGeneratorJava);
        eventListener.setAdapter(className + C_ADAPTER_POSTIFX);
        eventListener.setEventMacro(eventMacro);
        m_eventListeners.add(eventListener);
    }

    private String getEventMacroName(String variable) {
        return "__ADD_TO_LISTENER_" + variable + "__";
    }

    private String translateDim(String strLine) {
        // form is
            // dim variable_name as data_type
        strLine = strLine.trim();
        String[] words = G.splitSpace(strLine);//strLine.split("\\s+");
        String dataType = "";
        String vbIdentifier = "";
        String identifier = "";
        String misc = "";

        if (words.length > 1) {
            vbIdentifier = words[1];
            identifier = getVariableName(words[1]);
            if (words.length > 3) {
                dataType = words[3];
            }
            for (int i = 4; i < words.length; i++) {
                misc += " " + words[i] ;
            }
        }
        if (dataType.isEmpty()) {
            dataType = getObjectTypeName();
        }
        dataType = getDataType(dataType);

        Variable var = new Variable();
        var.setJavaName(identifier);
        var.setVbName(vbIdentifier);
        var.setType(dataType);
        m_functionVariables.add(var);

        if (identifier.contains("(")) {
            words = G.split(identifier);
            if (words.length > 3) {
                dataType += "[" + words[2] + "]";
            }
            else {
                dataType += "[]";
            }
            return dataType + " " + identifier + " = null;" + misc + newline;
        }
        else {
            return dataType + " " + identifier + " = "
                    + getZeroValueForDataType(dataType) + ";" + misc + newline;
        }
    }

    private String removeExtraSpaces(String strLine) {
        String rtn = "";
        boolean previousWasAChar = false;
        for (int i = 0; i < strLine.length(); i++) {
            if (strLine.charAt(i) == ' ') {
                if (!previousWasAChar) {
                    rtn += " ";
                    previousWasAChar = true;
                }
            }
            else {
                rtn += strLine.charAt(i);
                previousWasAChar = false;
            }
        }
        return rtn;
    }

    public void initDbObjects() {
        m_classObject = new ClassObject();
        m_functionObject = new FunctionObject();
        m_variableObject = new VariableObject();
        m_typeClassObject = new ClassObject();
        m_enumClassObject = new ClassObject();
        createADODBClasses();
        createVBClasses();
    }

    public void initTranslator(String name) {
        m_isVbSource = false;
        m_codeHasStarted = false;
        m_attributeBlockHasStarted = false;
        m_inFunction = false;
        m_inEnum = false;
        m_inWith = false;
        m_withDeclaration = false;
        m_endWithDeclaration = false;
        m_type = "";
        m_vbClassName = "";
        m_javaClassName = "";
        m_collTypes.removeAll(m_collTypes);
        m_collEnums.removeAll(m_collEnums);
        m_collWiths.removeAll(m_collWiths);
        m_eventListeners.removeAll(m_eventListeners);
        m_memberVariables.removeAll(m_memberVariables);
        m_functionVariables.removeAll(m_functionVariables);
        m_raiseEvents = false;
        m_privateFunctions = new ArrayList<Function>();
        m_publicFunctions = new ArrayList<Function>();
        m_publicVariables = new ArrayList<Variable>();
        m_raiseEventFunctions = new ArrayList<String>();
        m_types = new ArrayList<Type>();
        m_tabCount = 0;
        m_listenerInterface = "";
        m_adapterClass = "";
        m_addDateAuxFunction = false;
        m_addParseDateAuxFunction = false;
        m_addIsNumericAuxFunction = false;
        m_addRedimAuxFunction = false;
        m_addRedimPreserveAuxFunction = false;
        m_returnValue = "";
        m_imports = new String[100];
        m_importCount = 0;
        m_setReturnValueFound = false;
        m_needReturnVariable = false;
        m_function = null;
        m_isBasFile = false;
        m_onErrorFound = false;
        m_onErrorResumeNext = false;
        m_onErrorLabel = "";
        m_onCatchBlock = false;

        if (name.contains(".")) {
            if (name.length() > 0) {
                String ext = name.substring(name.length()-3).toLowerCase();
                if ( ext.equals("bas") || ext.equals("cls") || ext.equals("frm") ) {
                    m_isVbSource = true;
                }
                m_isBasFile = ext.equals("bas");
            }
        }
    }

    private void saveTypeClassInDB(String className) {
        int i = className.indexOf("'");
        if (i > 0) {
            className = className.substring(0, i - 1).trim();
        }
        i = className.indexOf(" ");
        if (i > 0) {
            className = className.substring(0, i - 1).trim();
        }        
        m_typeClassObject.setPackageName(m_packageName);
        m_typeClassObject.setVbName(className);
        m_typeClassObject.setJavaName(className);
        m_typeClassObject.getClassIdFromClassName();
        m_typeClassObject.saveClass();
    }

    private void saveEnumClassInDB(String className, boolean isPublic) {
        int i = className.indexOf("'");
        if (i > 0) {
            className = className.substring(0, i - 1).trim();
        }
        i = className.indexOf(" ");
        if (i > 0) {
            className = className.substring(0, i - 1).trim();
        }
        m_enumClassObject.setPackageName(m_packageName);
        m_enumClassObject.setVbName(className);
        m_enumClassObject.setJavaName(className);
        m_enumClassObject.getClassIdFromClassName();
        if (isPublic) {
            m_enumClassObject.setIsPublicEnum(true);
            m_enumClassObject.setEnumParentClass("");
        }
        else {
            m_enumClassObject.setIsPublicEnum(false);
            m_enumClassObject.setEnumParentClass(m_javaClassName);
        }
        m_enumClassObject.saveClass();
    }

    private void addToType(String strLine) {
        String className = "";
        strLine = G.ltrim(strLine);
        int startComment = getStartComment(strLine);
        String comments = "";
        if (startComment >= 0) {
            comments =  "//" + strLine.substring(startComment);
            strLine = strLine.substring(0, startComment-1);
        }

        if (strLine.length() > 5) {
            if (strLine.substring(0,5).toLowerCase().equals("type ")) {
                Type type = new Type();
                type.vbName = strLine.substring(5);
                className = type.vbName;
                type.javaName = className;
                type.getVbCode().append(strLine);
                m_types.add(type);
                m_type += "private class " + className + " {" + comments + newline;
                saveTypeClassInDB(className);
                return;
            }
        }

        if (strLine.length() > 12) {
            if (strLine.substring(0,12).toLowerCase().equals("public type ")) {
                Type type = new Type();
                type.isPublic = true;
                type.vbName = strLine.substring(12);
                className = type.vbName;
                type.javaName = className;
                type.getVbCode().append(strLine);
                m_types.add(type);
                m_type += "public class " + className + " {" + comments + newline;
                saveTypeClassInDB(className);
                return;
            }
        }

        if (strLine.length() > 13) {
            if (strLine.substring(0,13).toLowerCase().equals("private type ")) {
                Type type = new Type();
                type.vbName = strLine.substring(13);
                className = type.vbName;
                type.javaName = className;
                type.getVbCode().append(strLine);
                m_types.add(type);
                m_type += "private class " + className + " {" + comments + newline;
                saveTypeClassInDB(className);
                return;
            }
        }

        if (G.beginLike(strLine, "end type")) {
            m_inType = false;
            m_type += "}" + comments + newline + newline;
            m_collTypes.add(m_type);
            Type type = m_types.get(m_types.size()-1);
            type.getVbCode().append(strLine);
            type.getJavaCode().append(m_type);
            m_type = "";
            if (type.isPublic)
                m_caller.addPublicType(type);
        }
        else {
            String dataType = "";
            String identifier = "";

            Type type = m_types.get(m_types.size()-1);
            type.getVbCode().append(strLine);

            strLine = strLine.trim();
            String[] words = G.splitSpace(strLine);//strLine.split("\\s+");

            if (words.length > 0) {

                // check to see if it is an array
                //
                if (words[0].contains("(")) {

                    String size = words[0].substring(words[0].indexOf("(")+1);
                    if (size.equals(")")) {
                        dataType = getObjectTypeName() + "[]";
                    }
                    else {
                        if (words.length >= 3) {
                            if (words[1].equalsIgnoreCase("to")) {

                                int lowBound = Integer.parseInt(size);
                                size = words[2].substring(0,words[2].length()-1);
                                String upperBound = size;
                                if (lowBound == 1) {
                                    size = upperBound;
                                }
                                else {
                                    size = upperBound + " - " + ((Integer)(lowBound-1)).toString();
                                }

                                // complete sentence eg: type_member(low_bound to upper_bound) as data_type
                                //                       |                   | |  |          | |  |
                                //                       1                     2  3            4  5
                                //
                                if (words.length >= 5) {
                                    if (words[4].charAt(0) =='\'') {
                                        dataType = getObjectTypeName() + "[" + size + "]";
                                    }
                                    else {
                                        dataType = words[4] + "[" + size + "]";
                                    }
                                }
                                else {
                                    dataType = getObjectTypeName() + "[" + size + "]";
                                }
                            }
                            else {
                                size = words[0].substring(words[0].indexOf("(")+1);
                                size = size.substring(0, size.length()-1);
                                dataType = words[2] + "[" + size + "]";
                            }
                        }
                        // variant array like: type_member(dimension)
                        //
                        else {
                            size = words[0].substring(words[0].indexOf("(")+1);
                            size = size.substring(0, size.length()-1);
                            dataType = words[2] + "[" + size + "]";
                        }
                    }
                    identifier = words[0].substring(0, words[0].indexOf("("));
                }
                else {
                    // complete sentence eg: type_member as data_type
                    //
                    if (words.length >= 3) {

                        if (words[1].charAt(0) =='\'') {
                            dataType = getObjectTypeName();
                        }
                        else {
                            if (words[2].charAt(0) =='\'') {
                                dataType = getObjectTypeName();
                            }
                            else {
                                dataType = words[2];
                            }
                        }
                    }
                    // implicit sentence eg: type_member {no declaration of type}
                    //
                    else {
                        dataType = getObjectTypeName();
                    }
                    identifier = words[0];
                }

                String vbIdentifier = identifier;
                if (!identifier.isEmpty()) {
                    if (identifier.length() > 2)
                        identifier = identifier.substring(0, 1).toLowerCase() + identifier.substring(1);
                    else
                        identifier = identifier.substring(0, 1).toLowerCase();
                }

                saveVariableInType(vbIdentifier, identifier, dataType);

                Variable var = new Variable();
                var.setVbName(vbIdentifier);
                var.setJavaName(identifier);
                var.setType(dataType);
                var.isPublic = true;
                type.getMembersVariables().add(var);

                m_type += "    public " + dataType + ' ' + identifier + ";" + comments + newline;
            } 
            else {
                m_type += strLine;
            }
        }
    }

    private void addToEnum(String strLine) {
        strLine = G.ltrim(strLine);

        if (strLine.length() > 5) {
            if (strLine.substring(0, 5).toLowerCase().equals("enum ")) {
                String enumClass = strLine.substring(5);
                saveEnumClassInDB(enumClass, m_isBasFile);
                if (m_translateToJava)
                    m_enum += "private class " + enumClass + " {" + newline;
                else
                    m_enum += "private enum " + enumClass + " {" + newline;
                return;
            }
        }

        if (strLine.length() > 12) {
            if (strLine.substring(0, 12).toLowerCase().equals("public enum ")) {
                String enumClass = strLine.substring(12);
                saveEnumClassInDB(enumClass, true);
                if (m_translateToJava)
                    m_enum += "public class " + enumClass + " {" + newline;
                else
                    m_enum += "public enum " + enumClass + " {" + newline;
                return;
            }
        }

        if (strLine.length() > 13) {
            if (strLine.substring(0, 13).toLowerCase().equals("private enum ")) {
                String enumClass = strLine.substring(13);
                saveEnumClassInDB(enumClass, false);
                if (m_translateToJava)
                    m_enum += "private class " + enumClass + " {" + newline;
                else
                    m_enum += "private enum " + enumClass + " {" + newline;
                return;
            }
        }

        if (G.beginLike(strLine,"end enum")) {
            m_inEnum = false;
            int lastColon = 0;
            for (int i = 0; i < m_enum.length(); i++) {
                if (m_enum.charAt(i) == ',') {
                    lastColon = i;
                }
                else if (m_enum.charAt(i) == '\'') {
                    break;
                }
            }
            if (lastColon > 0) {
                m_enum = m_enum.substring(0,lastColon) + m_enum.substring(lastColon+1);
            }
            m_enum += "}" + newline + newline;
            m_collEnums.add(m_enum);
            m_enum = "";
        }
        else {
            String constValue = "";
            String identifier = "";
            String misc = "";

            strLine = strLine.trim();
            String[] words = G.splitSpace(strLine);//strLine.split("\\s+");

            if (words.length > 0) {

                identifier = words[0];
                
                // complete sentence eg: enum_member = enum_value
                //
                if (words.length >= 3) {
                    if (words[1].charAt(0) == '=') {
                        constValue = words[2];
                    }
                }
                // implicit sentence eg: enum_member {no declaration of value}
                //
                else {
                    int lenIdentifier = identifier.length();
                    if (strLine.length() > lenIdentifier) {
                        misc = "//" + strLine.substring(lenIdentifier);
                    }
                }

                if (constValue.isEmpty()) {
                    if (m_translateToJava)
                        m_enum += "    public static final int " + identifier.toUpperCase() 
                                + ";" + misc + newline;
                    else
                        m_enum += "    " + identifier.toUpperCase() 
                                + "," + misc + newline;
                }
                else {
                    if (constValue.length() > 2) {
                        if (constValue.substring(0, 2).equalsIgnoreCase("&h")) {
                            constValue = "0x" + constValue.substring(2);
                        }
                    }
                    if (m_translateToJava)
                        m_enum += "    public static final int " + identifier.toUpperCase() + " = "
                                + constValue + ";" + misc + newline;
                    else
                        m_enum += "    " + identifier.toUpperCase() + " = "
                                + constValue + "," + misc + newline;
                }
                saveVariableInEnum(identifier, identifier.toUpperCase(), "int");
            }
            else {
                m_enum += strLine;
            }
        }
    }

    public String getSubClasses() {
        String subClasses = "";
        for (int i = 0; i < m_collTypes.size(); i++) {
            subClasses += m_collTypes.get(i) + newline;
        }
        for (int i = 0; i < m_collEnums.size(); i++) {
            subClasses += m_collEnums.get(i) + newline;
        }
        return subClasses;
    }

    public void addEventListenerInterface() {
        if (!m_listenerInterface.isEmpty())
            m_caller.addClass(m_javaClassName + C_INTERFACE_POSTIFX,
                    "public interface "
                    + m_javaClassName
                    + C_INTERFACE_POSTIFX + " {"
                    + newline
                    + m_listenerInterface
                    + "}");
    }

    public void addEventListenerAdapter() {
        if (!m_listenerInterface.isEmpty())
            m_caller.addClass(m_javaClassName + C_ADAPTER_POSTIFX,
                    "public class "
                    + m_javaClassName
                    + C_ADAPTER_POSTIFX + " implements "
                    + m_javaClassName
                    + C_INTERFACE_POSTIFX + " {"
                    + newline
                    + m_adapterClass
                    + "}");
    }

    public void implementListeners(StringBuilder code) {
        
        Iterator itrListener = m_eventListeners.iterator();
        while(itrListener.hasNext()) {
            EventListener listener = (EventListener)itrListener.next();
            String innerClass = listener.getAnonymousInnerClass();
            String callToAddListener = listener.getGeneratorJava() + ".addListener("
                                        + innerClass + ")";
            int i = code.indexOf(listener.getEventMacro());
            if (i > 0) {
                int j = i + listener.getEventMacro().length();
                code.replace(i, j, callToAddListener);
            }
        }
    }

    private void checkBeginBlock(String strLine) {
        m_wasSingleLineIf = false;
        strLine = G.ltrimTab(strLine);

        if (m_onCatchBlock) {
            m_tabCount++;
            m_onCatchBlock = false;
        }
        // If
        //
        else if (G.beginLike(strLine, "If ")) {
            int startComment = getStartComment(strLine);
            if (startComment >= 0) {
                strLine = strLine.substring(0, startComment-1);
            }
            strLine = strLine.trim();
            if (G.endLike(strLine, " Then")) {
                m_tabCount++;
                m_wasSingleLineIf = false;
            }
            else {
                m_wasSingleLineIf = true;
            }
        }
        // Else If
        //
        else if (G.beginLike(strLine, "ElseIf ")) {
            int startComment = getStartComment(strLine);
            if (startComment >= 0) {
                strLine = strLine.substring(0, startComment-1);
            }
            strLine = strLine.trim();
            if (G.endLike(strLine, " Then")) {
                m_tabCount++;
                m_wasSingleLineIf = false;
            }
            else {
                m_wasSingleLineIf = true;
            }
        }
        // Else
        //
        else if (G.beginLike(strLine, "Else ")) {
            int startComment = getStartComment(strLine);
            if (startComment >= 0) {
                strLine = strLine.substring(0, startComment-1);
            }
            strLine = strLine.trim();
            if (strLine.trim().equalsIgnoreCase("Else")) {
                m_tabCount++;
                m_wasSingleLineIf = false;
            }
            else {
                m_wasSingleLineIf = true;
            }
        }
        // Else
        //
        else if (strLine.trim().equalsIgnoreCase("Else")) {
            m_tabCount++;
        }
        // For
        //
        else if (G.beginLike(strLine, "For ")) {
            m_tabCount++;
        }
        // While
        //
        else if (G.beginLike(strLine, "While ")) {
            m_tabCount++;
        }
        // Do
        //
        else if (G.beginLike(strLine, "Do ")) {
            m_tabCount++;
        }
        // With
        //
        else if (G.beginLike(strLine, "With ")) {
            m_tabCount++;
        }
        // Select Case
        //
        else if (G.beginLike(strLine, "Select Case ")) {
            m_tabCount+=2;
        }
        // Case
        //
        else if (G.beginLike(strLine, "Case ")) {
            m_tabCount++;
        }
        // Public Function
        //
        else if (G.beginLike(strLine, "Public Function ")) {
            m_tabCount++;
        }
        // Private Function
        //
        else if (G.beginLike(strLine, "Private Function ")) {
            m_tabCount++;
        }
        // Public Sub
        //
        else if (G.beginLike(strLine, "Public Sub ")) {
            m_tabCount++;
        }
        // Private Sub
        //
        else if (G.beginLike(strLine, "Private Sub ")) {
            m_tabCount++;
        }
        // Function
        //
        else if (G.beginLike(strLine, "Function ")) {
            m_tabCount++;
        }
        // Sub
        //
        else if (G.beginLike(strLine, "Sub ")) {
            m_tabCount++;
        }
        // Public Property
        //
        else if (G.beginLike(strLine, "Public Property ")) {
            m_tabCount++;
        }
        // Private Property
        //
        else if (G.beginLike(strLine, "Private Property ")) {
            m_tabCount++;
        }
        // Property
        //
        else if (G.beginLike(strLine, "Property ")) {
            m_tabCount++;
        }
        // Friend Function
        //
        else if (G.beginLike(strLine, "Friend Function ")) {
            m_tabCount++;
        }
        // Friend Sub
        //
        else if (G.beginLike(strLine, "Friend Sub ")) {
            m_tabCount++;
        }
        // Friend Property
        //
        else if (G.beginLike(strLine, "Friend Property ")) {
            m_tabCount++;
        }
        // On error
        //
        else if (G.beginLike(strLine, "On Error ")) {
            m_tabCount++;
            m_onErrorFound = true;
        }
    }

    private void checkEndBlock(String strLine) {
        strLine = G.ltrimTab(strLine);
        
        int startComment = getStartComment(strLine);
        if (startComment >= 0) {
            strLine = strLine.substring(0, startComment-1);
        }
        
        if (strLine.trim().equalsIgnoreCase("End If")) {
            m_tabCount--;
        }
        // ElseIf
        //
        else if (G.beginLike(strLine, "ElseIf ")) {
            //if (G.endLike(strLine, " Then")) {
            //    m_tabCount--;
            //}
            //if (!m_wasSingleLineIf) m_tabCount--;
            m_tabCount--;
        }
        // Else
        //
        else if (G.beginLike(strLine, "Else ")) {
            //if (!m_wasSingleLineIf) m_tabCount--;
            m_tabCount--;
        }
        // Else
        //
        else if (strLine.trim().equalsIgnoreCase("Else")) {
            //if (!m_wasSingleLineIf) m_tabCount--;
            m_tabCount--;
        }
        // End Select
        //
        else if (strLine.trim().equalsIgnoreCase("End Select")) {
            m_tabCount-=2;
        }
        // Case
        //
        else if (G.beginLike(strLine, "Case ")) {
            m_tabCount--;
        }
        // End With
        //
        else if (strLine.trim().equalsIgnoreCase("End With")) {
            m_tabCount--;
        }
        // Loop
        //
        else if (G.beginLike(strLine, "Loop ")) {
            m_tabCount--;
        }
        else if (strLine.trim().equalsIgnoreCase("Loop")) {
            m_tabCount--;
        }
        // Wend
        //
        else if (strLine.trim().equalsIgnoreCase("Wend")) {
            m_tabCount--;
        }
        // Next
        //
        else if (G.beginLike(strLine, "Next ")) {
            m_tabCount--;
        }
        // Next
        //
        else if (strLine.trim().equalsIgnoreCase("Next")) {
            m_tabCount--;
        }
        // End Function
        //
        else if (strLine.trim().equalsIgnoreCase("End Function")) {
            m_tabCount--;
            if (m_onErrorFound)
                m_tabCount--;
        }
        // End Sub
        //
        else if (strLine.trim().equalsIgnoreCase("End Sub")) {
            m_tabCount--;
            if (m_onErrorFound)
                m_tabCount--;
        }
        // End Property
        //
        else if (strLine.trim().equalsIgnoreCase("End Property")) {
            m_tabCount--;
            if (m_onErrorFound)
                m_tabCount--;
        }
        // Label Error
        //
        else if (isOnErrorLabelSentence(strLine)) {
            m_tabCount--;
            m_onCatchBlock = true;
        }
        // End On Error
        //
        else if (isOnErrorSentence(strLine)) {
            if (m_onErrorFound)
                m_tabCount--;
        }

        if (m_tabCount < 1) {
            m_tabCount = 1;
        }
    }

    private Boolean isEndFunction(String strLine) {
        strLine = G.ltrimTab(strLine);

        int startComment = getStartComment(strLine);
        if (startComment >= 0) {
            strLine = strLine.substring(0, startComment-1);
        }
        // En Function
        //
        if (strLine.trim().equalsIgnoreCase("End Function")) {
            return true;
        }
        // End Sub
        //
        else if (strLine.trim().equalsIgnoreCase("End Sub")) {
            return true;
        }
        // End Property
        //
        else if (strLine.trim().equalsIgnoreCase("End Property")) {
            return true;
        }
        else
            return false;
    }

    private String getTabs() {
        return G.rep(' ', m_tabCount * 4);
    }

    private String removeLineNumbers(String strLine) {
        boolean isNumber = true;
        strLine = G.ltrimTab(strLine);
        String[] words = G.splitSpace(strLine);//strLine.split("\\s+");

        if (words.length > 0) {
            for (int i = 0; i < words[0].length(); i++) {
                if(!C_NUMBERS.contains(String.valueOf(words[0].charAt(i)))) {
                    isNumber = false;
                    break;
                }
            }
            if (isNumber) {
                return strLine.replaceFirst(words[0], "");
            }
            else {
                return strLine;
            }
        }
        else {
            return strLine;
        }
    }

    private boolean isStringIdentifier(String identifier) {
        return isXTypeIdentifier(identifier, "String");
    }
    private boolean isNumericIdentifier(String identifier) {
        return isXTypeIdentifier(identifier, "@numeric");
    }
    private boolean isXTypeIdentifier(String identifier, String type) {
        // first we evaluate constants expressions
        //
        if (type.equals("String")) {
            if (identifier.startsWith("\"")) {
                identifier = removeLiterals(identifier);
                if (identifier.length() == 0)
                    return true;
            }
        }
        if (type.equals("@numeric")) {
            if ("-".equals(identifier.substring(0, 1))) {
                if (identifier.length() < 2) {
                    return false;
                }
                else if ("1234567890".contains(identifier.substring(2, 3))) {
                    return true;
                }
            }
            else {
                if ("1234567890".contains(identifier.substring(0, 1))) {
                    return true;
                }
            }
        }

        // if is not a constant we look for the type of the variable or
        // function
        //
        IdentifierInfo info = null;
        String varType = "";
        String parent = "";
        String[] words = G.split2(identifier, "\t/*-+ .()");
        identifier = "";
        String[] parents = new String[30]; // why 30? who nows :P, 30 should be enough :)
        int openParentheses = 0;

        for (int i = 0; i < words.length; i++) {
            if (!(",.()\"'".contains(words[i]))) {
                info = getIdentifierInfo(words[i], parent, false);
                if (info == null)
                    varType = "";
                else if (info.isFunction) {
                    varType = info.function.getReturnType().dataType;
                }
                else {
                    varType = info.variable.dataType;
                }
                parent = varType;
            }
            else if (words[i].equals("(")) {
                parents[openParentheses] = parent;
                parent = "";
                openParentheses++;
            }
            else if (words[i].equals(")")) {
                openParentheses--;
                parent = parents[openParentheses];
            }
            else if (words[i].equals(" ")) {
                parent = "";
            }
            
        }
        if (type.equals("@numeric")) {
            return C_NUMERIC_DATA_TYPES.contains(varType.toLowerCase());
        }
        else
            return varType.toLowerCase().equals(varType.toLowerCase());
    }

    private void showError(String msg) {
        msg = "module: " + m_vbClassName + newline + msg;
        msg = "function: " + m_vbFunctionName + newline + msg;
        //G.showInfo(msg);
    }

    // ADODB
    //
    private String replaceADODBSentence(String strLine) {
        Preference pref = PreferenceObject.getPreference(G.C_AUX_ADO_REPLACE_ID);
        if (pref != null) {
            if (!pref.getValue().equals("0")) {
                strLine = replaceADODBConnection(strLine);
                strLine = replaceADODBRecordSet(strLine);
                strLine = replaceADODBFields(strLine);
                strLine = replaceADODBField(strLine);
                //strLine = translateADODBBofAndEof(strLine);
            }
        }
        return strLine;
    }

    private String replaceADODBConnection(String strLine) {
        return strLine;
    }
    private String replaceADODBRecordSet(String strLine) {
        return strLine;
    }
    private String replaceADODBFields(String strLine) {
        return strLine;
    }
    private String replaceADODBField(String strLine) {
        return strLine;
    }
    private boolean isADODBType(String dataType) {
        if (dataType.equalsIgnoreCase("ADODB.Connection")) {
            return true;
        }
        else if (dataType.equalsIgnoreCase("Connection")) {
            return true;
        }
        else if (dataType.equalsIgnoreCase("ADODB.RecordSet")) {
            return true;
        }
        else if (dataType.equalsIgnoreCase("RecordSet")) {
            return true;
        }
        else if (dataType.equalsIgnoreCase("ADODB.Fields")) {
            return true;
        }
        else if (dataType.equalsIgnoreCase("Fields")) {
            return true;
        }
        else if (dataType.equalsIgnoreCase("ADODB.Field")) {
            return true;
        }
        else if (dataType.equalsIgnoreCase("Field")) {
            return true;
        }
        else {
            return false;
        }
    }
    private String translateADODBType(String dataType) {
        if (dataType.equalsIgnoreCase("ADODB.Connection")) {
            addToImportList("import java.sql.Connection;");
            return "Connection";
        }
        else if (dataType.equalsIgnoreCase("Connection")) {
            addToImportList("import java.sql.Connection;");
            return "Connection";
        }
        else if (dataType.equalsIgnoreCase("ADODB.RecordSet")) {
            return "DBRecordSet";
        }
        else if (dataType.equalsIgnoreCase("RecordSet")) {
            return "DBRecordSet";
        }
        else if (dataType.equalsIgnoreCase("ADODB.Fields")) {
            addToImportList("import org.apache.commons.beanutils.DynaBean;");
            return "DBFields";
        }
        else if (dataType.equalsIgnoreCase("Fields")) {
            addToImportList("import org.apache.commons.beanutils.DynaBean;");
            return "DBFields";
        }
        else if (dataType.equalsIgnoreCase("ADODB.Field")) {
            return "DBField";
        }
        else if (dataType.equalsIgnoreCase("Field")) {
            return "DBField";
        }
        else {
            return dataType;
        }
    }
    private void createADODBClasses() {
        // Connection
        m_classObject.setPackageName("ADODB");
        m_classObject.setVbName("Connection");
        m_classObject.setJavaName("DBConnection");
        m_classObject.getClassIdFromClassName();
        m_classObject.saveClass();
        
        // RecordSet
        m_classObject.setPackageName("ADODB");
        m_classObject.setVbName("RecordSet");
        m_classObject.setJavaName("DBRecordSet");
        m_classObject.getClassIdFromClassName();
        m_classObject.saveClass();

        saveFunction("BOF", "isBOF", "boolean");
        saveFunction("EOF", "isEOF", "boolean");

        // Fields
        m_classObject.setPackageName("ADODB");
        m_classObject.setVbName("Fields");
        m_classObject.setJavaName("DBFields");
        m_classObject.getClassIdFromClassName();
        m_classObject.saveClass();

        // Field
        m_classObject.setPackageName("ADODB");
        m_classObject.setVbName("Field");
        m_classObject.setJavaName("DBField");
        m_classObject.getClassIdFromClassName();
        m_classObject.saveClass();
        
        m_classObject.setId(0);
    }
    
    // Visual Basic Standar Objects
    
    private void createVBClasses() {
        // Collection
        m_classObject.setPackageName("VBA");
        m_classObject.setVbName("Collection");
        //m_classObject.setJavaName("ArrayList");
        m_classObject.setJavaName("LinkedMap");
        m_classObject.getClassIdFromClassName();
        m_classObject.saveClass();

        saveFunction("count", "size", "int");
        saveFunction("item", "get", getObjectTypeName());
        saveFunction("remove", "remove", "void");

        m_classObject.setPackageName("VBA");
        m_classObject.setVbName("VBA");
        m_classObject.setJavaName("VBA");
        m_classObject.getClassIdFromClassName();
        m_classObject.saveClass();

        m_functionObject.setId(0);
        saveVariable("Err", "ex", getObjectTypeName(), false, true);
        
        m_classObject.setId(0);
    }
    private boolean isVBStandarObject(String dataType) {
        if (dataType.equalsIgnoreCase("Collection")) {
            return true;
        }
        else {
            return false;
        }
    }
    private String translateVBStandarObject(String dataType) {
        if (dataType.equalsIgnoreCase("Collection")) {
            //addToImportList("import java.util.ArrayList;");
            addToImportList("import org.apache.commons.collections.map.LinkedMap;");
            return "LinkedMap";
        }
        else {
            return dataType;
        }
    }
    private String getObjectTypeName() {
        if (m_translateToJava)
            return "Object";
        else
            return "object";
    }
}

class IdentifierInfo {
    boolean isFunction = false;
    Function function = null;
    Variable variable = null;
}

/*
 And
 As
 Call
 Do
 Exit
 False
 True
 For
 Function
 GoTo
 If
 Loop
 Me
 Next
 Not
 Nothing
 Option
 Or
 Private
 Public
 Resume
 Step
 Sub
 Then
 Until
 While
 If..Else..ElseIf..Then
 */

/*
 * TODO_DONE: file mError.bas line 72 {s = Replace(s, "$" & i + 1, X(i))}
 *       the code is translated as
 *              {s = Replace(s, "$" + ((Integer) i).toString() + 1, X(i));}
 *       it is wrong because i + 1 must to be evaluated first and then has to apply
 *       the cast to Integer:
 *              {s = Replace(s, "$" + ((Integer) (i + 1)).toString(), X(i));}
 */

/*
 *
 * TODO_DONE: manage events
 * TODO: manage byref params that actually aren't byref because are not asigned to a value
 *       by the function code
 * TODO_DONE: change getters in assignment eg:
 *              m_obj.getProperty() = ...;
 *       must be
 *              m_obj.setProperty(...);
 * TODO: translate byref for strings
 * TODO: translate byref for arrays. this is for array type params that are resized
 *       by the the function code. we have to search for redim
 * TODO_DONE: translate redim
 * TODO_DONE: translate instr
 * TODO: translate database access. replace recordsets.
 * TODO: translate globals (be aware of multi threading)
 * TODO: file functions (print, open, getattr, etc.)
 * TODO_DONE: translate Not sentence eg return Not cancel (this is parcially translated functionName = Not Cancel)
 * TODO: translate default property
 * TODO: translate on error goto controlerror
 * TODO: add import calls for references to vb projects we have translated
 * TODO_DONE: initialize local variables to zero or null string or null date or false
 * TODO: translate replace function
 * TODO_DONE: replace literal dates which are sourronded by #
 * TODO: resolve params array
 * TODO: translate IsEmpty for variants
 *
 * TODO: make an html report with a sumary of the work done (total classes translated,
 *       total files created, total projects translated, total functions)
 *
 *       erros: list of windows api calls founded and the line number where it appears in files
 *              list o variables of with block which can not be resolved and the line number
 *               where it appears in files
 *              list of references which are not vb projects we have translated yet eg: stdole2.dll
 *
 *       warnnings: list all the cases where default properties were translated
 *                  list all the cases where array indexes where translated
 *                  list all the cases where byref strings and byref numbers where translated
 *                  list all the cases where byref objects where translated to byval because
 *                   the object is not assigned by the code in the function and neither by the code
 *                   in other functions called by the function which was translated and takes
 *                   the object as a byref parameter
 *                  list all the cases where we found #If #else and #end if
 *
 */
