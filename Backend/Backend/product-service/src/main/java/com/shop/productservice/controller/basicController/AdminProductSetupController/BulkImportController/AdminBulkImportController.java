package com.shop.productservice.controller.basicController.AdminProductSetupController.BulkImportController;

import com.shop.productservice.DTOs.BulkImportDTOs.RequestDTOs.BulkImportRequest;
import com.shop.productservice.DTOs.BulkImportDTOs.ResponseDTOs.BulkImportResponse;
import com.shop.productservice.service.bulkImport.BulkImportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/adminProduct/bulk-import")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminBulkImportController {

    private final BulkImportService bulkImportService;

    @PostMapping("/import")
    public ResponseEntity<BulkImportResponse> bulkImport(
            @Valid @RequestBody BulkImportRequest request,
            HttpServletRequest servletRequest) {

        BulkImportResponse response = bulkImportService.importRows(request, servletRequest);
        return ResponseEntity.ok(response);
    }
}
