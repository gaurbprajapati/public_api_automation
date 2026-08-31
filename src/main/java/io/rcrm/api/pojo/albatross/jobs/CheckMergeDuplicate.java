package io.rcrm.api.pojo.albatross.jobs;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckMergeDuplicate {
    private Integer primaryJobId;
    private Integer secondaryJobId;
}
