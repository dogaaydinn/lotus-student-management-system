package com.lotus.lotusSPM.search.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

/**
 * Elasticsearch Document for Student indexing.
 *
 * Enterprise Pattern: Search Index Document
 *
 * Annotations:
 * - @Document: Marks this as an Elasticsearch document
 * - @Id: Document identifier
 * - @Field: Field mapping configuration
 * - FieldType.Text: Full-text searchable
 * - FieldType.Keyword: Exact match, aggregations
 *
 * Indexing Strategy:
 * - Real-time indexing on create/update
 * - Async indexing for performance
 * - Bulk indexing for large datasets
 * - Reindexing strategy for schema changes
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "students")
public class StudentDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String username;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String surname;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String email;

    @Field(type = FieldType.Keyword)
    private String faculty;

    @Field(type = FieldType.Keyword)
    private String department;

    @Field(type = FieldType.Keyword)
    private String internshipStatus;

    @Field(type = FieldType.Text)
    private String fullName; // Composite field for better search

    @Field(type = FieldType.Date)
    private Instant createdAt;

    @Field(type = FieldType.Date)
    private Instant updatedAt;

    // Suggestion field for auto-complete
    @Field(type = FieldType.Search_As_You_Type)
    private String suggest;
}
