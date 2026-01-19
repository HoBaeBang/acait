package com.aslan.academymanagement.service.material;

import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.Material;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.MaterialRequest;
import com.aslan.academymanagement.dto.MaterialResponse;
import com.aslan.academymanagement.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MaterialService {

    private final MaterialRepository materialRepository;

    @Transactional(readOnly = true)
    public List<MaterialResponse> searchMaterials(Member member, String keyword) {
        Academy academy = member.getAcademy();
        return materialRepository.searchByTitle(keyword, academy).stream()
                .map(MaterialResponse::from)
                .collect(Collectors.toList());
    }

    public MaterialResponse createMaterial(Member member, MaterialRequest request) {
        Academy academy = member.getAcademy();

        Material material = Material.builder()
                .academy(academy) // 학원 전용 교재로 등록
                .title(request.getTitle())
                .isbn(request.getIsbn())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();

        Material saved = materialRepository.save(material);
        return MaterialResponse.from(saved);
    }
}
