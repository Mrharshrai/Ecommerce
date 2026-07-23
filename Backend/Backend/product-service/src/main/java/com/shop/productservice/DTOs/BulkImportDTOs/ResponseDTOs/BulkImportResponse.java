package com.shop.productservice.DTOs.BulkImportDTOs.ResponseDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkImportResponse {

    private int totalRows;

    private int productsCreated;
    private int productsUpdated;

    private int variantsCreated;
    private int variantsSkipped;

    private int sizesCreated;
    private int sizesSkipped;

    private int failedRows;

    private List<ImportError> errors;
}
