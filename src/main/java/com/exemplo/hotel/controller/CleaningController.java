package com.example.hotel.controller;

import com.example.hotel.model.CleaningRecord;
import com.example.hotel.service.CleaningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for API endpoints called by frontend JS.
 * Handles CRUD for records.
 */
@RestController
@RequestMapping("/api/cleaning")
public class CleaningController {

    @Autowired
    private CleaningService service;

    // Create new record
    @PostMapping("/create")
    public CleaningRecord create(@RequestParam int numMaids,
                                 @RequestParam List<String> maidNames,
                                 @RequestParam String excludedRooms) {
        return service.createRecord(numMaids, maidNames, excludedRooms);
    }

    // Get all records
    @GetMapping("/all")
    public List<CleaningRecord> getAll() {
        return service.getAllRecords();
    }

    // Get one record
    @GetMapping("/{id}")
    public ResponseEntity<CleaningRecord> getById(@PathVariable Long id) {
        CleaningRecord record = service.getRecordById(id);
        return record != null ? ResponseEntity.ok(record) : ResponseEntity.notFound().build();
    }

    // Update record
    @PutMapping("/{id}")
    public ResponseEntity<CleaningRecord> update(@PathVariable Long id,
                                                 @RequestParam int numMaids,
                                                 @RequestParam List<String> maidNames,
                                                 @RequestParam String excludedRooms) {
        CleaningRecord updated = service.updateRecord(id, numMaids, maidNames, excludedRooms);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // Delete record
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteRecord(id);
        return ResponseEntity.ok().build();
    }
}