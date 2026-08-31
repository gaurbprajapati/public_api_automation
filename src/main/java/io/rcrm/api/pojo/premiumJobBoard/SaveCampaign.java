package io.rcrm.api.pojo.premiumJobBoard;

public class SaveCampaign {

    public SaveCampaign() {
    }

    private String campaign_name;
    private int job_id;
    private channels[] channels;
    private String total_channels;
    private String status;
    private String campaign_id;
    private String currency;
    private int total_price;
    private String draft_id;

    public String getCampaign_name() {
        return campaign_name;
    }

    public void setCampaign_name(String campaign_name) {
        this.campaign_name = campaign_name;
    }

    public int getJob_id() {
        return job_id;
    }

    public void setJob_id(int job_id) {
        this.job_id = job_id;
    }

    public channels[] getChannels() {
        return channels;
    }

    public void setChannels(channels[] channels) {
        this.channels = channels;
    }

    public String getTotal_channels() {
        return total_channels;
    }

    public void setTotal_channels(String total_channels) {
        this.total_channels = total_channels;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCampaign_id() {
        return campaign_id;
    }

    public void setCampaign_id(String campaign_id) {
        this.campaign_id = campaign_id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getTotal_price() {
        return total_price;
    }

    public void setTotal_price(int total_price) {
        this.total_price = total_price;
    }

    public String getDraft_id() {
        return draft_id;
    }

    public void setDraft_id(String draft_id) {
        this.draft_id = draft_id;
    }

    public static class channels {

        public channels() {
        }

        private String channel_name;
        private String channel_id;
        private boolean is_product;
        private String status;
        private String currency;
        private int total_price;
        private String job_board_link;
        private long start_date;
        private long end_date;

        public String getChannel_name() {
            return channel_name;
        }

        public void setChannel_name(String channel_name) {
            this.channel_name = channel_name;
        }

        public String getChannel_id() {
            return channel_id;
        }

        public void setChannel_id(String channel_id) {
            this.channel_id = channel_id;
        }

        public boolean getIs_product() {
            return is_product;
        }

        public void setIs_product(boolean is_product) {
            this.is_product = is_product;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public int getTotal_price() {
            return total_price;
        }

        public void setTotal_price(int total_price) {
            this.total_price = total_price;
        }

        public String getJob_board_link() {
            return job_board_link;
        }

        public void setJob_board_link(String job_board_link) {
            this.job_board_link = job_board_link;
        }

        public long getStart_date() {
            return start_date;
        }

        public void setStart_date(long start_date) {
            this.start_date = start_date;
        }

        public long getEnd_date() {
            return end_date;
        }

        public void setEnd_date(long end_date) {
            this.end_date = end_date;
        }
    }
}