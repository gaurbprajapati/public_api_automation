package io.rcrm.api.pojo.albatross.jobs;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobExternalPage {

    private int limit;
    private int offset;
    private String search_data;
    private boolean onlyJobs;
    
}
