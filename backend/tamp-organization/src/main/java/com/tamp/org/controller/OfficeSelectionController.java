package com.tamp.org.controller;

import com.tamp.org.entity.OfficeSelection;
import com.tamp.org.service.OfficeSelectionService;
import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/office-selections")
@RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
public class OfficeSelectionController {

    private final OfficeSelectionService officeSelectionService;

    public OfficeSelectionController(OfficeSelectionService officeSelectionService) {
        this.officeSelectionService = officeSelectionService;
    }

    @GetMapping("/office/{officeId}")
    public Result<List<OfficeSelection>> getByOfficeId(@PathVariable Long officeId) {
        return Result.ok(officeSelectionService.getByOfficeId(officeId));
    }

    @GetMapping("/office/{officeId}/type/{itemType}")
    public Result<List<OfficeSelection>> getByOfficeIdAndType(
            @PathVariable Long officeId, @PathVariable String itemType) {
        return Result.ok(officeSelectionService.getByOfficeIdAndType(officeId, itemType));
    }

    @PostMapping
    public Result<OfficeSelection> addSelection(@RequestBody Map<String, Object> body) {
        Long officeId = Long.valueOf(body.get("officeId").toString());
        String itemType = (String) body.get("itemType");
        Long itemId = Long.valueOf(body.get("itemId").toString());
        return Result.ok(officeSelectionService.addSelection(officeId, itemType, itemId));
    }

    @DeleteMapping("/office/{officeId}/type/{itemType}/item/{itemId}")
    public Result<Void> removeSelection(
            @PathVariable Long officeId, @PathVariable String itemType, @PathVariable Long itemId) {
        officeSelectionService.removeSelection(officeId, itemType, itemId);
        return Result.ok();
    }

    @PutMapping("/office/{officeId}/type/{itemType}/batch")
    public Result<Void> batchUpdate(
            @PathVariable Long officeId, @PathVariable String itemType,
            @RequestBody Map<String, List<Long>> body) {
        List<Long> itemIds = body.get("itemIds");
        officeSelectionService.batchUpdate(officeId, itemType, itemIds);
        return Result.ok();
    }

    @PutMapping("/{id}/recommend")
    public Result<OfficeSelection> toggleRecommend(@PathVariable Long id) {
        return Result.ok(officeSelectionService.toggleRecommend(id));
    }
}
