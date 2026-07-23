package com.shop.productservice.service.bulkImport;

import com.shop.productservice.DTOs.BulkImportDTOs.RequestDTOs.BulkImportRequest;
import com.shop.productservice.DTOs.BulkImportDTOs.ResponseDTOs.BulkImportResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface BulkImportService {

    BulkImportResponse importRows(BulkImportRequest request, HttpServletRequest servletRequest);
}
