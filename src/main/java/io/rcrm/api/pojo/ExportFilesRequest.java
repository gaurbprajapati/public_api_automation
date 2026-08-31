package io.rcrm.api.pojo;

import java.util.List;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExportFilesRequest {

    private List<Integer> entityIds;
    private List<String> columnKeys;
    private String entityName;

}
