package com.undoschool.coursesearch;

import com.undoschool.coursesearch.document.CourseDocument;
import com.undoschool.coursesearch.service.CourseSearchService;
import com.undoschool.coursesearch.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CourseSearchController {

    @Autowired
    private CourseSearchService courseSearchService;

    @Autowired
    private SuggestionService suggestionService;

    @GetMapping("/search")
    public List<CourseDocument> searchCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return courseSearchService.searchCourses(q, category, type, minAge, maxAge, minPrice, maxPrice, startDate, sort, page, size);
    }

    @GetMapping("/suggest")
    public List<String> getSuggestions(@RequestParam String q) {
        return suggestionService.getSuggestions(q);
    }
}
