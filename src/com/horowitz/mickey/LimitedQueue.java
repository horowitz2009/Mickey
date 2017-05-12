package com.horowitz.mickey;

import java.util.LinkedList;
import java.util.Queue;

public class LimitedQueue<E> extends LinkedList<E> implements Queue<E> {
  private int limit;

  @Override
  public boolean add(E e) {
    if (size() >= limit)
      removeFirst();
    return super.add(e);
  }
  

  public LimitedQueue(int limit) {
    super();
    this.limit = limit;
  }


  /**
   * @param args
   */
  public static void main(String[] args) {
    LimitedQueue<Integer> passQueue = new LimitedQueue<Integer>(5);

    for (int i = 1; i <= 10; i++) {
      passQueue.add(i);
    }
    for (Integer l : passQueue) {
      System.out.println(l);
    }

  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }
}
