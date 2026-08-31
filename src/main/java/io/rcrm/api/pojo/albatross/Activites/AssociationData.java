package io.rcrm.api.pojo.albatross.Activites;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssociationData {

    private String associated_entity;
    private String associated_entity_type_id;
    private int activity_id;
    private String event_type;
    private String activity_type;

}
