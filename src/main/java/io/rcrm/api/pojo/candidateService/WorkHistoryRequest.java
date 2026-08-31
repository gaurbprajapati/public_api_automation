package io.rcrm.api.pojo.candidateService;

public class WorkHistoryRequest {
    private String title;
    private String workCompanyName;
    private int employmentType;
    private int industryId;
    private String workLocation;
    private int salary;
    private boolean isCurrentlyWorking;
    private int workStartDate;
    private int workEndDate;
    private String workDescription;
    private boolean isManuallyAdded;
    private String candidateSlug;

    // Constructors
    public WorkHistoryRequest() {
    }

    public WorkHistoryRequest(String title, String workCompanyName, int employmentType, int industryId,
            String workLocation, int salary, boolean isCurrentlyWorking, int workStartDate,
            int workEndDate, String workDescription, boolean isManuallyAdded, String candidateSlug) {
        this.title = title;
        this.workCompanyName = workCompanyName;
        this.employmentType = employmentType;
        this.industryId = industryId;
        this.workLocation = workLocation;
        this.salary = salary;
        this.isCurrentlyWorking = isCurrentlyWorking;
        this.workStartDate = workStartDate;
        this.workEndDate = workEndDate;
        this.workDescription = workDescription;
        this.isManuallyAdded = isManuallyAdded;
        this.candidateSlug = candidateSlug;
    }

    // Getters and Setters (you can generate these using IDE or manually)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWorkCompanyName() {
        return workCompanyName;
    }

    public void setWorkCompanyName(String workCompanyName) {
        this.workCompanyName = workCompanyName;
    }

    public int getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(int employmentType) {
        this.employmentType = employmentType;
    }

    public int getIndustryId() {
        return industryId;
    }

    public void setIndustryId(int industryId) {
        this.industryId = industryId;
    }

    public String getWorkLocation() {
        return workLocation;
    }

    public void setWorkLocation(String workLocation) {
        this.workLocation = workLocation;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public boolean isCurrentlyWorking() {
        return isCurrentlyWorking;
    }

    public void setCurrentlyWorking(boolean currentlyWorking) {
        isCurrentlyWorking = currentlyWorking;
    }

    public int getWorkStartDate() {
        return workStartDate;
    }

    public void setWorkStartDate(int workStartDate) {
        this.workStartDate = workStartDate;
    }

    public int getWorkEndDate() {
        return workEndDate;
    }

    public void setWorkEndDate(int workEndDate) {
        this.workEndDate = workEndDate;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }

    public boolean isManuallyAdded() {
        return isManuallyAdded;
    }

    public void setIsManuallyAdded(boolean IsManuallyAdded) {
        isManuallyAdded = IsManuallyAdded;
    }

    public String getCandidateSlug() {
        return candidateSlug;
    }

    public void setCandidateSlug(String candidateSlug) {
        this.candidateSlug = candidateSlug;
    }
}
