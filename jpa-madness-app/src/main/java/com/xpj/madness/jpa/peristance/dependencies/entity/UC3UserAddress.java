package com.xpj.madness.jpa.peristance.dependencies.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
@Entity
@Table(name = "uc3_user_address")
public class UC3UserAddress {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "uc3_user_address_sequence_generator"
    )
    @SequenceGenerator(
            name = "uc3_user_address_sequence_generator",
            sequenceName = "uc3_seq_user_address",
            allocationSize = 30
    )
    private Long id;

    @Column(nullable = false)
    private String city;

    @ManyToOne
    @JoinColumn(name="user_id")
    private UC3User user;

}
