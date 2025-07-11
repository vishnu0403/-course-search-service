package com.undoschool.coursesearch.service;

import com.undoschool.coursesearch.document.CourseDocument;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseSearchService {

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    public List<CourseDocument> searchCourses(String q, String category, String type,
                                              Integer minAge, Integer maxAge, Double minPrice,
                                              Double maxPrice, Instant startDate, String sort, int page, int size) {

        Criteria criteria = new Criteria();

        if (q != null && !q.isEmpty()) {
            criteria = criteria.and(new Criteria("title").matches(q)).or(new Criteria("description").matches(q));
        }

        if (category != null && !category.isEmpty()) {
            criteria = criteria.and(new Criteria("category").is(category));
        }

        if (type != null && !type.isEmpty()) {
            criteria = criteria.and(new Criteria("type").is(type));
        }

        if (minAge != null && maxAge != null) {
            criteria = criteria.and(new Criteria("minAge").lessThanEqual(maxAge));
            criteria = criteria.and(new Criteria("maxAge").greaterThanEqual(minAge));
        }

        if (minPrice != null && maxPrice != null) {
            criteria = criteria.and(new Criteria("price").greaterThanEqual(minPrice).lessThanEqual(maxPrice));
        }

        if (startDate != null) {
            criteria = criteria.and(new Criteria("nextSessionDate").greaterThanEqual(startDate));
        }

        Sort sortField;
        if ("price_asc".equalsIgnoreCase(sort)) {
            sortField = Sort.by(Sort.Order.asc("price"));
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            sortField = Sort.by(Sort.Order.desc("price"));
        } else {
            sortField = Sort.by(Sort.Order.asc("nextSessionDate"));
        }

        Query query = new CriteriaQuery(criteria);
        query.setPageable(PageRequest.of(page, size, sortField));

        SearchHits<CourseDocument> searchHits = elasticsearchTemplate.search(query, CourseDocument.class);

        return searchHits.getSearchHits()
                .stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadSampleData() {
        try {
            System.out.println("🟡 Loading sample course data...");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // ✅ Fix for Instant

            InputStream inputStream = new ClassPathResource("sample-courses.json").getInputStream();
            List<CourseDocument> courses = objectMapper.readValue(inputStream, new TypeReference<List<CourseDocument>>() {});

            System.out.println("✅ Loaded " + courses.size() + " courses.");

            for (CourseDocument course : courses) {
                elasticsearchTemplate.save(course);
            }

            System.out.println("✅ Indexed sample course data into Elasticsearch.");
        } catch (Exception e) {
            System.err.println("❌ Failed to load sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
