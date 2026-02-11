package com.aslan.academymanagement.service.deduction;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.dto.DeductionItemRequest;
import com.aslan.academymanagement.dto.DeductionItemResponse;

import java.util.List;

public interface DeductionItemService {
    List<DeductionItemResponse> getDeductionItems(Member owner);
    DeductionItemResponse createDeductionItem(Member owner, DeductionItemRequest request);
    DeductionItemResponse updateDeductionItem(Member owner, Long itemId, DeductionItemRequest request);
    void deleteDeductionItem(Member owner, Long itemId);
}
