package com.demo.repositories;

import com.demo.entities.Counter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface CounterRepository extends JpaRepository<Counter, UUID> {
    @Query("SELECT c.value from Counter c WHERE c.name = :name")
    Long findValueByName(String name);

    @Modifying
    @Transactional
    @Query("""
            UPDATE Counter c set c.value = :value
            WHERE c.name = :name
            """)
    void updateValueByName(String name, Long value);

}
