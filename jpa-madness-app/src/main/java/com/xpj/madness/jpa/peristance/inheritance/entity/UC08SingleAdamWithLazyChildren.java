package com.xpj.madness.jpa.peristance.inheritance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@DiscriminatorValue("Adam")
public class UC08SingleAdamWithLazyChildren extends UC08SingleParent {

    @Column(name = "adam_surname", nullable = false)
    private String adamSurname;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UC08SingleAdamChild> adamsChildren;

}
