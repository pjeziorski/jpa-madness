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
@Table(name = "uc08_joined_column_beth")
@DiscriminatorValue("Beth")
public class UC08JoinedColumnBethWithEagerChildren extends UC08JoinedColumnParent {

    @Column(name = "beth_surname", nullable = false)
    private String bethSurname;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<UC08JoinedColumnBethChild> bethChildren;

}
