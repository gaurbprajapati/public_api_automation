package io.rcrm.api.pojo.albatross.notification;

import java.util.ArrayList;

public class QueueNotification {

	private String action_name;
	private String sender;
	private ArrayList<Integer> receiver;
	private String source;
	private String destination;
	private String template_id;
	private ArrayList<String> cta_id;
	private Context context;

	public QueueNotification() {
	}

	public QueueNotification(String action_name, String sender, ArrayList<Integer> receiver, String source,
			String destination, String template_id, ArrayList<String> cta_id, Context context) {
		this.action_name = action_name;
		this.sender = sender;
		this.receiver = receiver;
		this.source = source;
		this.destination = destination;
		this.template_id = template_id;
		this.cta_id = cta_id;
		this.context = context;
	}

	public String getAction_name() {
		return action_name;
	}

	public void setAction_name(String action_name) {
		this.action_name = action_name;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public ArrayList<Integer> getReceiver() {
		return receiver;
	}

	public void setReceiver(ArrayList<Integer> receiver) {
		this.receiver = receiver;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getTemplate_id() {
		return template_id;
	}

	public void setTemplate_id(String template_id) {
		this.template_id = template_id;
	}

	public ArrayList<String> getCta_id() {
		return cta_id;
	}

	public void setCta_id(ArrayList<String> cta_id) {
		this.cta_id = cta_id;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
}
