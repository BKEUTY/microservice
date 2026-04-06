package com.bkeuty.review_service.repository;

import com.bkeuty.review_service.entity.AdminReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminReplyRepository extends JpaRepository<AdminReply, Long> {
}
