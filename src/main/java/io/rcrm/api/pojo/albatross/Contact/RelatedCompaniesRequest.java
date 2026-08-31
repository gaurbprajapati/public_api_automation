package io.rcrm.api.pojo.albatross.Contact;

import java.util.List;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RelatedCompaniesRequest {
    private List<Integer> contactIds;
    private boolean fromListPage;
}
