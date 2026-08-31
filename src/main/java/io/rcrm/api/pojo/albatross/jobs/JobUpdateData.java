package io.rcrm.api.pojo.albatross.jobs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobUpdateData {
    private String slug;
    private String name;
    private String description;
    private int noofopenings;
    private int qualificationid;
    private String specialization;
    private int minexperienceinyears;
    private int maxexperienceinyears;
    private int annualsalarymin;
    private int annualsalarymax;
    private String salarytype;
    private String job_type;
    private String locality;
    private String city;
    private String country;
    private String postalcode;
    private String state;
    private String address;
    private int currencyid;
    private int companyid;
    private int contactid;
    private String details;
    private String detailfilename;
    private int allowapply;
    private String jobcode;
    private int showcompany;
    private int showaccountname;
    private int jobstatus;
    private String jobstatuscomment;
    private String collaborator;
    private int ownerid;
    private String jobquestions;
    private String jdtext;
    private String job_category;
    private String job_skill;
    private String remote;
    private int pay_rate;
    private int bill_rate;
    private int id;
    private int jobpostingstatus;
    private int jobpostingdate;
    private String custcolumn10;
    private int hiring_pipeline_id;
    private String mapped_pending_job_id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer calculate_charge_by;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double margin_percentage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double markup_percentage;
}