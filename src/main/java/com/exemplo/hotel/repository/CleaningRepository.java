package com.example.hotel.repository;

import com.example.hotel.model.CleaningRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for CRUD operations on CleaningRecord.
 * Extends JpaRepository for automatic implementations of save/find/delete.
 */
@Repository
public interface CleaningRepository extends JpaRepository<CleaningRecord, Long> {
}