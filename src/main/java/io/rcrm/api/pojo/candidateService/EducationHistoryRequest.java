package io.rcrm.api.pojo.candidateService;

public class EducationHistoryRequest {
    private String instituteName;
    private String educationalQualification;
    private String educationalSpecialization;
    private String grade;
    private String educationLocation;
    private String educationStartDate;
    private String educationEndDate;
    private String educationDescription;
    private String candidateSlug;
    private boolean isManuallyAdded;

    // Constructors
    public EducationHistoryRequest() {
    }

    public EducationHistoryRequest(String instituteName, String educationalQualification,
            String educationalSpecialization,
            String grade, String educationLocation, String educationStartDate,
            String educationEndDate, String educationDescription, String candidateSlug,
            boolean isManuallyAdded) {
        this.instituteName = instituteName;
        this.educationalQualification = educationalQualification;
        this.educationalSpecialization = educationalSpecialization;
        this.grade = grade;
        this.educationLocation = educationLocation;
        this.educationStartDate = educationStartDate;
        this.educationEndDate = educationEndDate;
        this.educationDescription = educationDescription;
        this.candidateSlug = candidateSlug;
        this.isManuallyAdded = isManuallyAdded;
    }

    // Getters and Setters (You can generate these using IDE or manually)
    public String getInstituteName() {
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
    }

    public String getEducationalQualification() {
        return educationalQualification;
    }

    public void setEducationalQualification(String educationalQualification) {
        this.educationalQualification = educationalQualification;
    }

    public String getEducationalSpecialization() {
        return educationalSpecialization;
    }

    public void setEducationalSpecialization(String educationalSpecialization) {
        this.educationalSpecialization = educationalSpecialization;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getEducationLocation() {
        return educationLocation;
    }

    public void setEducationLocation(String educationLocation) {
        this.educationLocation = educationLocation;
    }

    public String getEducationStartDate() {
        return educationStartDate;
    }

    public void setEducationStartDate(String educationStartDate) {
        this.educationStartDate = educationStartDate;
    }

    public String getEducationEndDate() {
        return educationEndDate;
    }

    public void setEducationEndDate(String educationEndDate) {
        this.educationEndDate = educationEndDate;
    }

    public String getEducationDescription() {
        return educationDescription;
    }

    public void setEducationDescription(String educationDescription) {
        this.educationDescription = educationDescription;
    }

    public String getCandidateSlug() {
        return candidateSlug;
    }

    public void setCandidateSlug(String candidateSlug) {
        this.candidateSlug = candidateSlug;
    }

    public boolean isIsManuallyAdded() {
        return isManuallyAdded;
    }

    public void setIsManuallyAdded(boolean isManuallyAdded) {
        isManuallyAdded = isManuallyAdded;
    }
}
