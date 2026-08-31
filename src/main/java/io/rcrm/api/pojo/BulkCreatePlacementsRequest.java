package io.rcrm.api.pojo;

import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BulkCreatePlacementsRequest {

    private List<PlacementItem> placements;
    
}
