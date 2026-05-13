package com.bkeuty.promotion_service.repository;

import com.bkeuty.promotion_service.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Integer> {
    Optional<UserVoucher> findByUserIdAndVoucherId(String userId, Integer voucherId);
}
