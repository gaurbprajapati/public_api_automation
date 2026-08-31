package io.rcrm.api.pojo.ostrich;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Tasks {
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonProperty("page_size")
    private int pageSize;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int page;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonProperty("sort_by")
    private String sortBy;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String sortOrder;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String searchTerm;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String currentTime;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonProperty("user_ids")
    private List<Integer> userIds;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonProperty("user_slugs")
    private List<String> userSlugs;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonProperty("team_ids")
    private List<Integer> teamIds;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private DateRange createdOn;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private Map<String, List<String>> associations;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private Map<String, List<String>> relatedTo;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean isFilterApplied;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private DateRange dueDate;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> meetingTypes;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String startDate;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> createdBy;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> updatedBy;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private DateRange updatedOn;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> attendeesId;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<String> attendeeSlugs;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean relatedToAsNone;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> ownedByTeamIds;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> ownerIds;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> ownedByTeam;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> createdByTeamIds;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> createdByIds;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> createdByTeam;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> updatedByTeamIds;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> updatedByIds;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> updatedByTeam;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> taskStatus;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> meetingStatus;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Integer> relatedToType;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public List<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }

    public List<String> getUserSlugs() {
        return userSlugs;
    }

    public void setUserSlugs(List<String> userSlugs) {
        this.userSlugs = userSlugs;
    }

    public List<Integer> getTeamIds() {
        return teamIds;
    }

    public void setTeamIds(List<Integer> teamIds) {
        this.teamIds = teamIds;
    }

    public DateRange getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateRange createdOn) {
        this.createdOn = createdOn;
    }

    public Map<String, List<String>> getAssociations() {
        return associations;
    }

    public void setAssociations(Map<String, List<String>> associations) {
        this.associations = associations;
    }

    public Map<String, List<String>> getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(Map<String, List<String>> relatedTo) {
        this.relatedTo = relatedTo;
    }

    public boolean getIsFilterApplied() {
        return isFilterApplied;
    }

    public void setIsFilterApplied(boolean filterApplied) {
        isFilterApplied = filterApplied;
    }

    public DateRange getDueDate() {
        return dueDate;
    }

    public void setDueDate(DateRange dueDate) {
        this.dueDate = dueDate;
    }

    public List<Integer> getMeetingTypes() {
        return meetingTypes;
    }

    public void setMeetingTypes(List<Integer> meetingTypes) {
        this.meetingTypes = meetingTypes;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public List<Integer> getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(List<Integer> createdBy) {
        this.createdBy = createdBy;
    }

    public List<Integer> getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(List<Integer> updatedBy) {
        this.updatedBy = updatedBy;
    }

    public DateRange getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(DateRange updatedOn) {
        this.updatedOn = updatedOn;
    }

    public List<Integer> getAttendeesId() {
        return attendeesId;
    }

    public void setAttendeesId(List<Integer> attendeesId) {
        this.attendeesId = attendeesId;
    }

    public List<String> getAttendeeSlugs() {
        return attendeeSlugs;
    }

    public void setAttendeeSlugs(List<String> attendeeSlugs) {
        this.attendeeSlugs = attendeeSlugs;
    }

    public boolean isRelatedToAsNone() {
        return relatedToAsNone;
    }

    public void setRelatedToAsNone(boolean relatedToAsNone) {
        this.relatedToAsNone = relatedToAsNone;
    }

    public List<Integer> getOwnedByTeamIds() {
        return ownedByTeamIds;
    }

    public void setOwnedByTeamIds(List<Integer> ownedByTeamIds) {
        this.ownedByTeamIds = ownedByTeamIds;
    }

    public List<Integer> getOwnerIds() {
        return ownerIds;
    }

    public void setOwnerIds(List<Integer> ownerIds) {
        this.ownerIds = ownerIds;
    }

    public List<Integer> getOwnedByTeam() {
        return ownedByTeam;
    }

    public void setOwnedByTeam(List<Integer> ownedByTeam) {
        this.ownedByTeam = ownedByTeam;
    }

    public List<Integer> getCreatedByTeamIds() {
        return createdByTeamIds;
    }

    public void setCreatedByTeamIds(List<Integer> createdByTeamIds) {
        this.createdByTeamIds = createdByTeamIds;
    }

    public List<Integer> getCreatedByIds() {
        return createdByIds;
    }

    public void setCreatedByIds(List<Integer> createdByIds) {
        this.createdByIds = createdByIds;
    }

    public List<Integer> getCreatedByTeam() {
        return createdByTeam;
    }

    public void setCreatedByTeam(List<Integer> createdByTeam) {
        this.createdByTeam = createdByTeam;
    }

    public List<Integer> getUpdatedByTeamIds() {
        return updatedByTeamIds;
    }

    public void setUpdatedByTeamIds(List<Integer> updatedByTeamIds) {
        this.updatedByTeamIds = updatedByTeamIds;
    }

    public List<Integer> getUpdatedByIds() {
        return updatedByIds;
    }

    public void setUpdatedByIds(List<Integer> updatedByIds) {
        this.updatedByIds = updatedByIds;
    }

    public List<Integer> getUpdatedByTeam() {
        return updatedByTeam;
    }

    public void setUpdatedByTeam(List<Integer> updatedByTeam) {
        this.updatedByTeam = updatedByTeam;
    }

    public List<Integer> getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(List<Integer> taskStatus) {
        this.taskStatus = taskStatus;
    }

    public List<Integer> getMeetingStatus() {
        return meetingStatus;
    }

    public void setMeetingStatus(List<Integer> meetingStatus) {
        this.meetingStatus = meetingStatus;
    }

    public List<Integer> getRelatedToType() {
        return relatedToType;
    }

    public void setRelatedToType(List<Integer> relatedToType) {
        this.relatedToType = relatedToType;
    }

}