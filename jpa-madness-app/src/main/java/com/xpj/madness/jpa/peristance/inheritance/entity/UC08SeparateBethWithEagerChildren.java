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
@Table(name = "uc08_separate_beth")
public class UC08SeparateBethWithEagerChildren extends UC08SeparateParent {

    @Column(name = "beth_surname", nullable = false)
    private String bethSurname;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<UC08SeparateBethChild> bethChildren;

}
