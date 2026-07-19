package org.shashanka.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyRecordModel {
    @Id
    private String idempotencyKey;
    private Long paymentId;
    private LocalDateTime createdAt;
}
