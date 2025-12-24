package br.com.lapaz.cupomio.domain.service;

import br.com.lapaz.cupomio.api.dto.CouponRequest;
import br.com.lapaz.cupomio.api.dto.CouponResponse;

public interface CouponService {

    CouponResponse create(CouponRequest request);

    void delete(Long id);
}
