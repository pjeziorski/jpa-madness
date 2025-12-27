package com.xpj.madness.jpa.legacy.entities;

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
@Table
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // intentionally not marking not-null on entity level
    private String title;

    @Column(nullable = false)
    private String content;
}
