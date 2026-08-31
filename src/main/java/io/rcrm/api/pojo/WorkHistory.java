package io.rcrm.api.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkHistory {
    private String candidate_slug = "";
    private String work_company_name = "";
    private String title = "";
    private int employment_type = 1;
    private int industry_id = 1;
    private String work_location = "";
    private int is_currently_working = 0;
    private int work_start_date = 0;
    private int work_end_date = 0;
    private String work_description = "";
    private int salary = 0;
    private String institute_name;
    private String educational_qualification;
    private int candidate_id = 0;

    public WorkHistory() {

    }

    public WorkHistory(String candidate_slug, String work_company_name, String title, int employment_type,
                       int industry_id, String work_location, int is_currently_working, int work_start_date, int work_end_date,
                       String work_description, int salary) {
        super();
        this.candidate_slug = candidate_slug;
        this.work_company_name = work_company_name;
        this.title = title;
        this.employment_type = employment_type;
        this.industry_id = industry_id;
        this.work_location = work_location;
        this.is_currently_working = is_currently_working;
        this.work_start_date = work_start_date;
        this.work_end_date = work_end_date;
        this.work_description = work_description;
        this.salary = salary;
    }

    public void WorkHistory1(String institute_name, String educational_qualification) {
        this.institute_name = institute_name;
        this.educational_qualification = educational_qualification;
    }

    public WorkHistory(String candidate_slug, String work_company_name, String title) {
        super();
        this.candidate_slug = candidate_slug;
        this.work_company_name = work_company_name;
        this.title = title;
    }

    public WorkHistory(String candidate_slug, String title) {
        super();
        this.candidate_slug = candidate_slug;
        this.title = title;
    }

    public WorkHistory(int candidate_id, String candidate_slug, String work_company_name, String title, int employment_type,
                       int industry_id, String work_location, int is_currently_working, int work_start_date, int work_end_date,
                       String work_description, int salary) {
        this.candidate_id = candidate_id;
        this.candidate_slug = candidate_slug;
        this.work_company_name = work_company_name;
        this.title = title;
        this.employment_type = employment_type;
        this.industry_id = industry_id;
        this.work_location = work_location;
        this.is_currently_working = is_currently_working;
        this.work_start_date = work_start_date;
        this.work_end_date = work_end_date;
        this.work_description = work_description;
        this.salary = salary;
    }
}
