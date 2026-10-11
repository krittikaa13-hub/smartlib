package com.smartlib.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

/**
 * Controller serving the SmartLib Homepage and Stage 1 placeholder routes.
 * Keeps business logic isolated and delegates presentation to Thymeleaf templates.
 */
@Controller
public class HomeController {

    /**
     * Serves the main SmartLib portal homepage.
     *
     * @param query Optional search term passed from the hero search input
     * @param model Thymeleaf view model
     * @return Template name "home"
     */
    @GetMapping("/")
    public String home(@RequestParam(name = "query", required = false) String query, Model model) {
        model.addAttribute("pageTitle", "SmartLib - Unified University Library Hub");
        model.addAttribute("activeNav", "home");
        model.addAttribute("searchQuery", query != null ? query.trim() : "");

        // Empty announcements list demonstrating clear empty-state design
        List<String> announcements = Collections.emptyList();
        model.addAttribute("announcements", announcements);

        // Stage 1 status metadata
        model.addAttribute("stageName", "Stage 1: Foundation & Homepage");
        model.addAttribute("dbStatus", "Configured (MySQL smartlib_db)");

        return "home";
    }


    /**
     * Placeholder route for Syllabus Finder (Stage 4).
     */
    @GetMapping("/syllabus-finder")
    public String syllabusFinderPlaceholder(Model model) {
        model.addAttribute("featureName", "Syllabus Finder");
        model.addAttribute("featureStage", "Stage 4 (Academic Course Mapping)");
        model.addAttribute("featureDescription", "Match university course syllabi topics with required and recommended reading lists.");
        return "placeholder";
    }

    /**
     * Placeholder route for Shelf Finder (Stage 5).
     */
    @GetMapping("/shelf-finder")
    public String shelfFinderPlaceholder(Model model) {
        model.addAttribute("featureName", "Find on Shelf");
        model.addAttribute("featureStage", "Stage 5 (Physical Stacks & Wayfinding)");
        model.addAttribute("featureDescription", "Locate books inside university library wings, floor plans, and call-number ranges.");
        return "placeholder";
    }

    /**
     * Placeholder route for My Library (Stage 6).
     */
    @GetMapping("/my-library")
    public String myLibraryPlaceholder(Model model) {
        model.addAttribute("featureName", "My Library & Deadline Notifications");
        model.addAttribute("featureStage", "Stage 6 (Circulation & Notifications)");
        model.addAttribute("featureDescription", "Track checked-out books, active renewals, and impending due-date return alerts.");
        return "placeholder";
    }

    /**
     * Placeholder route for Library Quest (Stage 7).
     */
    @GetMapping("/library-quest")
    public String libraryQuestPlaceholder(Model model) {
        model.addAttribute("featureName", "Library Quest & Learning Rewards");
        model.addAttribute("featureStage", "Stage 7 (Gamified Study Missions)");
        model.addAttribute("featureDescription", "Earn research badges, complete citation quests, and unlock library privileges.");
        return "placeholder";
    }

    /**
     * Placeholder route for Digital Resources.
     */
    @GetMapping("/digital-resources")
    public String digitalResourcesPlaceholder(Model model) {
        model.addAttribute("featureName", "Digital Resources Portal");
        model.addAttribute("featureStage", "Stage 1 (External Verified Index)");
        model.addAttribute("featureDescription", "Access authorized academic repositories, e-journals, and open-access university archives.");
        return "placeholder";
    }

}
