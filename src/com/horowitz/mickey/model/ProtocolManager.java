package com.horowitz.mickey.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ProtocolManager {

  public static final String   DEFAULT         = "D";

  private Gson                  _gson           = new GsonBuilder().setPrettyPrinting().create();

  private Protocol[]            protocols;

  private Protocol              currentProtocol = null;
  private long                  start;

  private boolean               running         = false;
  private boolean               autoJourney     = false;
  private PropertyChangeSupport support         = new PropertyChangeSupport(this);

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    support.addPropertyChangeListener(propertyName, listener);
  }

  public Protocol getCurrentProtocol() {
    return currentProtocol;
  }

  public void setCurrentProtocol(String name) {
    Protocol newProtocol = getProtocol(name);
    if (newProtocol == null)
      newProtocol = getProtocol(DEFAULT);
    if (!newProtocol.equals(this.currentProtocol)) {
      Protocol oldProtocol = this.currentProtocol;
      this.currentProtocol = newProtocol;
      System.err.println(this.currentProtocol);
      support.firePropertyChange("PROTOCOL_CHANGED", oldProtocol, newProtocol);
      support.firePropertyChange("TIME_ELAPSED", -1l, 0l);
      stopCounting();
      startCounting();
    }
  }

  private void stopCounting() {
    running = false;
    try {
      Thread.sleep(250);
    } catch (InterruptedException e) {
    }
  }

  private void startCounting() {
    if (currentProtocol != null) {
      start = System.currentTimeMillis();
      if (currentProtocol.getDuration() > 0) {
        running = true;
        Thread t = new Thread(new Runnable() {
          public void run() {
            while (running && (!autoJourney || System.currentTimeMillis() - start < currentProtocol.getDuration() * 60000)) {
              try {
                for (int i = 0; running && i < 25; i++)
                  Thread.sleep(200);
                support.firePropertyChange("TIME_ELAPSED", 0l, System.currentTimeMillis() - start);
              } catch (InterruptedException e) {
              }
            }
            // time is up
            if (running && autoJourney)
              setCurrentProtocol(currentProtocol.getNextProtocol());
          }

        }, "PROTOCOL_COUNTER");
        t.start();
      } else
        support.firePropertyChange("TIME_ELAPSED", -1l, 0l);
    }
  }

  public long timeSinceLastChange() {
    return System.currentTimeMillis() - start;
  }

  public Protocol[] readProtocols() throws IOException {
    String json = FileUtils.readFileToString(new File("data/protocols.json"));

    Protocol[] protocols = _gson.fromJson(json, Protocol[].class);

    return protocols;
  }

  public void loadProtocols() {
    try {
      protocols = readProtocols();
      // if (currentProtocol == null)
      // setCurrentProtocol(DEFAULT);
      // else {
      // //what if current protocol is changed
      // setCurrentProtocol(currentProtocol.getName());
      // }
    } catch (IOException e) {
      Protocol p = buildDefaultProtocol();
      protocols = new Protocol[1];
      protocols[0] = p;
      saveProtocols();
      // setCurrentProtocol(DEFAULT);
    }
  }

  public Protocol buildDefaultProtocol() {
    Protocol p = new Protocol(DEFAULT);
    p.setDestination(10);
    p.setDuration(-1);
    p.setInternational(true);
    p.setMaglev15(true);
    p.setResend(true);
    p.setNextProtocol(null);
    return p;
  }

  public void saveProtocols() {

    try {
      String json = _gson.toJson(protocols);
      FileUtils.writeStringToFile(new File("data/protocols.json"), json);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public Protocol getProtocol(String name) {
    for (Protocol protocol : protocols) {
      if (protocol.getName().equals(name))
        return protocol;
    }
    return null;
  }

  public static void main(String[] args) {
    ProtocolManager protocolManager = new ProtocolManager();
    protocolManager.loadProtocols();
    Protocol p = protocolManager.getProtocol(DEFAULT);
    System.err.println(p);
    System.err.println("done");
  }

  public Protocol[] getProtocols() {
    return protocols;
  }

  public long getStart() {
    return start;
  }

  public void setStart(long start) {
    this.start = start;
  }

  public boolean isAutoJourney() {
    return autoJourney;
  }

  public void setAutoJourney(boolean autoJourney) {
    this.autoJourney = autoJourney;
  }

}
