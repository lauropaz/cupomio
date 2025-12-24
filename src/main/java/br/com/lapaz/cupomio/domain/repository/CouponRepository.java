package br.com.lapaz.cupomio.domain.repository;

import br.com.lapaz.cupomio.domain.model.Coupon;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByIdAndDeletedFalse(Long id);

    List<Coupon> findAllByDeletedFalseOrderByCreatedAtDesc();
}
