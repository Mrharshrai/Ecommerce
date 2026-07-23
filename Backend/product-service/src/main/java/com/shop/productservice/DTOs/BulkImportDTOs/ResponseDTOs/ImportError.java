package com.shop.productservice.DTOs.BulkImportDTOs.ResponseDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportError {

    private int rowIndex;

    private String entityType;

    private String identifier;

    private String message;
}
