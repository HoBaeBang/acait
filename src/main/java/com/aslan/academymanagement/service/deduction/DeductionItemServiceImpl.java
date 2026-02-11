package com.aslan.academymanagement.service.deduction;

import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.DeductionItem;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.DeductionItemRequest;
import com.aslan.academymanagement.dto.DeductionItemResponse;
import com.aslan.academymanagement.repository.DeductionItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DeductionItemServiceImpl implements DeductionItemService {

    private final DeductionItemRepository deductionItemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DeductionItemResponse> getDeductionItems(Member owner) {
        return deductionItemRepository.findAllByAcademy(owner.getAcademy()).stream()
                .map(DeductionItemResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public DeductionItemResponse createDeductionItem(Member owner, DeductionItemRequest request) {
        Academy academy = owner.getAcademy();

        DeductionItem item = DeductionItem.builder()
                .academy(academy)
                .name(request.getName())
                .type(request.getType())
                .value(request.getValue())
                .build();

        DeductionItem savedItem = deductionItemRepository.save(item);
        return DeductionItemResponse.from(savedItem);
    }

    @Override
    public DeductionItemResponse updateDeductionItem(Member owner, Long itemId, DeductionItemRequest request) {
        DeductionItem item = deductionItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공제 항목이 없습니다."));

        if (!item.getAcademy().getId().equals(owner.getAcademy().getId())) {
            throw new IllegalArgumentException("다른 학원의 공제 항목을 수정할 수 없습니다.");
        }

        item.update(request.getName(), request.getType(), request.getValue());
        return DeductionItemResponse.from(item);
    }

    @Override
    public void deleteDeductionItem(Member owner, Long itemId) {
        DeductionItem item = deductionItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공제 항목이 없습니다."));

        if (!item.getAcademy().getId().equals(owner.getAcademy().getId())) {
            throw new IllegalArgumentException("다른 학원의 공제 항목을 삭제할 수 없습니다.");
        }

        deductionItemRepository.delete(item);
    }
}
