package io.rcrm.api.pojo.nyma;

public class SequenceSettingPage {

	int thread_emails_as_replies;
	int execute_step_on_business_days;

	public SequenceSettingPage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SequenceSettingPage(int thread_emails_as_replies,int execute_step_on_business_days) {
		super();
		this.thread_emails_as_replies = thread_emails_as_replies;
		this.execute_step_on_business_days = execute_step_on_business_days;
	}

	public int getThread_emails_as_replies() {
		return thread_emails_as_replies;
	}

	public void setThread_emails_as_replies(int thread_emails_as_replies) {
		this.thread_emails_as_replies = thread_emails_as_replies;
	}

	public int getExecute_step_on_business_days() {
		return execute_step_on_business_days;
	}

	public void setExecute_step_on_business_days(int execute_step_on_business_days) {
		this.execute_step_on_business_days = execute_step_on_business_days;
	}
	
	

}
