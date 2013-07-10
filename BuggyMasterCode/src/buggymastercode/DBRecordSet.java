/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buggymastercode;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.beanutils.DynaBean;


/**
 *
 * @author jalvarez
 */
public class DBRecordSet {

        private boolean m_bof = true;
        private boolean m_eof = true;
        private int m_iRow = 0;
        private int m_rowCount = 0;
        private ResultSetMetaData m_rsmd;

	private List<DynaBean> m_rows;
        private DBFields m_fields = new DBFields();

        public boolean isBOF() {
            return m_bof;
        }

        public boolean isEOF() {
            return m_eof;
        }

        public DBFields getFields() {
            return m_fields;
        }

        public DBField getFields(String name) {
            return m_fields.getItems().get(m_fields.getKeys().get(name.toLowerCase()));
        }

        public DBField getFields(int index) {
            return m_fields.getItems().get(index);
        }

	public void setRows(List<DynaBean> rows) { 
            m_rows = rows;
            m_bof = true;
            if (m_rows != null) {
                m_iRow = 0;
                m_rowCount = m_rows.size();
            }
            else {
                m_iRow = 0;
                m_rowCount = 0;
            }
            if (m_rowCount > 0) {
                m_eof = false;
            }
            else {
                m_eof = true;
            }
            fillItems();
        }
	public List<DynaBean> getRows() { 
            return m_rows;
        }
        public void moveFirst() {
            m_iRow = 0;
            m_bof = true;
            if (m_rowCount > 0)
                m_eof = false;
            fillItems();
        }
        public void moveLast() { 
            m_iRow = m_rowCount -1; 
            if (m_iRow < 0)
                m_iRow = 0;
            if (m_rowCount > 0)
                m_eof = false;
            if (m_iRow == 0)
                m_bof = true;
            fillItems();
        }
        public void moveNext() { 
            m_iRow++; 
            if (m_iRow > m_rowCount -1) {
                m_iRow = m_rowCount;
                m_eof = true;
            }
            if (m_iRow > 0)
                m_bof = false;
            fillItems();
        }
        public void movePrevious() {
            m_iRow--;
            if (m_iRow < 0)
                m_iRow = 0;
            if (m_iRow == 0)
                m_bof = true;
            if (m_iRow < m_rowCount && m_rowCount > 0)
                m_eof = false;
            fillItems();
        }
        public void fillItems() {
            List<DBField> fields = m_fields.getItems();
            if (m_rowCount > 0 && !m_eof) {
                DynaBean row = m_rows.get(m_iRow);
                for (Iterator<DBField> i = fields.iterator(); i.hasNext();) {
                    DBField field = i.next();
                    field.setValue(row.get(field.getName().toLowerCase()));
                }
            }
            else {
                for (Iterator<DBField> i = fields.iterator(); i.hasNext();) {
                    DBField field = i.next();
                    field.setValue(null);
                }
            }
        }
        public int rowCount() {
            return m_rowCount;
        }

        protected void setMetaData(ResultSetMetaData rsmd) {
            m_rsmd = rsmd;
            try {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    m_fields.getItems().add(new DBField(rsmd.getColumnName(i), 
                                            rsmd.getColumnType(i),
                                            null));
                    m_fields.getKeys().put(rsmd.getColumnName(i).toLowerCase(), i-1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(DBRecordSet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
}
