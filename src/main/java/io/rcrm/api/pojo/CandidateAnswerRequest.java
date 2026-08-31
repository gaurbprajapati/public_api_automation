package io.rcrm.api.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateAnswerRequest {
    private String answer;
    private int questionid;
    private Integer answerid;
    private int candidateid;
}