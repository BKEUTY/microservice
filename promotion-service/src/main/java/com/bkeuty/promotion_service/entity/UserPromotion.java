package com.bkeuty.promotion_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@DiscriminatorValue("UserPromotion")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPromotion extends Promotion {
    @ElementCollection
    @CollectionTable(
            name = "promotion_birthday_month",
            joinColumns = @JoinColumn(name = "promotion_id")
    )
    @Column(name = "birthday_month")
    private Set<Integer> birthdayMonth;

    @ElementCollection
    @CollectionTable(
            name = "promotion_user_ids",
            joinColumns = @JoinColumn(name = "promotion_id")
    )
    @Column(name = "user_id")
    private Set<String> userIds;
}
