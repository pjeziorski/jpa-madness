package com.xpj.madness.jpa.peristance.inheritance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@DiscriminatorValue("Adam")
public class UC08SingleAdamWithLazyChildren extends UC08SingleParent {

    @Column(nullable = false)
    private String adamSurname;



}
