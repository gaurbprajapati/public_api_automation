package io.rcrm.api.pojo.albatross.jobs;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchEntity {
    private String search;
    private boolean candidates;
    private boolean compnaies;
    private boolean jobs;
    private boolean contacts;
    private boolean users;
    private boolean isMergeSearch;
}
