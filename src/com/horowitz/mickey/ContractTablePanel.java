package com.horowitz.mickey;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import sun.swing.table.DefaultTableCellHeaderRenderer;

import com.horowitz.mickey.data.IContractAnalysys;
import com.horowitz.mickey.data.Need;

public class ContractTablePanel extends JPanel {

  private TableModel               _model;
  private CTable                   _table;
  private DefaultTableCellRenderer _cellRenderer;
  private DefaultTableCellRenderer _cellRendererOne;
  private JScrollPane              _scrollPane;
  private IContractAnalysys        _contractAnalysis;

  public ContractTablePanel(IContractAnalysys contractAnalysys) {
    super();

    setLayout(new BorderLayout());
    ToolTipManager.sharedInstance().setDismissDelay(30000);
    _contractAnalysis = contractAnalysys;
    _model = new TableModel(_contractAnalysis.collectNeeds());

    _cellRendererOne = new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String t = (String) value;

        ImageIcon image = ImageManager.getImage("contracts/" + t + "24.png");
        if (image == null) {
          image = ImageManager.getImage("contracts/Unknown24.png");
        }
        label.setIcon(image);
        return label;
      }
    };

    _cellRenderer = new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        label.setToolTipText("");
        String t = (String) value;
        if (t != null && t.indexOf(":") > 0) {
          String[] ss = t.split(":");
          // NumberFormat nf = NumberFormat.getIntegerInstance();
          NumberFormat nf = NumberFormat.getNumberInstance();
          nf.setMaximumFractionDigits(0);
          Long v1 = Long.parseLong(ss[0]);

          if (v1 > 0) {
            Double t1 = v1 / 1.10;
            Double t2 = t1 / 2;
            Double t3 = t1 / 3;
            Double t4 = t1 / 4;
            
            Double t2a = t1 / 1.15;
            Double t3a = t1 / 1.30;
            Double t4a = t1 / 1.45;
            
            
            NumberFormat nf2 = NumberFormat.getNumberInstance();
            nf2.setMaximumFractionDigits(0);
            String tt1 = nf2.format(t1);
            String tt2 = nf2.format(t2);
            String tt2a = nf2.format(t2a);
            String tt3 = nf2.format(t3);
            String tt3a = nf2.format(t3a);
            String tt4 = nf2.format(t4);
            String tt4a = nf2.format(t4a);
            label.setToolTipText("<html>" + tt1 + 
                "<br>" + tt2 + " (" + tt2a + ")" + 
            "<br>" + tt3 + " (" + tt3a + ")" + 
            "<br>" + tt4 + " (" + tt4a + ")" + 
                "</html>");

          }

          Long v2 = Long.parseLong(ss[1]);
          Double vv1 = v1 > 800 ? v1 / 1000.0 : v1;
          Double vv2 = v2 > 800 ? v2 / 1000.0 : v2;

          String suffix1 = "";
          String suffix2 = "";
          if (vv1 < 8) {
            nf.setMaximumFractionDigits(3);
          } else {
            nf.setMaximumFractionDigits(0);
          }

          // if (vv1 > 2000) {
          // vv1 = vv1 / 1000;
          // nf.setMaximumFractionDigits(0);
          // suffix1 = "M";
          // }
          String red = nf.format(vv1) + suffix1;

          if (vv2 < 8) {
            nf.setMaximumFractionDigits(3);
          } else {
            nf.setMaximumFractionDigits(0);
          }
          // if (vv2 > 2000) {
          // vv2 = vv2 / 1000;
          // nf.setMaximumFractionDigits(0);
          // suffix2 = "M";
          // }
          String gray = nf.format(vv2) + suffix2;

          if (red.startsWith("0"))
            red = "";
          if (gray.startsWith("0"))
            gray = "";
          if (red.length() > 0) {
            if (v1 <= 800) {
              vv1 = v1 * 1.0;
              label.setForeground(Color.ORANGE);
            } else {
              label.setForeground(Color.RED);
            }
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
    // _model._map = map;
    _model = new TableModel(map);

    _table = new CTable(_model, _cellRenderer);
    _table.setRowHeight(26);
    
    TableCellRenderer defaultRenderer = _table.getTableHeader().getDefaultRenderer();
    System.err.println(defaultRenderer);
    _table.getTableHeader().setDefaultRenderer(new DefaultTableCellHeaderRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String name = (String) value;
        if (name!= null && name.length() > 0) {
          //try to get icon
          String[] ss = name.split(" ");
          if (ss != null) {
            name = ss[0];
          }
          name = name +"40.png";
          ImageIcon image = ImageManager.getImage("contracts/" + name);
          label.setIcon(image);
          label.setHorizontalTextPosition(SwingConstants.CENTER);
          label.setVerticalTextPosition(SwingConstants.BOTTOM);
          label.setText("");
          label.setToolTipText((String)value);
        }
        return label;
      }
    });
    
    _scrollPane.getViewport().setView(_table);
    repaint();
  }

  class CTable extends JTable {

    public CTable(javax.swing.table.TableModel model, DefaultTableCellRenderer cellRenderer) {
      super(model);
      getColumnModel().getColumn(0).setCellRenderer(_cellRendererOne);
      for (int i = 1; i < model.getColumnCount(); ++i) {
        getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
      }
    }

  }

  public void reload() {

    _contractAnalysis.calcALLNeeds();
    Map<String, Map<String, Need>> map = _contractAnalysis.collectNeeds();
    setMap(map);
  }

}
