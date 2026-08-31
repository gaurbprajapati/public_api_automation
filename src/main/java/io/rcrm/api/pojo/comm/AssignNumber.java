package io.rcrm.api.pojo.comm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssignNumber {
    private String user_id;
    private String number_title;
    private String masked_number;
    private String voice_reply;
    private String availability;
    private String phone_number_id;
}
