package io.rcrm.api.pojo.albatross.jobs;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobCustomFields {

    private String source;
    private String jobslug;

}
