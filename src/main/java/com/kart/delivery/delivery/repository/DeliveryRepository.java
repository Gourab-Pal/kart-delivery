package com.kart.delivery.delivery.repository;

import com.kart.delivery.delivery.entity.DeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<DeliveryEntity, UUID> {

    Optional<DeliveryEntity> findByOrderId(UUID orderId);
    boolean existsByOrderId(UUID orderId);
}
