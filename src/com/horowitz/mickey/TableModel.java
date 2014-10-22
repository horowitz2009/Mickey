package com.horowitz.mickey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.horowitz.mickey.data.Contractor;
import com.horowitz.mickey.data.DataStore;
import com.horowitz.mickey.data.Material;
import com.horowitz.mickey.data.Need;
import com.horowitz.mickey.data.Objective;

class TableModel extends AbstractTableModel {

  Map<String, Map<String, Need>> _map;

  private Contractor[]                   _contractors;

  private Material[]                     _allmats;

  private String[]                       _other;
  private String[]                       _rows;

  public TableModel(Map<String, Map<String, Need>> map) {
    super();
    _map = map;
    _allmats = Material.createArray();
    _other = Material.OTHER;
    if (_map != null) {
      List<String> rows = new ArrayList<>();
      for (int i = 0; i < _other.length; i++) {
        if (_map.containsKey(_other[i])) {
          rows.add(_other[i]);
        }
      }
      for (int i = 0; i < _allmats.length; i++) {
        if (_map.containsKey(_allmats[i].getName())) {
          rows.add(_allmats[i].getName());
        }
      }

      _rows = rows.toArray(new String[0]);
    }
    
    try {
      _contractors = new DataStore().readContractors();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public int getColumnCount() {
    if (_contractors == null)
      return 1;
    return _contractors.length + 1;
  }

  @Override
  public int getRowCount() {
    if (_rows == null)
      return 0;
    return _rows.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Object value = null;
    if (_rows == null || _contractors == null || _map == null)
      return value;
    
    String mat = _rows[rowIndex];
    if (columnIndex == 0) {
      value = mat;
    } else {
      Contractor contractor = _contractors[columnIndex - 1];
      Map<String, Need> innerMap = _map.get(mat);

      Need need = innerMap.get(contractor.getName());
      if (need != null) {
        Objective o = need.getObjective();
        value = (o.getNeededAmount() - o.getCurrentAmount());
      }
    }
    return value;
  }

  @Override
  public String getColumnName(int column) {
    if (column == 0) {
      return "What";
    }
    Contractor contractor = _contractors[column - 1];
    return contractor.getName();
  }

}