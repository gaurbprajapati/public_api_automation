package io.rcrm.api.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationHistoryRequestInCandidateDetailPage {
    private int id;
    private String institute_name;
    private String educational_qualification;
    private String educational_specialization;
    private String grade;
    private String education_location;
    private long education_start_date;
    private long education_end_date;
    private String education_description;
    private int is_manually_added;
    private int candidate_id;
    private String candidate_slug;

    // Custom constructor without id
    public EducationHistoryRequestInCandidateDetailPage(String institute_name, String educational_qualification,
                                                        String educational_specialization, String grade,
                                                        String education_location, long education_start_date,
                                                        long education_end_date, String education_description,
                                                        int is_manually_added, int candidate_id, String candidate_slug) {
        this.institute_name = institute_name;
        this.educational_qualification = educational_qualification;
        this.educational_specialization = educational_specialization;
        this.grade = grade;
        this.education_location = education_location;
        this.education_start_date = education_start_date;
        this.education_end_date = education_end_date;
        this.education_description = education_description;
        this.is_manually_added = is_manually_added;
        this.candidate_id = candidate_id;
        this.candidate_slug = candidate_slug;
    }
}