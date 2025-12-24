package br.com.lapaz.cupomio.api.controller;

import br.com.lapaz.cupomio.api.dto.CouponRequest;
import br.com.lapaz.cupomio.api.dto.CouponResponse;
import br.com.lapaz.cupomio.api.exception.ApiExceptionHandler;
import br.com.lapaz.cupomio.domain.exception.BusinessException;
import br.com.lapaz.cupomio.domain.exception.NotFoundException;
import br.com.lapaz.cupomio.domain.service.CouponService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(controllers = CouponController.class)
@Import(ApiExceptionHandler.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CouponService couponService;

    @Test
    void shouldCreateCoupon() throws Exception {
        CouponRequest request = new CouponRequest("abc-def", "Desc", BigDecimal.valueOf(5), LocalDate.now().plusDays(3), true);
        CouponResponse response = new CouponResponse(1L, "ABCDEF", "Desc", BigDecimal.valueOf(5), request.expirationDate(), true, false, LocalDateTime.now(), LocalDateTime.now(), null);
        Mockito.when(couponService.create(request)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", "http://localhost/api/coupons/1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ABCDEF"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.published").value(true));
    }

    @Test
    void shouldReturnValidationError() throws Exception {
        CouponRequest request = new CouponRequest("", "", BigDecimal.valueOf(0.1), LocalDate.now().minusDays(1), null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("VALIDATION"));
    }

    @Test
    void shouldDeleteCoupon() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/coupons/{id}", 1))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(couponService).delete(1L);
    }

    @Test
    void shouldReturnNotFoundOnDelete() throws Exception {
        Mockito.doThrow(new NotFoundException("not found")).when(couponService).delete(99L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/coupons/{id}", 99))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void shouldReturnBusinessErrorWhenDeletingAlreadyDeleted() throws Exception {
        Mockito.doThrow(new BusinessException("Coupon already deleted")).when(couponService).delete(2L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/coupons/{id}", 2))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("BUSINESS_RULE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Coupon already deleted"));
    }

    @Test
    void shouldGetCouponById() throws Exception {
        CouponResponse response = new CouponResponse(1L, "ABCDEF", "Desc", BigDecimal.valueOf(5), LocalDate.now().plusDays(3), true, false, LocalDateTime.now(), LocalDateTime.now(), null);
        Mockito.when(couponService.findById(1L)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/coupons/{id}", 1))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ABCDEF"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.published").value(true));
    }

    @Test
    void shouldReturnNotFoundWhenGettingById() throws Exception {
        Mockito.when(couponService.findById(99L)).thenThrow(new NotFoundException("not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/coupons/{id}", 99))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void shouldListAllCoupons() throws Exception {
        List<CouponResponse> list = List.of(
                new CouponResponse(1L, "ABCDEF", "Desc1", BigDecimal.valueOf(5), LocalDate.now().plusDays(1), true, false, LocalDateTime.now(), LocalDateTime.now(), null),
                new CouponResponse(2L, "UVWXYZ", "Desc2", BigDecimal.valueOf(10), LocalDate.now().plusDays(2), false, false, LocalDateTime.now(), LocalDateTime.now(), null)
        );
        Mockito.when(couponService.findAll()).thenReturn(list);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/coupons"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].code").value("ABCDEF"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].code").value("UVWXYZ"));
    }
}
