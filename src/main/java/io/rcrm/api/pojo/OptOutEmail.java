package io.rcrm.api.pojo;

public class OptOutEmail {
    private String related_to_type;
    private String related_to;
    private String opt_out;

    public OptOutEmail(){

    }

    public OptOutEmail(String related_to_type, String related_to, String opt_out){
        this.related_to_type = related_to_type;
        this.related_to = related_to;
        this.opt_out = opt_out;
    }

    public String getRelated_to_type() {
        return related_to_type;
    }

    public void setRelated_to_type(String related_to_type) {
        this.related_to_type = related_to_type;
    }

    public String getRelated_to() {
        return related_to;
    }

    public void setRelated_to(String related_to) {
        this.related_to = related_to;
    }

    public String getOpt_out() {
        return opt_out;
    }

    public void setOpt_out(String opt_out) {
        this.opt_out = opt_out;
    }
}
