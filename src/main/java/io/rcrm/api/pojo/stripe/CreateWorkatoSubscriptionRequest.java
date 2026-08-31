package io.rcrm.api.pojo.stripe;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkatoSubscriptionRequest {

    private Plan plan;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Plan {
        private Integer existing_plan;
        private String plan_cycle;
        private String task;
    }
}