package com.xpj.madness.jpa.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class BasketItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

}
