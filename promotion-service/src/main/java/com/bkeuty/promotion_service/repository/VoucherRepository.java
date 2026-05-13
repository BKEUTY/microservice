package com.bkeuty.promotion_service.repository;

import com.bkeuty.promotion_service.entity.VoucherPromotion;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoucherRepository extends JpaRepository<VoucherPromotion, Integer> {
}
