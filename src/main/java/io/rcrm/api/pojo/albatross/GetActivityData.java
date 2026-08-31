package io.rcrm.api.pojo.albatross;

public class GetActivityData {
    private String type;
    private int pagesize;
    private String page;
    private String relatedToSlug;
    private int relatedtotypeid;
    private String relatedtocompany; // Assuming 'null' should be represented as a nullable String
    private int offset;

    public GetActivityData() {
        super();
    }

    public GetActivityData(String type, int pagesize, String page, String relatedToSlug, int relatedtotypeid, String relatedtocompany, int offset) {
        this.type = type;
        this.pagesize = pagesize;
        this.page = page;
        this.relatedToSlug = relatedToSlug;
        this.relatedtotypeid = relatedtotypeid;
        this.relatedtocompany = relatedtocompany;
        this.offset = offset;
    }

    // Getters
    public String getType() {
        return type;
    }

    public int getPagesize() {
        return pagesize;
    }

    public String getPage() {
        return page;
    }

    public String getRelatedToSlug() {
        return relatedToSlug;
    }

    public int getRelatedtotypeid() {
        return relatedtotypeid;
    }

    public String getRelatedtocompany() {
        return relatedtocompany;
    }

    public int getOffset() {
        return offset;
    }

    // Setters
    public void setType(String type) {
        this.type = type;
    }

    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public void setRelatedToSlug(String relatedToSlug) {
        this.relatedToSlug = relatedToSlug;
    }

    public void setRelatedtotypeid(int relatedtotypeid) {
        this.relatedtotypeid = relatedtotypeid;
    }

    public void setRelatedtocompany(String relatedtocompany) {
        this.relatedtocompany = relatedtocompany;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

}
