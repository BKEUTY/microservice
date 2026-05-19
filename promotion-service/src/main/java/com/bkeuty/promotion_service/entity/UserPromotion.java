package com.bkeuty.promotion_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.List;
import java.util.Set;

@DiscriminatorValue("UserPromotion")
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserPromotion extends Promotion {
    @ElementCollection
    @CollectionTable(
            name = "promotion_birthday_month",
            joinColumns = @JoinColumn(name = "promotion_id")
    )
    @Column(name = "birthday_month")
    @BatchSize(size = 100)
    private Set<Integer> birthdayMonth;

    @ElementCollection
    @CollectionTable(
            name = "promotion_user_ids",
            joinColumns = @JoinColumn(name = "promotion_id")
    )
    @Column(name = "user_id")
    @BatchSize(size = 100)
    private Set<String> userIds;
}
