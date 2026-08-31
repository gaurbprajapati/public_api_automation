package io.rcrm.api.pojo.albatross.global;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class UpdateFieldWidgetCustomizationRequest {
    private int id;
    private boolean isSilentProcess;
    private String key;
    private String tableFlag;
    private String value;
}
