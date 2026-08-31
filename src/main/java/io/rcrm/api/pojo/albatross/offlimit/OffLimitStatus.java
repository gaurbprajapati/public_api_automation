package io.rcrm.api.pojo.albatross.offlimit;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OffLimitStatus {

    private offLimitStatus[] offLimitStatus;

    public OffLimitStatus.offLimitStatus[] getOffLimitStatus() {
        return offLimitStatus;
    }

    public void setOffLimitStatus(OffLimitStatus.offLimitStatus[] offLimitStatus) {
        this.offLimitStatus = offLimitStatus;
    }

    public static class offLimitStatus {
        private String status_label;
        private String status_colour_id;
        private int sequence_no;
        private String account_id;
        //Field name in JSON is "default" which is a reserved keyword in Java. so we cannot use "default" as an identifier.
        @JsonProperty("default")
        private String defaultStatus;
        private String offlimit_status_colour_id;
        private String background_color_hex;
        private String text_color_hex;
        private int count;

        public int getSequence_no() {
            return sequence_no;
        }

        public void setSequence_no(int sequence_no) {
            this.sequence_no = sequence_no;
        }

        public String getDefaultStatus() {
            return defaultStatus;
        }

        public void setDefaultStatus(String defaultStatus) {
            this.defaultStatus = defaultStatus;
        }

        public String getStatus_label() {
            return status_label;
        }

        public void setStatus_label(String status_label) {
            this.status_label = status_label;
        }

        public String getStatus_colour_id() {
            return status_colour_id;
        }

        public void setStatus_colour_id(String status_colour_id) {
            this.status_colour_id = status_colour_id;
        }

        public String getAccount_id() {
            return account_id;
        }

        public void setAccount_id(String account_id) {
            this.account_id = account_id;
        }

        public String getOfflimit_status_colour_id() {
            return offlimit_status_colour_id;
        }

        public void setOfflimit_status_colour_id(String offlimit_status_colour_id) {
            this.offlimit_status_colour_id = offlimit_status_colour_id;
        }

        public String getBackground_color_hex() {
            return background_color_hex;
        }

        public void setBackground_color_hex(String background_color_hex) {
            this.background_color_hex = background_color_hex;
        }

        public String getText_color_hex() {
            return text_color_hex;
        }

        public void setText_color_hex(String text_color_hex) {
            this.text_color_hex = text_color_hex;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }


}
