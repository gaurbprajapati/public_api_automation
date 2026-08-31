package io.rcrm.api.pojo;

public class ParseJob {

    private ResumeParserData resumeParserData;
    private Boolean onlyParserData;

    public ParseJob() {
    }

    public ResumeParserData getResumeParserData() {
        return resumeParserData;
    }

    public void setResumeParserData(ResumeParserData resumeParserData) {
        this.resumeParserData = resumeParserData;
    }

    public Boolean getOnlyParserData() {
        return onlyParserData;
    }

    public void setOnlyParserData(Boolean onlyParserData) {
        this.onlyParserData = onlyParserData;
    }

    public static class ResumeParserData {
        private DetailFilename detailfilename;

        public DetailFilename getDetailfilename() {
            return detailfilename;
        }

        public void setDetailfilename(DetailFilename detailfilename) {
            this.detailfilename = detailfilename;
        }
    }

    public static class DetailFilename {
        private String key;
        private String name;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
