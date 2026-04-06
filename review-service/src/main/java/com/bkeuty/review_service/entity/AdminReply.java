package com.bkeuty.review_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_replies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;

    private String adminId;

    @CreationTimestamp
    private LocalDateTime repliedAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
