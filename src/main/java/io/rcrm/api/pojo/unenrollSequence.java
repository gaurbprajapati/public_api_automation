package io.rcrm.api.pojo;

public class unenrollSequence {

		public unenrollSequence() {
			super();
		}

		public unenrollSequence( int unenrolled_by,String prospect_slug) {
			super();
			this.unenrolled_by = unenrolled_by;
			this.prospect_slug = prospect_slug;
		}

		private int unenrolled_by;
		private String  prospect_slug;

		public int getunenrolled_by() {
			return unenrolled_by;
		}

		public void setUnenrolled_by(int unenrolled_by) {
			this.unenrolled_by = unenrolled_by;
		}
		public String getprospect_slug() {
			return prospect_slug;
		}

		public void setProspect_slug(String prospect_slug) {
			this.prospect_slug = prospect_slug;
		}


	}