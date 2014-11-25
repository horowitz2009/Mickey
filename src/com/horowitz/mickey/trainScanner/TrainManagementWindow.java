package com.horowitz.mickey.trainScanner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.horowitz.mickey.ImageManager;
import com.horowitz.mickey.JCanvas;
import com.horowitz.mickey.data.DataStore;

public class TrainManagementWindow extends JFrame {
  private List<Train> _trains;
  TrainScanner        _tscanner;

  public TrainManagementWindow(List<Train> trains, TrainScanner tscanner) {
    super();

    setDefaultCloseOperation(HIDE_ON_CLOSE);
    setTitle("Int. Train Manager");
    init();
    setSize(700, 550);
    setLocationRelativeTo(null);

    _trains = trains;
    _tscanner = tscanner;
  }

  private List<TrainView> _trainViews;

  private void init() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());

    initToolbar(mainPanel);

    Box box = Box.createVerticalBox();

    mainPanel.add(new JScrollPane(box));

    _trainViews = new ArrayList<>();

    for (Train t : _trains) {
      TrainView tv = buildTrainView(t);
      box.add(tv._panel);
      box.add(Box.createVerticalStrut(5));

      _trainViews.add(tv);

    }
    getContentPane().add(mainPanel);

  }

  private void initToolbar(JPanel mainPanel) {
    JToolBar toolbar = new JToolBar();

    toolbar.setFloatable(false);

    {
      JButton button = new JButton(new AbstractAction("Send Now") {

        @Override
        public void actionPerformed(ActionEvent e) {
          sendTrains();
        }
      });

      toolbar.add(button);
    }
    {
      JButton button = new JButton(new AbstractAction("Schedule") {

        @Override
        public void actionPerformed(ActionEvent e) {
          schedule();
        }
      });

      toolbar.add(button);
    }

  }

  protected void sendTrains() {
    for (TrainView tv : _trainViews) {
      Train t = tv._train;
      List<String> contractorsToSend = t.getContractorsToSend();
      contractorsToSend.clear();
      
      for (JToggleButton b : tv._buttons) {
        if (b.isSelected()) {
          contractorsToSend.add(b.getName());
        }
      }
    }

    _tscanner.sendTrains(_trains);
  }

  protected void schedule() {

    // TODO Auto-generated method stub

  }

  class TrainView {
    Train                      _train;
    JPanel                     _panel;
    public List<JToggleButton> _buttons;

  }

  private TrainView buildTrainView(Train t) {
    final TrainView trainView = new TrainView();
    JPanel panel = new JPanel(new BorderLayout());
    trainView._panel = panel;
    trainView._train = t;

    // train image
    JCanvas trainCanvas = new JCanvas();
    trainCanvas.setImage(t.getFullImage());
    panel.add(trainCanvas, BorderLayout.NORTH);

    // additional info image
    JCanvas addInfoCanvas = new JCanvas();
    if (!t.isIdle()) {
      addInfoCanvas.setImage(t.getAdditionalInfoShort());
    } else {
      try {
        BufferedImage read = ImageIO.read(ImageManager.getImageURL("int/empty.png"));
        addInfoCanvas.setImage(read);
      } catch (IOException e) {
      }
    }
    panel.add(addInfoCanvas, BorderLayout.WEST);

    // contractor buttons
    Box box = Box.createHorizontalBox();
    trainView._buttons = new ArrayList<>();

    try {
      List<String> activeContractorNames = new DataStore().getActiveContractorNames();
      for (String cname : activeContractorNames) {
        JToggleButton cbutton = createContractorButton(cname);
        box.add(cbutton);
        trainView._buttons.add(cbutton);
      }
      selectSame(trainView);
    } catch (IOException e) {
      e.printStackTrace();
    }

    JPanel buttonsPanel = new JPanel(new BorderLayout());
    buttonsPanel.add(new JScrollPane(box), BorderLayout.NORTH);

    Box box2 = Box.createHorizontalBox();
    JButton selectSameButton = new JButton(new AbstractAction("Select same") {

      @Override
      public void actionPerformed(ActionEvent evt) {
        selectSame(trainView);
      }
    });
    box2.add(selectSameButton);

    JButton clearButton = new JButton(new AbstractAction("Clear") {

      @Override
      public void actionPerformed(ActionEvent evt) {
        clear(trainView);
      }
    });
    box2.add(Box.createHorizontalStrut(10));
    box2.add(clearButton);

    buttonsPanel.add(box2, BorderLayout.SOUTH);

    panel.add(buttonsPanel); // center

    return trainView;
  }

  private JToggleButton createContractorButton(String name) {
    JToggleButton button = new JToggleButton();
    button.setName(name);
    // button.setText(name);
    ImageIcon icon1 = ImageManager.getImage("int/" + name + "3.png");
    button.setIcon(icon1);

    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    // button.setPreferredSize(new Dimension(40+4, 54+4));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setBorder(BorderFactory.createLineBorder(new Color(224, 10, 2), 7));
    // button.setPreferredSize(new Dimension(80, 80));
    // button.setMinimumSize(new Dimension(70, 70));
    button.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        JToggleButton b = (JToggleButton) e.getSource();
        b.setBorderPainted(b.isSelected());
        b.revalidate();
      }
    });

    return button;
  }

  private void clear(TrainView tv) {
    for (JToggleButton button : tv._buttons) {
      button.setSelected(false);
    }
  }

  private void selectSame(TrainView tv) {
    clear(tv);
    if (!tv._train.isIdle()) {
      List<ContractorView> contractorViews = tv._train.getContractorViews();
      for (ContractorView contractorView : contractorViews) {
        String cname = contractorView.getName();
        for (JToggleButton button : tv._buttons) {
          if (cname.equals(button.getName())) {
            button.setSelected(true);
            break;
          }
        }
      }
    }
  }
}
