package com.aslan.academymanagement.service.schedule;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.ScheduleUpdateRequest;

public interface ScheduleService {
    void updateSchedule(Member teacher, Long scheduleId, ScheduleUpdateRequest request);
}
