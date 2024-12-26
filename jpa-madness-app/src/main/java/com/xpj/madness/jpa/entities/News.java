package com.xpj.madness.jpa.entities;

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
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.LAZY) // cascade in order to save tags if they do not exist
    @JoinColumn(name = "news_id")
    private List<NewsComment> comments;

}
