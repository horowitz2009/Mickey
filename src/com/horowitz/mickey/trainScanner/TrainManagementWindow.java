package com.horowitz.mickey.trainScanner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.horowitz.mickey.DateUtils;
import com.horowitz.mickey.ImageManager;
import com.horowitz.mickey.JCanvas;
import com.horowitz.mickey.common.Scheduler;
import com.horowitz.mickey.data.DataStore;

public class TrainManagementWindow extends JFrame {
  class TrainView {
    public List<JToggleButton> _buttons;
    JPanel                     _panel;
    Train                      _train;

  }

  public String           _fancyTime;

  private JScrollPane     _jScrollPane;

  private Thread          _scheduleThread;
  private JLabel          _timeLabel;
  private long            _timeLeft;
  private JTextField      _timeTF;

  private List<Train>     _trains;

  private List<TrainView> _trainViews;

  TrainScanner            _tscanner;

  public TrainManagementWindow(List<Train> trains, TrainScanner tscanner) {
    super();
    _trains = trains;
    _tscanner = tscanner;

    setDefaultCloseOperation(HIDE_ON_CLOSE);
    setTitle("Int. Train Manager");
    init();
    setSize(700, 550);
    setLocationRelativeTo(null);
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
        box.add(Box.createHorizontalStrut(2));
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

  private void clear(TrainView tv) {
    for (JToggleButton button : tv._buttons) {
      button.setSelected(false);
    }
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

  private void init() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());

    initToolbar(mainPanel);

    Box box = Box.createVerticalBox();

    _jScrollPane = new JScrollPane(box);
    mainPanel.add(_jScrollPane);

    getContentPane().add(mainPanel);

    // updateView();
    reload();
  }

  private void initToolbar(JPanel mainPanel) {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    {
      JButton button = new JButton(new AbstractAction("Reload") {

        @Override
        public void actionPerformed(ActionEvent e) {
          reload();
        }
      });

      toolbar.add(button);
    }
    {
      JButton button = new JButton(new AbstractAction("Rescan") {

        @Override
        public void actionPerformed(ActionEvent e) {
          scan();
        }
      });

      toolbar.add(button);
    }
    {
      JButton button = new JButton(new AbstractAction("Save") {

        @Override
        public void actionPerformed(ActionEvent e) {
          save();
        }
      });

      toolbar.add(button);
    }
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

    _timeTF = new JTextField(8);
    _timeTF.setMaximumSize(new Dimension(50, 20));
    _timeTF.setMinimumSize(new Dimension(50, 20));
    _timeLabel = new JLabel("   no schedule at the moment");

    toolbar.add(_timeTF);
    toolbar.add(_timeLabel);

    mainPanel.add(toolbar, BorderLayout.NORTH);
  }

  private boolean isRunning(String threadName) {
    boolean isRunning = false;
    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
    for (Iterator<Thread> it = threadSet.iterator(); it.hasNext();) {
      Thread thread = it.next();
      if (thread.getName().equals(threadName)) {
        isRunning = true;
        break;
      }
    }
    return isRunning;
  }

  protected void reload() {
    Thread t = new Thread(new Runnable() {
      public void run() {
        // TrainManagementWindow.this.setVisible(false);
        _trains = new ArrayList<>();
        try {
          Train[] trains = new DataStore().readTrains();
          if (trains != null)
            for (Train train : trains) {
              if (train.getFullImage() == null) {
                BufferedImage image = ImageIO.read(new File(train.getFullImageFileName()));
                train.setFullImage(image);
                String additionalInfoShortFileName = train.getAdditionalInfoShortFileName();
                if (additionalInfoShortFileName != null) {
                  image = ImageIO.read(new File(additionalInfoShortFileName));
                  train.setAdditionalInfoShort(image);
                }
                image = ImageIO.read(new File(train.getScanImageFileName()));
                train.setScanImage(image);
              }
              _trains.add(train);
            }
        } catch (IOException e) {
          e.printStackTrace();
        }
        updateView();
        // TrainManagementWindow.this.setVisible(true);
      }
    });
    t.start();
  }

