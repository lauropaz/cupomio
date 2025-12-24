package br.com.lapaz.cupomio.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CouponRequest(
        @NotBlank String code,
        @NotBlank String description,
        @NotNull @DecimalMin(value = "0.5") BigDecimal discountValue,
        @NotNull @FutureOrPresent LocalDate expirationDate,
        Boolean published
) {
}
