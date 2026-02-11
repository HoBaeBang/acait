package com.aslan.academymanagement.repository;

import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.DeductionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeductionItemRepository extends JpaRepository<DeductionItem, Long> {
    List<DeductionItem> findAllByAcademy(Academy academy);
}
