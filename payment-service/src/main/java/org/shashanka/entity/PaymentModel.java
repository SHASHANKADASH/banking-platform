package org.shashanka.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long accountId;
    private Double amount;
    private String merchant;
    private String status;
    private LocalDateTime createdAt;
}
