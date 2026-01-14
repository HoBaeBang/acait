package com.aslan.academymanagement.service.record;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.RecordRequest;

public interface LectureRecordService {
    void createRecord(Member teacher, RecordRequest request);
}
