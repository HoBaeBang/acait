package com.aslan.academymanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequest {

    @NotBlank(message = "교재명은 필수입니다.")
    private String title;

    private String isbn;
    private String thumbnailUrl;
}
