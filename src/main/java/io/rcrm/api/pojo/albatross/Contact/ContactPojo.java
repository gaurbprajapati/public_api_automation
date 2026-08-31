package io.rcrm.api.pojo.albatross.Contact;

import java.util.List;

public class ContactPojo {
    private boolean address_changed;
    private Contact contact;
    private List<SelectedCompany> selectedcompanies;
    private Object filesInfo; 

    public ContactPojo(Contact contact, List<SelectedCompany> selectedcompanies) {
        this.contact = contact;
        this.selectedcompanies = selectedcompanies;
    }

    public void ContactPojo() {
    }


    public boolean isAddress_changed() {
        return address_changed;
    }

    public void setAddress_changed(boolean address_changed) {
        this.address_changed = address_changed;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public List<SelectedCompany> getSelectedcompanies() {
        return selectedcompanies;
    }

    public void setSelectedcompanies(List<SelectedCompany> selectedcompanies) {
        this.selectedcompanies = selectedcompanies;
    }

    public Object getFilesInfo() {
        return filesInfo;
    }

    public void setFilesInfo(Object filesInfo) {
        this.filesInfo = filesInfo;
    }

    public static class Contact {
        private String slug;
        private String firstname;
        private String lastname;
        private String designation;
        private String contactnumber;
        private String address;
        private String city;
        private String locality;
        private String profilefacebook;
        private String profiletwitter;
        private String profilelinkedin;
        private String profilexing;
        private int stageid;
        private int companyid;
        private int ownerid;
        private boolean fromQuickview;

        public Contact(String slug, String firstname, String lastname, String designation, String contactnumber,
                String address, String profilelinkedin) {
            this.slug = slug;
            this.firstname = firstname;
            this.lastname = lastname;
            this.designation = designation;
            this.contactnumber = contactnumber;
            this.address = address;
            this.profilelinkedin = profilelinkedin;
        }

        public Contact() {
            // Default constructor
        }

        // Getters and Setters
        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
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

        public void setlastname(String lastname) {
            this.lastname = lastname;
        }

        public String getDesignation() {
            return designation;
        }

        public void setDesignation(String designation) {
            this.designation = designation;
        }

        public String getContactnumber() {
            return contactnumber;
        }

        public void setContactnumber(String contactnumber) {
            this.contactnumber = contactnumber;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getLocality() {
            return locality;
        }

        public void setLocality(String locality) {
            this.locality = locality;
        }

        public String getProfilefacebook() {
            return profilefacebook;
        }

        public void setProfilefacebook(String profilefacebook) {
            this.profilefacebook = profilefacebook;
        }

        public String getProfiletwitter() {
            return profiletwitter;
        }

        public void setProfiletwitter(String profiletwitter) {
            this.profiletwitter = profiletwitter;
        }

        public String getProfilelinkedin() {
            return profilelinkedin;
        }

        public void setProfilelinkedin(String profilelinkedin) {
            this.profilelinkedin = profilelinkedin;
        }

        public String getProfilexing() {
            return profilexing;
        }

        public void setProfilexing(String profilexing) {
            this.profilexing = profilexing;
        }

        public int getStageid() {
            return stageid;
        }

        public void setStageid(int stageid) {
            this.stageid = stageid;
        }

        public int getCompanyid() {
            return companyid;
        }

        public void setCompanyid(int companyid) {
            this.companyid = companyid;
        }

        public int getOwnerid() {
            return ownerid;
        }

        public void setOwnerid(int ownerid) {
            this.ownerid = ownerid;
        }

        public boolean isFromQuickview() {
            return fromQuickview;
        }

        public void setFromQuickview(boolean fromQuickview) {
            this.fromQuickview = fromQuickview;
        }
    }

    public static class SelectedCompany {
        private String id;
        private String title;
        private int companyid;
        private String slug;

        public SelectedCompany(String id, String title, int companyid, String slug) {
            this.id = id;
            this.title = title;
            this.companyid = companyid;
            this.slug = slug;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getCompanyid() {
            return companyid;
        }

        public void setCompanyid(int companyid) {
            this.companyid = companyid;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }
    }
}
