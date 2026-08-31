package io.rcrm.api.pojo.chromeExtension;

import java.util.List;

public class TaskPojo {
	private Task task;
	private List<Integer> collaborator_user_ids;
	private List<Integer> collaborator_team_ids;
	private List<Integer> collaborator;
	private String extension_version = "3.1.51";
	private Object extraData = null;

	public static class Task {
		private String title;
		private int ownerid;
		private String relatedto;
		private String reminderdate;
		private long startdate;
		private int accountid;
		private String address;
		private int allday;
		private String creatorname;
		private Object emailbatchid;
		private int eventid;
		private String status;
		private String description;
		private int type;
		private String relatedtoname;
		private String relatedtotypeid;

		public Task() {
		}

		public Task(String title, int ownerid, String relatedto, String reminderdate, long startdate, int accountid,
				String description, String relatedtotypeid) {
			this.title = title;
			this.ownerid = ownerid;
			this.relatedto = relatedto;
			this.reminderdate = reminderdate;
			this.startdate = startdate;
			this.accountid = accountid;
			this.description = description;
			this.relatedtotypeid = relatedtotypeid;
		}

		// Getters and Setters
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public int getOwnerid() {
			return ownerid;
		}

		public void setOwnerid(int ownerid) {
			this.ownerid = ownerid;
		}

		public String getRelatedto() {
			return relatedto;
		}

		public void setRelatedto(String relatedto) {
			this.relatedto = relatedto;
		}

		public String getReminderdate() {
			return reminderdate;
		}

		public void setReminderdate(String reminderdate) {
			this.reminderdate = reminderdate;
		}

		public long getStartdate() {
			return startdate;
		}

		public void setStartdate(long startdate) {
			this.startdate = startdate;
		}

		public int getAccountid() {
			return accountid;
		}

		public void setAccountid(int accountid) {
			this.accountid = accountid;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public int getAllday() {
			return allday;
		}

		public void setAllday(int allday) {
			this.allday = allday;
		}

		public String getCreatorname() {
			return creatorname;
		}

		public void setCreatorname(String creatorname) {
			this.creatorname = creatorname;
		}

		public Object getEmailbatchid() {
			return emailbatchid;
		}

		public void setEmailbatchid(Object emailbatchid) {
			this.emailbatchid = emailbatchid;
		}

		public int getEventid() {
			return eventid;
		}

		public void setEventid(int eventid) {
			this.eventid = eventid;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getRelatedtoname() {
			return relatedtoname;
		}

		public void setRelatedtoname(String relatedtoname) {
			this.relatedtoname = relatedtoname;
		}

		public String getRelatedtotypeid() {
			return relatedtotypeid;
		}

		public void setRelatedtotypeid(String relatedtotypeid) {
			this.relatedtotypeid = relatedtotypeid;
		}
	}

	// Getters and Setters for TaskData
	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Object getExtraData() {
		return extraData;
	}

	public void setExtraData(Object extraData) {
		this.extraData = extraData;
	}

	public List<Integer> getCollaborator_user_ids() {
		return collaborator_user_ids;
	}

	public void setCollaborator_user_ids(List<Integer> collaborator_user_ids) {
		this.collaborator_user_ids = collaborator_user_ids;
	}

	public List<Integer> getCollaborator_team_ids() {
		return collaborator_team_ids;
	}

	public void setCollaborator_team_ids(List<Integer> collaborator_team_ids) {
		this.collaborator_team_ids = collaborator_team_ids;
	}

	public List<Integer> getCollaborator() {
		return collaborator;
	}

	public void setCollaborator(List<Integer> collaborator) {
		this.collaborator = collaborator;
	}

	public String getExtension_version() {
		return extension_version;
	}

	public void setExtension_version(String extension_version) {
		this.extension_version = extension_version;
	}
}