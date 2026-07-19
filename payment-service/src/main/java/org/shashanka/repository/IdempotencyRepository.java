package org.shashanka.repository;

import org.shashanka.entity.IdempotencyRecordModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecordModel, String> {
}
