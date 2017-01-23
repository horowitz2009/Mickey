package com.horowitz.mickey.trainScanner;

public class Stats {

  private int  trains;
  private long coins;
  private long passengers;

  
  public Stats() {
    super();
    reset();
  }

  public void registerTrain(long coins, long passengers) {
    this.trains++;
    this.coins += coins;
    this.passengers += passengers;
  }

  public void reset() {
    trains = 0;
    coins = 0l;
    passengers = 0l;
  }

  public int getTrains() {
    return trains;
  }

  public void setTrains(int trains) {
    this.trains = trains;
  }

  public long getCoins() {
    return coins;
  }

  public void setCoins(long coins) {
    this.coins = coins;
  }

  public long getPassengers() {
    return passengers;
  }

  public void setPassengers(long passengers) {
    this.passengers = passengers;
  }

}
