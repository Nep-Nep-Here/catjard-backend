package com.catjard.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VoucherDTO(
        @NotBlank @Size(max = 255) String voucherUrl
) {}
