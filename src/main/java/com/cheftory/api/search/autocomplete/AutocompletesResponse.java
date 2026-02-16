package com.cheftory.api.search.autocomplete;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 자동완성 검색어 응답 DTO.
 *
 * @param autocompletes 자동완성 검색어 목록
 */
public record AutocompletesResponse(
        @JsonProperty("autocompletes") List<Autocomplete> autocompletes) {

    /**
     * 자동완성 검색어.
     *
     * @param autocomplete 자동완성 텍스트
     */
    private record Autocomplete(
            @JsonProperty("autocomplete") String autocomplete) {
        private static Autocomplete from(com.cheftory.api.search.autocomplete.Autocomplete autocomplete) {
            return new Autocomplete(autocomplete.getText());
        }
    }

    /**
     * 자동완션 엔티티 목록으로부터 응답을 생성합니다.
     *
     * @param autocompletes 자동완성 엔티티 목록
     * @return 자동완성 검색어 응답
     */
    public static AutocompletesResponse from(List<com.cheftory.api.search.autocomplete.Autocomplete> autocompletes) {
        List<Autocomplete> list = autocompletes.stream()
                .map(AutocompletesResponse.Autocomplete::from)
                .toList();
        return new AutocompletesResponse(list);
    }
}