  private void save() {
    if (_trains != null)
      try {
        updateTrainStatus(true);
        new DataStore().writeTrains(_trains.toArray(new Train[0]));
      } catch (IOException e) {
        e.printStackTrace();
      }
  }

  protected void scan() {
    Thread t = new Thread(new Runnable() {
      public void run() {
        TrainManagementWindow.this.setVisible(false);
        File d = new File("data/int");
        String[] list = d.list(new FilenameFilter() {

          @Override
          public boolean accept(File f, String name) {
            return name.endsWith(".bmp");
          }
        });
        for (String filename : list) {
          File f = new File(d, filename);
          f.delete();
        }

        _trains = _tscanner.analyzeIntTrains();
        updateView();
        save();
        TrainManagementWindow.this.setVisible(true);
      }
    });
    t.start();
  }

  protected void schedule() {
    if (_scheduleThread != null) {
      _scheduleThread.interrupt();
    }
    final long time = Scheduler.parse(_timeTF.getText()) + System.currentTimeMillis();
    _tscanner.LOGGER.info("Scheduling sending for " + _timeTF.getText());
    runScheduleThread(time);
  }

  private void runScheduleThread(final long time) {
    _scheduleThread = new Thread(new Runnable() {

      public void run() {
        while (true) {
          try {
            Thread.sleep(200);
          } catch (InterruptedException e) {
            System.err.println("Scheduler interrupted");
            break;
          }
          _timeLeft = time - System.currentTimeMillis();
          if (_timeLeft <= 0) {
            // it's time
            _timeLabel.setText("");
            if (!isRunning("MAGIC"))
              sendTrains();
            break;
          } else {
            _fancyTime = DateUtils.fancyTime2(_timeLeft) + " left to send int. trains...";
            _tscanner.LOGGER.info(_fancyTime);
            System.err.println(_fancyTime);
            _timeLabel.setText(_fancyTime);
          }
          try {
            Thread.sleep(19800);
          } catch (InterruptedException e) {
            System.err.println("Scheduler interrupted!");
            break;
          }
        }
      }
    }, "SCHEDULER");
    _scheduleThread.start();
  }

  private void selectSame(TrainView tv) {
    clear(tv);
    List<String> contractorsBeenSent = tv._train.getContractorsBeenSent();
    if (contractorsBeenSent != null)
      for (String cname : contractorsBeenSent) {
        for (JToggleButton button : tv._buttons) {
          if (cname.equals(button.getName())) {
            button.setSelected(true);
            break;
          }
        }
      }
  }

  private void updateTrainStatus(boolean save) {
    for (TrainView tv : _trainViews) {
      Train t = tv._train;
      List<String> contractorsToSend = t.getContractorsToSend();
      contractorsToSend.clear();

      for (JToggleButton b : tv._buttons) {
        if (b.isSelected()) {
          contractorsToSend.add(b.getName());
        }
      }
      if (save) {
        tv._train.setContractorsBeenSent(new ArrayList<>(contractorsToSend));
      }
    }
  }

  public void sendTrains() {
    if (!isRunning("SENDING")) {
      Thread t = new Thread(new Runnable() {
        public void run() {
          sendTrainsNow();
        }
      }, "SENDING");
      t.start();
    }
  }

  public void sendTrainsNow() {
    updateTrainStatus(true);
    _tscanner.sendTrains(_trains);
  }

  private void updateView() {
    Box box = Box.createVerticalBox();
    _trainViews = new ArrayList<>();
    if (_trains != null) {
      for (Train t : _trains) {
        TrainView tv = buildTrainView(t);
        box.add(tv._panel);
        box.add(Box.createVerticalStrut(5));
        _trainViews.add(tv);
      }
    }

    _jScrollPane.getViewport().setView(box);
  }

  public long getTimeLeft() {
    return _timeLeft;
  }

  public void setTimeLeft(long timeLeft) {
    _timeLeft = timeLeft;
  }

  public void reschedule(long time) {
    time += System.currentTimeMillis();
    setTimeLeft(time);
    runScheduleThread(time);
  }
}