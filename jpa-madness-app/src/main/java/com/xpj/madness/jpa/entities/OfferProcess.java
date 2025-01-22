package com.xpj.madness.jpa.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class OfferProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private OffsetDateTime creationTime;

    @Enumerated(EnumType.STRING)
    private OfferProcessStatus status;

}
