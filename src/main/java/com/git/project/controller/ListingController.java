package com.git.project.controller;

import com.git.project.model.Listing;
import com.git.project.model.User;
import com.git.project.repository.ListingRepository;
import com.git.project.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/listings")
public class ListingController {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ListingController(ListingRepository listingRepository, UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }

    @GetMapping
    public String getListingsPage(Model model, Principal principal) {
        User user = getAuthenticatedUser(principal);
        List<Listing> listings = listingRepository.findAll();
        model.addAttribute("listings", listings);
        model.addAttribute("newListing", new Listing());
        model.addAttribute("currentUser", user);
        return "listings";
    }
    

    // Create a new listing (requires the user's ID)
    @PostMapping("/create")
    public String createListing(@ModelAttribute("newListing") Listing newListing, Principal principal) {
        User user = getAuthenticatedUser(principal);
        newListing.setUser(user);
        listingRepository.save(newListing);
        return "redirect:/listings";
    }

    // Edit an existing listing (Owner OR Admin can edit)
    @PostMapping("/update/{id}")
    public String updateListing(
            @PathVariable Long id,
            @ModelAttribute("listing") Listing updatedListing,
            Principal principal) {

        User user = getAuthenticatedUser(principal);

        Optional<Listing> listingOpt = listingRepository.findById(id);
        if (listingOpt.isPresent()) {
            Listing listing = listingOpt.get();

            boolean isOwner = listing.getUser() != null && listing.getUser().getId().equals(user.getId());
            boolean isAdmin = "ADMIN".equals(user.getRole());

            if (isOwner || isAdmin) {
                listing.setTitle(updatedListing.getTitle());
                listing.setDescription(updatedListing.getDescription());
                listing.setPrice(updatedListing.getPrice());
                listingRepository.save(listing);
            }
        }
        return "redirect:/listings";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,Principal principal) {
        User user = getAuthenticatedUser(principal);
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid listing Id:" + id));

        // Safe checks: handle cases where user might be null
        boolean isOwner = listing.getUser() != null && listing.getUser().getId().equals(user.getId());
        boolean isAdmin = "ADMIN".equals(user.getRole());

        if (!isOwner && !isAdmin) {
            return "redirect:/listings";
        }

        model.addAttribute("listing", listing);
        return "edit-listings";
    }

    // Delete a listing (Owner OR Admin can delete)
    @PostMapping("/delete/{id}")
    public String deleteListing(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedUser(principal);
        
        Optional<Listing> listingOpt = listingRepository.findById(id);
        if (listingOpt.isPresent()) {
            Listing listing = listingOpt.get();

            boolean isOwner = listing.getUser() != null && listing.getUser().getId().equals(user.getId());
            boolean isAdmin = "ADMIN".equals(user.getRole());

            if (isOwner || isAdmin) {
                listingRepository.delete(listing);
            }
        }
        return "redirect:/listings";
    }

    @GetMapping("/profile")
    public String getUserProfile(Model model, Principal principal) {
        User user = getAuthenticatedUser(principal);

        // Fetch only listings belonging to this user
        List<Listing> userListings = listingRepository.findByUserId(user.getId());

        model.addAttribute("currentUser", user);
        model.addAttribute("listings", userListings);
        return "profile";
    }
}