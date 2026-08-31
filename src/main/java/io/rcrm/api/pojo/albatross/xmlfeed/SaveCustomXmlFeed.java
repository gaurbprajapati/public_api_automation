package io.rcrm.api.pojo.albatross.xmlfeed;

public class SaveCustomXmlFeed {

    private String title;
    private String parent_xml;
    private String dynamic_job_xml;
    private int decode_xml_data;
    private int is_preselect_xml;
    private int job_last_updatedon_limit;

    public SaveCustomXmlFeed() {}

    public SaveCustomXmlFeed(String title, String parent_xml, String dynamic_job_xml, int decode_xml_data, int is_preselect_xml, int job_last_updatedon_limit) {
        this.title = title;
        this.parent_xml = parent_xml;
        this.dynamic_job_xml = dynamic_job_xml;
        this.decode_xml_data = decode_xml_data;
        this.is_preselect_xml = is_preselect_xml;
        this.job_last_updatedon_limit = job_last_updatedon_limit;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getParent_xml() {
        return parent_xml;
    }

    public void setParent_xml(String parent_xml) {
        this.parent_xml = parent_xml;
    }

    public String getDynamic_job_xml() {
        return dynamic_job_xml;
    }

    public void setDynamic_job_xml(String dynamic_job_xml) {
        this.dynamic_job_xml = dynamic_job_xml;
    }

    public int getDecode_xml_data() {
        return decode_xml_data;
    }

    public void setDecode_xml_data(int decode_xml_data) {
        this.decode_xml_data = decode_xml_data;
    }

    public int getIs_preselect_xml() {
        return is_preselect_xml;
    }

    public void setIs_preselect_xml(int is_preselect_xml) {
        this.is_preselect_xml = is_preselect_xml;
    }

    public int getJob_last_updatedon_limit() {
        return job_last_updatedon_limit;
    }

    public void setJob_last_updatedon_limit(int job_last_updatedon_limit) {
        this.job_last_updatedon_limit = job_last_updatedon_limit;
    }
}
