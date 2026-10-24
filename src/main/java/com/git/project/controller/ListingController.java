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

    private User getAuthenticatedUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    @GetMapping
    public String getListingsPage(Model model, HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        List<Listing> listings = listingRepository.findAll();
        model.addAttribute("listings", listings);
        model.addAttribute("newListing", new Listing());
        model.addAttribute("currentUser", user);
        return "listings";
    }
    

    // Create a new listing (requires the user's ID)
    @PostMapping("/create")
    public String createListing(HttpSession session, @ModelAttribute("newListing") Listing newListing) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        newListing.setUser(user);
        listingRepository.save(newListing);
        return "redirect:/listings";
    }

    // Edit an existing listing (Owner OR Admin can edit)
    @PostMapping("/update/{id}")
    public String updateListing(
            @PathVariable Long id,
            HttpSession session,
            @ModelAttribute("listing") Listing updatedListing) {

        User user = getAuthenticatedUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Listing> listingOpt = listingRepository.findById(id);
        if (listingOpt.isPresent()) {
            Listing listing = listingOpt.get();

            boolean isOwner = listing.getUser().getId().equals(user.getId());
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

    // Delete a listing (Owner OR Admin can delete)
    @PostMapping("/delete/{id}")
    public String deleteListing(@PathVariable Long id, HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Listing> listingOpt = listingRepository.findById(id);
        if (listingOpt.isPresent()) {
            Listing listing = listingOpt.get();

            boolean isOwner = listing.getUser().getId().equals(user.getId());
            boolean isAdmin = "ADMIN".equals(user.getRole());

            if (isOwner || isAdmin) {
                listingRepository.delete(listing);
            }
        }
        return "redirect:/listings";
    }
}