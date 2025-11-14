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
 * Elasticsearch Document for Opportunity indexing.
 *
 * Enables:
 * - Full-text search on title and description
 * - Filtering by company, location
 * - Skills matching
 * - Salary range search
 * - Deadline-based search
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "opportunities")
public class OpportunityDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String company;

    @Field(type = FieldType.Keyword)
    private String location;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String requirements;

    @Field(type = FieldType.Keyword)
    private String salary;

    @Field(type = FieldType.Date)
    private Instant deadline;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    @Field(type = FieldType.Search_As_You_Type)
    private String suggest;

    // Skills array for matching
    @Field(type = FieldType.Keyword)
    private String[] skills;

    // Popularity score for ranking
    @Field(type = FieldType.Integer)
    private Integer applicationsCount = 0;
}
