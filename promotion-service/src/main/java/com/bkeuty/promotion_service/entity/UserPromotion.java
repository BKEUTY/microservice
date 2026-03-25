package com.bkeuty.promotion_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@DiscriminatorValue("UserPromotion")
@Entity
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
            name = "promotion_membership_level",
            joinColumns = @JoinColumn(name = "promotion_id")
    )
    @Column(name = "membership_level")
    private Set<Integer> membershipLevel;
}
