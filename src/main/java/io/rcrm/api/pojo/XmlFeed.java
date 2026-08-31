package io.rcrm.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class XmlFeed {

    @JsonProperty("default")
    private String def;
    private String custom;

    public XmlFeed() {}

    public XmlFeed(String def, String custom) {
        this.def = def;
        this.custom = custom;
    }

    public String getDefault() {
        return def;
    }

    public void setDefault(String def) {
        this.def = def;
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }
}
