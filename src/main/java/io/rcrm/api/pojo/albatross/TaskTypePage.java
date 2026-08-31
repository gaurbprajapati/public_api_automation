package io.rcrm.api.pojo.albatross;

public class TaskTypePage {

    private int id;
    private String label;
    private int defaultvalue;
    private boolean deleted;

    public TaskTypePage(int id, String label, int defaultvalue, boolean deleted) {
        super();
        this.id = id;
        this.label = label;
        this.defaultvalue = defaultvalue;
        this.deleted = deleted;
    }

    public TaskTypePage() {
        super();
        // TODO Auto-generated constructor stub
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getDefault() {
        return defaultvalue;
    }

    public void setDefault(int defaultvalue) {
        this.defaultvalue = defaultvalue;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

}
