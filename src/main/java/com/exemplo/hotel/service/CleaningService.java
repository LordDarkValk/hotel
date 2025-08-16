package com.example.hotel.service;

import com.example.hotel.model.CleaningRecord;
import com.example.hotel.repository.CleaningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Service class handling business logic: generating rooms, excluding, assigning to maids.
 * All rooms are hardcoded here as per requirements.
 */
@Service
public class CleaningService {

    @Autowired
    private CleaningRepository repository;

    // Hardcoded all rooms as lists for each floor
    private static final List<Integer> ALL_ROOMS = new ArrayList<>();
    static {
        // 1st floor: 101-122
        ALL_ROOMS.addAll(IntStream.rangeClosed(101, 122).boxed().toList());
        // 2nd: 201-219
        ALL_ROOMS.addAll(IntStream.rangeClosed(201, 219).boxed().toList());
        // 3rd: 301-314
        ALL_ROOMS.addAll(IntStream.rangeClosed(301, 314).boxed().toList());
        // 4th: 401-416
        ALL_ROOMS.addAll(IntStream.rangeClosed(401, 416).boxed().toList());
        // 5th: 501-512 and 514-516
        ALL_ROOMS.addAll(IntStream.rangeClosed(501, 512).boxed().toList());
        ALL_ROOMS.addAll(IntStream.rangeClosed(514, 516).boxed().toList());
    }

    /**
     * Creates and saves a new record: calculates rooms to clean, assigns to maids equally.
     * @param numMaids Number of maids.
     * @param maidNames List of maid names.
     * @param excludedRooms Comma-separated excluded rooms.
     * @return Saved record.
     */
    public CleaningRecord createRecord(int numMaids, List<String> maidNames, String excludedRooms) {
        // Parse excluded rooms
        List<Integer> excluded = excludedRooms.isEmpty() ? new ArrayList<>() :
                Arrays.stream(excludedRooms.split(","))
                      .map(String::trim)
                      .map(Integer::parseInt)
                      .collect(Collectors.toList());

        // Rooms to clean: all minus excluded
        List<Integer> roomsToClean = ALL_ROOMS.stream()
                .filter(room -> !excluded.contains(room))
                .sorted() // Sort for nice display
                .collect(Collectors.toList());

        // Assign rooms equally
        List<String> assignments = assignRoomsToMaids(maidNames, roomsToClean);

        // Create and save record
        CleaningRecord record = new CleaningRecord();
        record.setRegistrationTime(LocalDateTime.now());
        record.setMaids(maidNames);
        record.setRoomsToClean(roomsToClean);
        record.setAssignments(assignments);
        return repository.save(record);
    }

    /**
     * Assigns rooms to maids equally (round-robin for fairness).
     */
    private List<String> assignRoomsToMaids(List<String> maids, List<Integer> rooms) {
        List<String> assignments = new ArrayList<>();
        int numMaids = maids.size();
        List<List<Integer>> maidRooms = new ArrayList<>();
        for (int i = 0; i < numMaids; i++) {
            maidRooms.add(new ArrayList<>());
        }

        // Distribute rooms round-robin
        for (int i = 0; i < rooms.size(); i++) {
            maidRooms.get(i % numMaids).add(rooms.get(i));
        }

        // Format as "MaidName: room1,room2,..."
        for (int i = 0; i < numMaids; i++) {
            String assignment = maids.get(i) + ": " + maidRooms.get(i).stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            assignments.add(assignment);
        }
        return assignments;
    }

    // Get all records
    public List<CleaningRecord> getAllRecords() {
        return repository.findAll();
    }

    // Get by ID
    public CleaningRecord getRecordById(Long id) {
        return repository.findById(id).orElse(null);
    }

    // Update record (similar to create, but overwrite)
    public CleaningRecord updateRecord(Long id, int numMaids, List<String> maidNames, String excludedRooms) {
        CleaningRecord record = getRecordById(id);
        if (record != null) {
            // Re-calculate based on new inputs
            List<Integer> excluded = excludedRooms.isEmpty() ? new ArrayList<>() :
                    Arrays.stream(excludedRooms.split(",")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
            List<Integer> roomsToClean = ALL_ROOMS.stream().filter(room -> !excluded.contains(room)).sorted().collect(Collectors.toList());
            List<String> assignments = assignRoomsToMaids(maidNames, roomsToClean);

            record.setMaids(maidNames);
            record.setRoomsToClean(roomsToClean);
            record.setAssignments(assignments);
            return repository.save(record);
        }
        return null;
    }

    // Delete by ID
    public void deleteRecord(Long id) {
        repository.deleteById(id);
    }
}