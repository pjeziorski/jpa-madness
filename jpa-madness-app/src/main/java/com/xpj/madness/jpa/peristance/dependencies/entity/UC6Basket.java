package com.xpj.madness.jpa.peristance.dependencies.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "uc6_basket")
public class UC6Basket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;

    @OneToMany(mappedBy = "basket", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<UC6BasketItem> items;
}
