package br.com.lapaz.cupomio.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CouponResponse(
        Long id,
        String code,
        String description,
        BigDecimal discountValue,
        LocalDate expirationDate,
        boolean published,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
}
