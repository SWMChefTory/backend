package com.cheftory.api.search.autocomplete;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 자동완성 엔티티.
 *
 * <p>OpenSearch에 저장되는 자동완성 검색어 정보입니다.</p>
 */
@Document(indexName = "autocomplete")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Autocomplete {
    /**
     * 문서 ID (텍스트 해시).
     */
    @Id
    private String id;

    /**
     * 자동완성 텍스트.
     */
    @Field(type = FieldType.Text)
    private String text;

    /**
     * 검색 횟수.
     */
    @Field(type = FieldType.Integer)
    private Integer count;
}
