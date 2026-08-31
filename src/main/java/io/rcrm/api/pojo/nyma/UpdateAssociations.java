package io.rcrm.api.pojo.nyma;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAssociations {

    private String thread_id;
    private String associated_entity;
    private String associated_entity_type_id;
    private String event_type;
}