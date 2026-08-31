package io.rcrm.api.pojo.albatross.jobs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateJobRequest {
    private JobUpdateData job;
    private boolean address_changed;
    private Object[] filesInfo;
    private String deleteJobKey;
    private Object[] secondaryContacts;
    private Object[] xml_feeds;
    private Object[] jobParserData;
    private Collaborator collaborator;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Collaborator {
        private Integer[] user_ids;
        private Integer[] team_ids;
    }
} 