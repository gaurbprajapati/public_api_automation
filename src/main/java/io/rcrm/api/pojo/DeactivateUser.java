package io.rcrm.api.pojo;

public class DeactivateUser {
    private boolean disabled = true;
    private boolean isKeepLicense;
    private User user;

    // Getters and Setters
    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isKeepLicense() {
        return isKeepLicense;
    }

    public void setKeepLicense(boolean keepLicense) {
        isKeepLicense = keepLicense;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static class User {
        private int id;
        private String firstname;
        private String lastname;
        private int userstatus = 1;
        private int verifiedemail = 0;
        private int contactverified = 0;
        private Roleid roleid;
        

        // Getters and Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

		public String getFirstname() {
			return firstname;
		}

		public void setFirstname(String firstname) {
			this.firstname = firstname;
		}

		public String getLastname() {
			return lastname;
		}

		public void setLastname(String lastname) {
			this.lastname = lastname;
		}

		public int getUserstatus() {
			return userstatus;
		}

		public void setUserstatus(int userstatus) {
			this.userstatus = userstatus;
		}

		public int getVerifiedemail() {
			return verifiedemail;
		}

		public void setVerifiedemail(int verifiedemail) {
			this.verifiedemail = verifiedemail;
		}

		public int getContactverified() {
			return contactverified;
		}

		public void setContactverified(int contactverified) {
			this.contactverified = contactverified;
		}

        public Roleid getRoleid() {
            return roleid;
        }

        public void setRoleid(Roleid roleid) {
            this.roleid = roleid;
        }
    }
}