package io.rcrm.api.pojo.reaper;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmailTemplateRequestBody {
    
    private String emailcontext;
    private String emailsubject;
    private String template;
    private Integer accountid;
    private Integer createdby;
    private Integer relatedtotypeid;
    private Integer share;

}
