package io.rcrm.api.pojo.albatross;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SavedSearch {
    private String name;
    private String entitytype;
    private String json;
    private Object userid;
    private Object accountid;
    private int share_with_teammates;
    private int post_search_revamp;
    private List<Object> collaborator_id;
    private List<Object> collaborator_type;

}