package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class BreakInterval {
    private int breakStartTime;
    private int breakEndTime;
    private int id;

    // Manual setters and getters to ensure they work
    public void setBreakStartTime(int breakStartTime) {
        this.breakStartTime = breakStartTime;
    }

    public void setBreakEndTime(int breakEndTime) {
        this.breakEndTime = breakEndTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBreakStartTime() {
        return this.breakStartTime;
    }

    public int getBreakEndTime() {
        return this.breakEndTime;
    }

    public int getId() {
        return this.id;
    }
}