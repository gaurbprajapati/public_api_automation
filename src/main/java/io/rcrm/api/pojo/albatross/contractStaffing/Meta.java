package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class Meta {
    private String message;
    private String requestUuid;
    private ResponseType responseType;
    private String timestamp;
    private int status;
}
