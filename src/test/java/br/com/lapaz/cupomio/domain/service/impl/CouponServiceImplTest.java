package br.com.lapaz.cupomio.domain.service.impl;

import br.com.lapaz.cupomio.api.dto.CouponRequest;
import br.com.lapaz.cupomio.api.dto.CouponResponse;
import br.com.lapaz.cupomio.domain.exception.BusinessException;
import br.com.lapaz.cupomio.domain.exception.NotFoundException;
import br.com.lapaz.cupomio.domain.model.Coupon;
import br.com.lapaz.cupomio.domain.repository.CouponRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private CouponRepository couponRepository;

    @BeforeEach
    void setUp() {
        couponService = new CouponServiceImpl(couponRepository, CLOCK);
    }

    private CouponServiceImpl couponService;

    @Test
    void shouldCreateCouponAndReturnResponse() {
        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
            Coupon coupon = invocation.getArgument(0);
            ReflectionTestUtils.setField(coupon, "id", 1L);
            return coupon;
        });
        CouponRequest request = new CouponRequest("ab-cd!12", "Description", BigDecimal.valueOf(15), LocalDate.now(CLOCK).plusDays(2), true);

        CouponResponse response = couponService.create(request);

        assertEquals(1L, response.id());
        assertEquals("ABCD12", response.code());
        assertEquals("Description", response.description());
        assertNotNull(response.createdAt());
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void shouldSoftDeleteCoupon() {
        Coupon coupon = Coupon.create("ABCDEF", "Desc", BigDecimal.ONE, LocalDate.now(CLOCK).plusDays(1), false, CLOCK);
        ReflectionTestUtils.setField(coupon, "id", 5L);
        when(couponRepository.findById(5L)).thenReturn(Optional.of(coupon));
        when(couponRepository.save(coupon)).thenReturn(coupon);

        couponService.delete(5L);

        assertEquals(true, coupon.isDeleted());
        assertNotNull(coupon.getDeletedAt());
        verify(couponRepository, times(1)).save(coupon);
    }

    @Test
    void shouldFailDeletingNonExistingCoupon() {
        when(couponRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> couponService.delete(99L));
        verify(couponRepository, times(1)).findById(99L);
    }

    @Test
    void shouldFailDeletingAlreadyDeletedCoupon() {
        Coupon coupon = Coupon.create("ABCDEF", "Desc", BigDecimal.ONE, LocalDate.now(CLOCK).plusDays(1), false, CLOCK);
        coupon.markAsDeleted(CLOCK);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        assertThrows(BusinessException.class, () -> couponService.delete(1L));
        verify(couponRepository, Mockito.never()).save(any(Coupon.class));
    }
}
