package com.bkeuty.promotion_service.repository;

import com.bkeuty.promotion_service.entity.UserPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserPromotionRepository extends JpaRepository<UserPromotion, Integer> {
    @Query("SELECT DISTINCT p FROM UserPromotion p LEFT JOIN p.membershipLevels ml LEFT JOIN p.userIds ui " +
           "WHERE p.status = com.bkeuty.promotion_service.enums.PromotionStatus.STARTING AND p.startAt <= :now AND p.endAt >= :now " +
           "AND (ml = :membershipLevel OR ui = :userId OR (ml IS NULL AND ui IS NULL))")
    List<UserPromotion> findApplicablePromotions(@Param("membershipLevel") Integer membershipLevel,
                                                 @Param("userId") String userId,
                                                 @Param("now") LocalDateTime now);
}
