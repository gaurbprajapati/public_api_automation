package io.rcrm.api.pojo.albatross.jobs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJob {
    private Job job;
    
    // Inner Job class
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Job {
        private String name;
        private String description;
        private int noofopenings;
        private String companyid;
        private String contactid;
        private int ownerid;
        private String job_type;
        private Double pay_rate;
        private Double bill_rate;
        private Integer calculate_charge_by;
        private Double margin_percentage;
        private Double markup_percentage;

        // Custom constructor for convenience
        public Job(String name, String companyid, int ownerid) {
            this.name = name;
            this.companyid = companyid;
            this.ownerid = ownerid;
            this.noofopenings = 1;
        }
    }
}
