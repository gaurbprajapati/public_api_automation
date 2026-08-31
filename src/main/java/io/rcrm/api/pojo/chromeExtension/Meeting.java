package io.rcrm.api.pojo.chromeExtension;

import java.util.List;

public class Meeting {
	private Appointment appointment;
	private List<Integer> collaborator_user_ids;
	private List<Integer> collaborator_team_ids;
	private List<Integer> collaborator;
	private boolean task;
	private String extension_version = "3.1.51";
	private Object extraData = null;

	public Appointment getAppointment() {
		return appointment;
	}

	public void setAppointment(Appointment appointment) {
		this.appointment = appointment;
	}

	public boolean isTask() {
		return task;
	}

	public void setTask(boolean task) {
		this.task = task;
	}

	public String getextension_version() {
		return extension_version;
	}

	public void setextension_version(String extension_version) {
		this.extension_version = extension_version;
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

	public static class Appointment {
		private String title;
		private int ownerid;
		private String address;
		private String relatedto;
		private long startdate;
		private String reminderdate;
		private long enddate;
		private int accountid;
		private String creatorname;
		private Object emailbatchid;
		private int status;
		private String description;
		private String relatedtotypeid;

		public Appointment() {
		}

		public Appointment(String title, String relatedto, long startdate, String reminderdate, long enddate,
				int accountid, String description, String relatedtotypeid, int ownerid) {
			super();
			this.title = title;
			this.relatedto = relatedto;
			this.startdate = startdate;
			this.reminderdate = reminderdate;
			this.enddate = enddate;
			this.accountid = accountid;
			this.description = description;
			this.relatedtotypeid = relatedtotypeid;
			this.ownerid = ownerid;
		}

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

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getRelatedto() {
			return relatedto;
		}

		public void setRelatedto(String relatedto) {
			this.relatedto = relatedto;
		}

		public long getStartdate() {
			return startdate;
		}

		public void setStartdate(long startdate) {
			this.startdate = startdate;
		}

		public String getReminderdate() {
			return reminderdate;
		}

		public void setReminderdate(String reminderdate) {
			this.reminderdate = reminderdate;
		}

		public long getEnddate() {
			return enddate;
		}

		public void setEnddate(long enddate) {
			this.enddate = enddate;
		}

		public int getAccountid() {
			return accountid;
		}

		public void setAccountid(int accountid) {
			this.accountid = accountid;
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

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getRelatedtotypeid() {
			return relatedtotypeid;
		}

		public void setRelatedtotypeid(String relatedtotypeid) {
			this.relatedtotypeid = relatedtotypeid;
		}
	}

}
