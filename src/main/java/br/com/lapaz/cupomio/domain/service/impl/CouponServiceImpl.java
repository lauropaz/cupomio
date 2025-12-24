package br.com.lapaz.cupomio.domain.service.impl;

import br.com.lapaz.cupomio.api.dto.CouponRequest;
import br.com.lapaz.cupomio.api.dto.CouponResponse;
import br.com.lapaz.cupomio.domain.exception.NotFoundException;
import br.com.lapaz.cupomio.domain.model.Coupon;
import br.com.lapaz.cupomio.domain.repository.CouponRepository;
import br.com.lapaz.cupomio.domain.service.CouponService;
import java.util.List;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final Clock clock;

    public CouponServiceImpl(CouponRepository couponRepository, Clock clock) {
        this.couponRepository = couponRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CouponResponse create(CouponRequest request) {
        Coupon coupon = Coupon.create(
                request.code(),
                request.description(),
                request.discountValue(),
                request.expirationDate(),
                Boolean.TRUE.equals(request.published()),
                clock
        );
        Coupon saved = couponRepository.save(coupon);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Coupon not found"));
        coupon.markAsDeleted(clock);
        couponRepository.save(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse findById(Long id) {
        Coupon coupon = couponRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Coupon not found"));
        return toResponse(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> findAll() {
        return couponRepository.findAllByDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getDiscountValue(),
                coupon.getExpirationDate(),
                coupon.isPublished(),
                coupon.isDeleted(),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt(),
                coupon.getDeletedAt()
        );
    }
}
