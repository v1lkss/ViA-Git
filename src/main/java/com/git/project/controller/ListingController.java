package com.git.project.controller;

import com.git.project.model.Listing;
import com.git.project.model.User;
import com.git.project.repository.ListingRepository;
import com.git.project.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ListingController(ListingRepository listingRepository, UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    // Get all listings
    @GetMapping
    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    // Create a new listing (requires the user's ID)
    @PostMapping
    public ResponseEntity<?> createListing(@RequestParam Long userId, @RequestBody Listing newListing) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        newListing.setUser(userOpt.get());
        Listing savedListing = listingRepository.save(newListing);
        return ResponseEntity.ok(savedListing);
    }

    // Edit an existing listing (Owner OR Admin can edit)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateListing(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestBody Listing updatedListing) {

        Optional<Listing> listingOpt = listingRepository.findById(id);
        Optional<User> userOpt = userRepository.findById(userId);

        if (listingOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Listing listing = listingOpt.get();
        User user = userOpt.get();

        // Check if user is the owner OR an admin
        boolean isOwner = listing.getUser().getId().equals(user.getId());
        boolean isAdmin = "ADMIN".equals(user.getRole());

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).body("Access denied: You can only edit your own listings unless you are an admin.");
        }

        // Perform update
        listing.setTitle(updatedListing.getTitle());
        listing.setDescription(updatedListing.getDescription());
        listing.setPrice(updatedListing.getPrice());

        return ResponseEntity.ok(listingRepository.save(listing));
    }

    // Delete a listing (Owner OR Admin can delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteListing(@PathVariable Long id, @RequestParam Long userId) {
        Optional<Listing> listingOpt = listingRepository.findById(id);
        Optional<User> userOpt = userRepository.findById(userId);

        if (listingOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Listing listing = listingOpt.get();
        User user = userOpt.get();

        boolean isOwner = listing.getUser().getId().equals(user.getId());
        boolean isAdmin = "ADMIN".equals(user.getRole());

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).body("Access denied: You can only delete your own listings unless you are an admin.");
        }

        listingRepository.delete(listing);
        return ResponseEntity.ok().body("Listing deleted successfully");
    }
}