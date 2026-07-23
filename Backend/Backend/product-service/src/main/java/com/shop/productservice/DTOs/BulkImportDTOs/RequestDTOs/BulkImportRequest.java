package com.shop.productservice.DTOs.BulkImportDTOs.RequestDTOs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class BulkImportRequest {

    @NotEmpty(message = "At least one row is required")
    @Valid
    private List<BulkImportRow> rows;
}
