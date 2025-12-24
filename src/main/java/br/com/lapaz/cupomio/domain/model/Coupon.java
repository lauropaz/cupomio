package br.com.lapaz.cupomio.domain.model;

import br.com.lapaz.cupomio.domain.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

@Entity
@Table(name = "COUPON")
public class Coupon {

    private static final Pattern CODE_SANITIZER = Pattern.compile("[^A-Za-z0-9]");
    private static final int CODE_LENGTH = 6;
    private static final BigDecimal MIN_DISCOUNT = BigDecimal.valueOf(0.5);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CODE", nullable = false, length = CODE_LENGTH, unique = true)
    private String code;

    @Column(name = "DESCRIPTION", nullable = false, length = 255)
    private String description;

    @Column(name = "DISCOUNT_VALUE", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "EXPIRATION_DATE", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "PUBLISHED", nullable = false)
    private boolean published;

    @Column(name = "DELETED", nullable = false)
    private boolean deleted;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    protected Coupon() {
    }

    private Coupon(String code, String description, BigDecimal discountValue, LocalDate expirationDate, boolean published, Clock clock) {
        this.code = code;
        this.description = description;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.published = published;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = this.createdAt;
        this.deleted = false;
    }

    public static Coupon create(String rawCode, String description, BigDecimal discountValue, LocalDate expirationDate, boolean published, Clock clock) {
        Objects.requireNonNull(clock, "Clock is required");
        String normalizedCode = normalizeCode(rawCode);
        validateRequired(description, discountValue, expirationDate);
        validateDiscount(discountValue);
        validateExpiration(expirationDate, clock);
        return new Coupon(normalizedCode, description, discountValue, expirationDate, published, clock);
    }

    public void markAsDeleted(Clock clock) {
        Objects.requireNonNull(clock, "Clock is required");
        if (deleted) {
            throw new BusinessException("Coupon already deleted");
        }
        this.deleted = true;
        this.deletedAt = LocalDateTime.now(clock);
        this.updatedAt = this.deletedAt;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public boolean isPublished() {
        return published;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private static void validateRequired(String description, BigDecimal discountValue, LocalDate expirationDate) {
        if (description == null || description.isBlank()) {
            throw new BusinessException("Description is required");
        }
        if (discountValue == null) {
            throw new BusinessException("Discount value is required");
        }
        if (expirationDate == null) {
            throw new BusinessException("Expiration date is required");
        }
    }

    private static void validateDiscount(BigDecimal discountValue) {
        if (discountValue.compareTo(MIN_DISCOUNT) < 0) {
            throw new BusinessException("Discount value must be at least " + MIN_DISCOUNT);
        }
    }

    private static void validateExpiration(LocalDate expirationDate, Clock clock) {
        LocalDate today = LocalDate.now(clock);
        if (expirationDate.isBefore(today)) {
            throw new BusinessException("Expiration date cannot be in the past");
        }
    }

    private static String normalizeCode(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new BusinessException("Code is required");
        }
        String sanitized = CODE_SANITIZER.matcher(rawCode).replaceAll("");
        if (sanitized.length() != CODE_LENGTH) {
            throw new BusinessException("Code must have exactly " + CODE_LENGTH + " alphanumeric characters");
        }
        return sanitized.toUpperCase();
    }
}
