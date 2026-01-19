package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Material;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaterialResponse {
    private Long id;
    private String title;
    private String isbn;
    private String thumbnailUrl;
    private boolean isPublic; // 공용 교재 여부

    public static MaterialResponse from(Material material) {
        return MaterialResponse.builder()
                .id(material.getId())
                .title(material.getTitle())
                .isbn(material.getIsbn())
                .thumbnailUrl(material.getThumbnailUrl())
                .isPublic(material.getAcademy() == null)
                .build();
    }
}
