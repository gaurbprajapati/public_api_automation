package io.rcrm.api.pojo;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EditPlacementRequest {

    private String company_slug;
    private String candidate_slug;
    private String job_slug;
    private String contact_slugs;
    private String deal_slugs;
    private Integer currency_id;
    private List<CustomField> custom_fields;

}