package org.shashanka.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.*;

@Entity
@Table(name = "accounts")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountModel {
    @Id
    private Long id;
    private Double balance;

    // For optimistic locking. This is provided by hibernate.
    // Prevents lost updates
    // @Version is annotation given by jakarta
    @Version
    private Long version;
}
