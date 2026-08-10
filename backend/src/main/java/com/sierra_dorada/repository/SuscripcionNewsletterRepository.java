package com.sierra_dorada.repository;

import com.sierra_dorada.model.SuscripcionNewsletter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuscripcionNewsletterRepository extends JpaRepository<SuscripcionNewsletter, Integer> {
    boolean existsByEmailIgnoreCase(String email);
}

