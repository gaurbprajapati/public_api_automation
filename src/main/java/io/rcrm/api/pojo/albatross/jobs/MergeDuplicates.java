package io.rcrm.api.pojo.albatross.jobs;
import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MergeDuplicates {
    private List<Integer> selectedEntities;
    private int mergeTo;
    private int entityTypeId;
}
