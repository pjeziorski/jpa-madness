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
@Table(name = "uc08_joined_parent")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class UC08JoinedParent {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "uc08_joined_parent_sequence_generator"
    )
    @SequenceGenerator(
            name = "uc08_joined_parent_sequence_generator",
            sequenceName = "uc08_seq_joined_parent",
            allocationSize = 30
    )
    private Long id;

    @Column(name = "test_id", nullable = false)
    private String testId;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UC08JoinedCommonLazyChild> commonLazyChildren;

}
