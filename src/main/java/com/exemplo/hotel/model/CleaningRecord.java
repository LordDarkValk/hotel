package com.example.hotel.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a cleaning record stored in the database.
 * Uses JPA annotations for mapping to DB table.
 * Fields are lists to store maids, rooms to clean, and assignments.
 */
@Entity
@Data // Lombok: Auto-generates getters/setters/toString/etc.
public class CleaningRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime registrationTime; // Date and time of registration

    @ElementCollection // Stores lists as separate tables for simplicity
    private List<String> maids; // List of maid names

    @ElementCollection
    private List<Integer> roomsToClean; // Rooms that need cleaning (after exclusions)

    @ElementCollection
    private List<String> assignments; // Assignments as strings, e.g., "Maid1: 101,102,..."

    // No-args constructor for JPA
    public CleaningRecord() {}
}