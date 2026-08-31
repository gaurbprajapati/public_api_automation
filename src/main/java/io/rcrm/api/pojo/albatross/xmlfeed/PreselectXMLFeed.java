package io.rcrm.api.pojo.albatross.xmlfeed;

public class PreselectXMLFeed {
  private int jobboard_id;
  private int jobboard_type;
  private int account_id;
  private int status;
  private String jobboard_name;

  public PreselectXMLFeed() {
  }

  public PreselectXMLFeed(int jobboard_id, int jobboard_type, int account_id, int status, String jobboard_name) {
    this.jobboard_id = jobboard_id;
    this.jobboard_type = jobboard_type;
    this.account_id = account_id;
    this.status = status;
    this.jobboard_name = jobboard_name;
  }

  public int getJobboard_id() {
    return jobboard_id;
  }

  public void setJobboard_id(int jobboard_id) {
    this.jobboard_id = jobboard_id;
  }

  public int getJobboard_type() {
    return jobboard_type;
  }

  public void setJobboard_type(int jobboard_type) {
    this.jobboard_type = jobboard_type;
  }

  public int getAccount_id() {
    return account_id;
  }

  public void setAccount_id(int account_id) {
    this.account_id = account_id;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getJobboard_name() {
    return jobboard_name;
  }

  public void setJobboard_name(String jobboard_name) {
    this.jobboard_name = jobboard_name;
  }
}
