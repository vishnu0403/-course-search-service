package com.undoschool.coursesearch;

import com.undoschool.coursesearch.document.CourseDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CourseRepository extends ElasticsearchRepository<CourseDocument, String> {
}
