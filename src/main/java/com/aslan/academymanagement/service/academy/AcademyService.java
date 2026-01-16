package com.aslan.academymanagement.service.academy;

import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.dto.AcademyRequest;
import com.aslan.academymanagement.repository.AcademyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AcademyService {

    private final AcademyRepository academyRepository;

    @Transactional
    public Academy createAcademy(AcademyRequest request) {
        Academy academy = new Academy(request.getName());
        return academyRepository.save(academy);
    }
}
