package br.com.lapaz.cupomio.domain.model;

import br.com.lapaz.cupomio.domain.exception.BusinessException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CouponTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void shouldCreateCouponRemovingSpecialCharactersAndUppercasingCode() {
        Coupon coupon = Coupon.create("ab-cd!12", "desc", BigDecimal.valueOf(10), LocalDate.now(FIXED_CLOCK).plusDays(1), true, FIXED_CLOCK);

        assertEquals("ABCD12", coupon.getCode());
        assertTrue(coupon.isPublished());
        assertNotNull(coupon.getCreatedAt());
        assertNotNull(coupon.getUpdatedAt());
    }

    @Test
    void shouldRejectExpirationDateInPast() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                Coupon.create("ABCDEF", "desc", BigDecimal.ONE, LocalDate.now(FIXED_CLOCK).minusDays(1), false, FIXED_CLOCK)
        );

        assertEquals("Expiration date cannot be in the past", ex.getMessage());
    }

    @Test
    void shouldRejectDiscountBelowMinimum() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                Coupon.create("ABCDEF", "desc", BigDecimal.valueOf(0.4), LocalDate.now(FIXED_CLOCK).plusDays(1), false, FIXED_CLOCK)
        );

        assertEquals("Discount value must be at least 0.5", ex.getMessage());
    }

    @Test
    void shouldRejectCodeWithInvalidLengthAfterSanitization() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                Coupon.create("ab!12", "desc", BigDecimal.valueOf(1), LocalDate.now(FIXED_CLOCK).plusDays(1), false, FIXED_CLOCK)
        );

        assertEquals("Code must have exactly 6 alphanumeric characters", ex.getMessage());
    }

    @Test
    void shouldMarkAsDeletedAndUpdateTimestamps() {
        Coupon coupon = Coupon.create("ABCDEF", "desc", BigDecimal.ONE, LocalDate.now(FIXED_CLOCK).plusDays(1), false, FIXED_CLOCK);
        Clock deletionClock = Clock.fixed(Instant.parse("2024-01-02T10:00:00Z"), ZoneOffset.UTC);

        coupon.markAsDeleted(deletionClock);

        assertTrue(coupon.isDeleted());
        assertEquals(LocalDateTime.now(deletionClock), coupon.getDeletedAt());
        assertEquals(coupon.getDeletedAt(), coupon.getUpdatedAt());
    }

    @Test
    void shouldNotAllowDoubleDeletion() {
        Coupon coupon = Coupon.create("ABCDEF", "desc", BigDecimal.ONE, LocalDate.now(FIXED_CLOCK).plusDays(1), false, FIXED_CLOCK);
        coupon.markAsDeleted(FIXED_CLOCK);

        BusinessException ex = assertThrows(BusinessException.class, () -> coupon.markAsDeleted(FIXED_CLOCK));

        assertEquals("Coupon already deleted", ex.getMessage());
    }
}
