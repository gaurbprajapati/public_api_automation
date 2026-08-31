package io.rcrm.api.pojo.candidateService;

public class SavedPitchedField {
    private String fieldName;
    private int visible;

    // Constructors
    public SavedPitchedField() {
    }

    public SavedPitchedField(String fieldName, int visible) {
        this.fieldName = fieldName;
        this.visible = visible;
    }

    // Getters and Setters
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public int getVisible() {
        return visible;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }
}
