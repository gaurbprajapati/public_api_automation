package io.rcrm.api.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateInvoiceRequest {

    private Integer template_id;
    private String description;
    private String company_slug;
    private String associated_contact_slugs;
    private Object invoice_fields;
    private String address;
    private String contact_slug;
    private String contact_number;
    private String email;
    private String issue_date;
    private String due_date;
    private String additional_note;
    private Integer currency_id;
    private String associated_candidate_slugs;
    private String associated_company_slugs;
    private String associated_job_slugs;
    private String associated_deal_slugs;
    private List<CustomField> internal_custom_fields;
}