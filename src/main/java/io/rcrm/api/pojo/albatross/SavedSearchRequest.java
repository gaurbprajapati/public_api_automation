package io.rcrm.api.pojo.albatross;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedSearchRequest {
    private SavedSearch save_searches;
    private boolean updateUserObj;
}