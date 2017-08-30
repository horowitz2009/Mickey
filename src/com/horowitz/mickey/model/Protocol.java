package com.horowitz.mickey.model;

public class Protocol {

  private String  name;
  private String  nextProtocol;
  private boolean resend;
  private boolean maglev15;
  private boolean international;
  private int     destination;
  private int     duration;
  private boolean whistles;
  private boolean packages;
  private boolean ping;
  private boolean refresh;

  
  
  public Protocol() {
    super();
  }
  
  public Protocol(String name) {
    super();
    this.name = name;
  }



  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNextProtocol() {
    return nextProtocol;
  }

  public void setNextProtocol(String nextProtocol) {
    this.nextProtocol = nextProtocol;
  }

  public boolean isResend() {
    return resend;
  }

  public void setResend(boolean resend) {
    this.resend = resend;
  }

  public boolean isMaglev15() {
    return maglev15;
  }

  public void setMaglev15(boolean maglev15) {
    this.maglev15 = maglev15;
  }

  public boolean isInternational() {
    return international;
  }

  public void setInternational(boolean international) {
    this.international = international;
  }

  public int getDestination() {
    return destination;
  }

  public void setDestination(int destination) {
    this.destination = destination;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public boolean isWhistles() {
    return whistles;
  }

  public void setWhistles(boolean whistles) {
    this.whistles = whistles;
  }

  public boolean isPackages() {
    return packages;
  }

  public void setPackages(boolean packages) {
    this.packages = packages;
  }

  public boolean isPing() {
    return ping;
  }

  public void setPing(boolean ping) {
    this.ping = ping;
  }

  public boolean isRefresh() {
    return refresh;
  }

  public void setRefresh(boolean refresh) {
    this.refresh = refresh;
  }

  @Override
  public String toString() {
    return "Protocol [name=" + name + ", nextProtocol=" + nextProtocol + ", resend=" + resend + ", maglev15=" + maglev15 + ", international="
        + international + ", destination=" + destination + ", duration=" + duration + ", whistles=" + whistles + ", packages=" + packages + ", ping="
        + ping + ", refresh=" + refresh + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + destination;
    result = prime * result + duration;
    result = prime * result + (international ? 1231 : 1237);
    result = prime * result + (maglev15 ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((nextProtocol == null) ? 0 : nextProtocol.hashCode());
    result = prime * result + (packages ? 1231 : 1237);
    result = prime * result + (ping ? 1231 : 1237);
    result = prime * result + (refresh ? 1231 : 1237);
    result = prime * result + (resend ? 1231 : 1237);
    result = prime * result + (whistles ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Protocol other = (Protocol) obj;
    if (destination != other.destination)
      return false;
    if (duration != other.duration)
      return false;
    if (international != other.international)
      return false;
    if (maglev15 != other.maglev15)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (nextProtocol == null) {
      if (other.nextProtocol != null)
        return false;
    } else if (!nextProtocol.equals(other.nextProtocol))
      return false;
    if (packages != other.packages)
      return false;
    if (ping != other.ping)
      return false;
    if (refresh != other.refresh)
      return false;
    if (resend != other.resend)
      return false;
    if (whistles != other.whistles)
      return false;
    return true;
  }
  
  

}
