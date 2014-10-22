package com.horowitz.mickey;

import java.awt.BorderLayout;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.horowitz.mickey.data.ContractAnalysis;
import com.horowitz.mickey.data.Need;

public class ContractTablePanel extends JPanel {

  private TableModel _model;

  public ContractTablePanel(TableModel model) {
    super();

    setLayout(new BorderLayout());
    _model = model;
    JTable table = new JTable(_model);
    add(new JScrollPane(table));
  }

  public void setMap(Map<String, Map<String, Need>> map) {
    _model._map = map;
    repaint();
  }
  
  public void reload() {
    ContractAnalysis ca = new ContractAnalysis();
    Map<String, Map<String, Need>> map = ca.collectCurrentNeeds();
    _model._map = map;
    repaint();
  }

}
