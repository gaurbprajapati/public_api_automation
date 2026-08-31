package io.rcrm.api.pojo.albatross.SequencingState;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SequencingState {
    private String entity_name;
    private String entity_slug;
    private String sequencing_data;
}
