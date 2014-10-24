package com.horowitz.mickey;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
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

  private TableModel               _model;
  private CTable                   _table;
  private DefaultTableCellRenderer _cellRenderer;
  private JScrollPane              _scrollPane;

  public ContractTablePanel(Map<String, Map<String, Need>> map) {
    super();

    setLayout(new BorderLayout());
    _model = new TableModel(map);

    _cellRenderer = new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        String t = (String) value;
        if (t != null && t.indexOf(":") > 0) {
          String[] ss = t.split(":");
          //NumberFormat nf = NumberFormat.getIntegerInstance();
          NumberFormat nf = NumberFormat.getNumberInstance();
          nf.setMaximumFractionDigits(0);
          Long v1 = Long.parseLong(ss[0]);
          Long v2 = Long.parseLong(ss[1]);
          Double vv1 = v1 / 1000.0;
          Double vv2 = v2 / 1000.0;

          String suffix1 = "";
          String suffix2 = "";
          if (vv1 < 8) {
            nf.setMaximumFractionDigits(3);
          } else {
            nf.setMaximumFractionDigits(0);
          }
          
//          if (vv1 > 2000) {
//            vv1 = vv1 / 1000;
//            nf.setMaximumFractionDigits(0);
//            suffix1 = "M";
//          }
          String red = nf.format(vv1) + suffix1;
          if (vv2 < 8) {
            nf.setMaximumFractionDigits(3);
          } else {
            nf.setMaximumFractionDigits(0);
          }
//          if (vv2 > 2000) {
//            vv2 = vv2 / 1000;
//            nf.setMaximumFractionDigits(0);
//            suffix2 = "M";
//          }
          String gray = nf.format(vv2) + suffix2;
          
          
          if (red.startsWith("0"))
            red = "";
          if (gray.startsWith("0"))
            gray = "";
          if (red.length() > 0) {
            label.setForeground(Color.RED);
            if (gray.length() > 0)
              red = red + "  (" + gray + ")";
            label.setText(red);
            label.setFont(label.getFont().deriveFont(12f));
          } else {
            label.setText(gray);
            label.setFont(label.getFont().deriveFont(12f));
            label.setFont(label.getFont().deriveFont(9f));
            label.setForeground(Color.GRAY);
          }
        }
        /*
         * 
         * if (t != null && t.startsWith("-")) { t = t.substring(1); label.setText(t); label.setFont(label.getFont().deriveFont(9f));
         * label.setForeground(Color.GRAY); } else { label.setForeground(Color.RED); if (t != null && t.equals("0")) { t = ""; label.setText(t); } }
         */
        label.setHorizontalAlignment(JLabel.RIGHT);
        label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 6));

        return label;

      }
    };

    _table = new CTable(_model, _cellRenderer);

    _scrollPane = new JScrollPane(_table);
    add(_scrollPane);

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

    _table = new CTable(_model, _cellRenderer);
    _scrollPane.getViewport().setView(_table);
    repaint();
  }

  static class CTable extends JTable {

    public CTable(javax.swing.table.TableModel model, DefaultTableCellRenderer cellRenderer) {
      super(model);
      for (int i = 1; i < model.getColumnCount(); ++i) {
        getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
      }
    }

  }

  public void reload() {
    ContractAnalysis ca = new ContractAnalysis();
    ca.calcALLNeeds();
    Map<String, Map<String, Need>> map = ca.collectCurrentNeedsALL();
    setMap(map);
  }

}
