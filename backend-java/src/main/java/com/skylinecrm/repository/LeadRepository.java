package com.skylinecrm.repository;

import com.skylinecrm.model.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeadRepository extends JpaRepository<LeadEntity, String> {
    Optional<LeadEntity> findByMobile(String mobile);
}
