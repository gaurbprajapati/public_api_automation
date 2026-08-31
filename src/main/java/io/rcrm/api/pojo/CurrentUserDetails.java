package io.rcrm.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserDetails {
    private int id;
    private int timezone;
    private int currencyid;
    
    @JsonProperty("time_format_type")
    private Integer timeFormatType;
}
