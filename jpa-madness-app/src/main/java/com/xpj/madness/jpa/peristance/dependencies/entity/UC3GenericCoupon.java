package com.xpj.madness.jpa.peristance.dependencies.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "uc3_generic_coupon")
public class UC3GenericCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * In contrast to UserCoupon the column is intentionally made nullable
     * This situation may occur when using default settings and auto ddl creation.
     */
    @Column(name = "user_id", nullable = true)
    private String userId;

    @Column(nullable = false)
    private String code;
}
