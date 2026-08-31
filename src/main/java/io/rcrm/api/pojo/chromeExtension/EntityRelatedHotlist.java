package io.rcrm.api.pojo.chromeExtension;

public class EntityRelatedHotlist {
    private int relatedtoid;
    private String pagename;

    public EntityRelatedHotlist(int relatedtoid, String pagename) {
		super();
		this.relatedtoid = relatedtoid;
		this.pagename = pagename;
	}
    
    public EntityRelatedHotlist() {

  	}
	public int getRelatedtoid() {
        return relatedtoid;
    }

    public void setRelatedtoid(int relatedtoid) {
        this.relatedtoid = relatedtoid;
    }

    public String getPagename() {
        return pagename;
    }

    public void setPagename(String pagename) {
        this.pagename = pagename;
    }
}
