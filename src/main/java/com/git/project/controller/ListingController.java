package com.git.project.controller;

import com.git.project.model.Listing;
import com.git.project.repository.ListingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingRepository listingRepository;

    public ListingController(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    // Get all listings
    @GetMapping
    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    // Create a new listing
    @PostMapping
    public Listing createListing(@RequestBody Listing listing) {
        return listingRepository.save(listing);
    }

    // Edit an existing listing
    @PutMapping("/{id}")
    public ResponseEntity<Listing> updateListing(@PathVariable Long id, @RequestBody Listing updatedListing) {
        return listingRepository.findById(id)
                .map(listing -> {
                    listing.setTitle(updatedListing.getTitle());
                    listing.setDescription(updatedListing.getDescription());
                    listing.setPrice(updatedListing.getPrice());
                    return ResponseEntity.ok(listingRepository.save(listing));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}