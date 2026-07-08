package com.lorex.cabinet.repository;

import com.lorex.cabinet.model.SubscriptionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface KeyRepository extends JpaRepository<SubscriptionKey, Long> {
    Optional<SubscriptionKey> findByKeyCode(String keyCode);
}