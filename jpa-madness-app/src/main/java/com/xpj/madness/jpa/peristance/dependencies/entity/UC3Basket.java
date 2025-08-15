package com.xpj.madness.jpa.peristance.dependencies.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
//@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "uc3_basket")
public class UC3Basket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

}
