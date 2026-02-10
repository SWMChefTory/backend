package com.cheftory.api.search.query.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 검색 쿼리 엔티티.
 *
 * <p>OpenSearch에 저장되는 검색 쿼리 정보입니다.</p>
 */
@Document(indexName = "search_query")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SearchQuery {

    /** 문서 ID. */
    @Id
    private String id;

    /** 검색어. */
    @Field(type = FieldType.Text)
    private String searchText;

    /** 채널 제목. */
    @Field(type = FieldType.Text)
    private String channelTitle;

    /** 키워드 목록. */
    @Field(type = FieldType.Keyword)
    private List<String> keywords;
}
