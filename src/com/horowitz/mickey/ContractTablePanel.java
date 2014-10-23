package com.horowitz.mickey;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableCellRenderer;

import com.horowitz.mickey.data.ContractAnalysis;
import com.horowitz.mickey.data.Need;

public class ContractTablePanel extends JPanel {

  private TableModel _model;

  public ContractTablePanel(TableModel model) {
    super();

    setLayout(new BorderLayout());
    _model = model;
    JTable table = new JTable(_model);

    // table.setCellEditor(anEditor);
    DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);


        String t = (String) value;
        if (t != null && t.startsWith("-")) {
          t = t.substring(1);
          label.setText(t);
          label.setFont(label.getFont().deriveFont(9f));
          label.setForeground(Color.GRAY);
        } else {
          label.setForeground(Color.RED);
          if (t != null && t.equals("0")) {
            t = "";
            label.setText(t);
          }
        }
        
        label.setHorizontalAlignment(JLabel.RIGHT);
        label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 6));
        
        return label;

      }
    };
    for (int i = 1; i < _model.getColumnCount(); ++i){
      table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
    }

    add(new JScrollPane(table));
    
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    
    toolbar.add(new AbstractAction("Recalc") {
      
      @Override
      public void actionPerformed(ActionEvent e) {
        Thread t = new Thread(new Runnable() {
          public void run() {
            reload();
          }
        });
        t.start();
      }
    });
    
    add(toolbar, BorderLayout.NORTH);
  }

  public void setMap(Map<String, Map<String, Need>> map) {
    _model._map = map;
    repaint();
  }

  public void reload() {
    ContractAnalysis ca = new ContractAnalysis();
    ca.calcALLNeeds();
    Map<String, Map<String, Need>> map = ca.collectCurrentNeedsALL();
    _model._map = map;
    repaint();
  }

}
