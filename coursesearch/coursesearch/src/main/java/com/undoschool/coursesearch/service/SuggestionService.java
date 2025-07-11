package com.undoschool.coursesearch.service;

import com.undoschool.coursesearch.document.CourseDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuggestionService {

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    public List<String> getSuggestions(String q) {
        // Matches any course title containing the input substring
        Criteria criteria = new Criteria("title").matches(q).or(new Criteria("description").matches(q));
        CriteriaQuery query = new CriteriaQuery(criteria);

        SearchHits<CourseDocument> hits = elasticsearchTemplate.search(query, CourseDocument.class);

        return hits.getSearchHits()
                .stream()
                .map(hit -> hit.getContent().getTitle())
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }
}
