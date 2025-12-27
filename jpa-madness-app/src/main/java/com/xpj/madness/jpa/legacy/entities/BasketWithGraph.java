package com.xpj.madness.jpa.legacy.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
        name = "allRelations",
        attributeNodes = {
                @NamedAttributeNode("items")
        }
)
public class BasketWithGraph {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;

    @OneToMany(cascade= CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "basket_id")
    private List<BasketWithGraphItem> items;
}
