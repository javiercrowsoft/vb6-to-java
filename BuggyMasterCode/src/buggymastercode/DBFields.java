/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jalvarez
 */
public class DBFields {

    private List<DBField> m_items = new ArrayList();
    private Map<String, Integer> m_keys = new HashMap();

    public List<DBField> getItems() {
        return m_items;
    }
    public DBField getItem(String name) {
        return m_items.get(m_keys.get(name.toLowerCase()));
    }
    public DBField getItem(int index) {
        return m_items.get(index);
    }

    protected Map<String, Integer> getKeys() {
        return m_keys;
    }


}
