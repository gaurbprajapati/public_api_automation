package io.rcrm.api.pojo;

public class SelectedEntity {
    private int entity_type;
    private String entity_slug;
    private int entity_id;
    private int entity_owner;
    private String entity_name;

    // Constructors, Getters, and Setters
    public SelectedEntity(int entity_type, String entity_slug, int entity_id, int entity_owner, String entity_name) {
        this.entity_type = entity_type;
        this.entity_slug = entity_slug;
        this.entity_id = entity_id;
        this.entity_owner = entity_owner;
        this.entity_name = entity_name;
    }

    public int getentity_type() {
        return entity_type;
    }

    public void setentity_type(int entity_type) {
        this.entity_type = entity_type;
    }

    public String getentity_slug() {
        return entity_slug;
    }

    public void setentity_slug(String entity_slug) {
        this.entity_slug = entity_slug;
    }

    public int getentity_id() {
        return entity_id;
    }

    public void setentity_id(int entity_id) {
        this.entity_id = entity_id;
    }

    public int getentity_owner() {
        return entity_owner;
    }

    public void setentity_owner(int entity_owner) {
        this.entity_owner = entity_owner;
    }

    public String getentity_name() {
        return entity_name;
    }

    public void setentity_name(String entity_name) {
        this.entity_name = entity_name;
    }
}