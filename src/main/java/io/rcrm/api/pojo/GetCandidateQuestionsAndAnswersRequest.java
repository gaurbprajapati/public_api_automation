package io.rcrm.api.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetCandidateQuestionsAndAnswersRequest {
    private int id;
    private int filterQuestions;
    private int filterUnasnwered;

}