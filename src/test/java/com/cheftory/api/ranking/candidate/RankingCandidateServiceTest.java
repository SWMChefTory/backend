package com.cheftory.api.ranking.candidate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.SearchPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RankingCandidateService 테스트")
class RankingCandidateServiceTest {

    private RankingCandidateSearchPort candidateSearchPort;
    private RankingCandidateService service;

    @BeforeEach
    void setUp() {
        candidateSearchPort = mock(RankingCandidateSearchPort.class);
        service = new RankingCandidateService(candidateSearchPort);
    }

    @Nested
    @DisplayName("PIT 생성 (openPit)")
    class OpenPit {

        @Nested
        @DisplayName("Given - PIT 생성 요청 시")
        class GivenPitRequest {
            String pitId;

            @BeforeEach
            void setUp() throws SearchException {
                pitId = "test-pit-id";
                doReturn(pitId).when(candidateSearchPort).openPit();
            }

            @Nested
            @DisplayName("When - PIT를 생성하면")
            class WhenOpeningPit {
                String result;

                @BeforeEach
                void setUp() throws SearchException {
                    result = service.openPit();
                }

                @Test
                @DisplayName("Then - PIT ID를 반환한다")
                void thenReturnsPitId() throws SearchException {
                    assertThat(result).isEqualTo(pitId);
                    verify(candidateSearchPort).openPit();
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색 예외 발생 시")
        class GivenSearchException {
            SearchException exception;

            @BeforeEach
            void setUp() throws SearchException {
                exception = mock(SearchException.class);
                doThrow(exception).when(candidateSearchPort).openPit();
            }

            @Nested
            @DisplayName("When - PIT를 생성하면")
            class WhenOpeningPit {

                @Test
                @DisplayName("Then - 예외를 전파한다")
                void thenPropagatesException() {
                    SearchException thrown = assertThrows(SearchException.class, () -> service.openPit());
                    assertSame(exception, thrown);
                }
            }
        }
    }

    @Nested
    @DisplayName("PIT 검색 (searchWithPit)")
    class SearchWithPit {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            RankingSurfaceType surfaceType;
            RankingItemType itemType;
            int pageSize;
            PersonalizationProfile profile;
            String pitId;
            String searchAfter;
            SearchPage searchPage;

            @BeforeEach
            void setUp() throws SearchException {
                surfaceType = RankingSurfaceType.CUISINE_BABY;
                itemType = RankingItemType.RECIPE;
                pageSize = 10;
                profile = mock(PersonalizationProfile.class);
                pitId = "test-pit-id";
                searchAfter = null;
                searchPage = mock(SearchPage.class);
                doReturn(searchPage)
                        .when(candidateSearchPort)
                        .searchWithPit(surfaceType, itemType, pageSize, profile, pitId, searchAfter);
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenSearching {
                SearchPage result;

                @BeforeEach
                void setUp() throws SearchException {
                    result = service.searchWithPit(surfaceType, itemType, pageSize, profile, pitId, searchAfter);
                }

                @Test
                @DisplayName("Then - 검색 결과를 반환한다")
                void thenReturnsSearchPage() throws SearchException {
                    assertThat(result).isEqualTo(searchPage);
                    verify(candidateSearchPort)
                            .searchWithPit(surfaceType, itemType, pageSize, profile, pitId, searchAfter);
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색 예외 발생 시")
        class GivenSearchException {
            RankingSurfaceType surfaceType;
            RankingItemType itemType;
            int pageSize;
            PersonalizationProfile profile;
            String pitId;
            String searchAfter;
            SearchException exception;

            @BeforeEach
            void setUp() throws SearchException {
                surfaceType = RankingSurfaceType.CUISINE_BABY;
                itemType = RankingItemType.RECIPE;
                pageSize = 10;
                profile = mock(PersonalizationProfile.class);
                pitId = "test-pit-id";
                searchAfter = null;
                exception = mock(SearchException.class);
                doThrow(exception)
                        .when(candidateSearchPort)
                        .searchWithPit(surfaceType, itemType, pageSize, profile, pitId, searchAfter);
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenSearching {

                @Test
                @DisplayName("Then - 예외를 전파한다")
                void thenPropagatesException() {
                    SearchException thrown = assertThrows(
                            SearchException.class,
                            () -> service.searchWithPit(surfaceType, itemType, pageSize, profile, pitId, searchAfter));
                    assertSame(exception, thrown);
                }
            }
        }
    }

    @Nested
    @DisplayName("PIT 닫기 (closePit)")
    class ClosePit {

        @Nested
        @DisplayName("Given - PIT ID가 주어졌을 때")
        class GivenPitId {
            String pitId;

            @BeforeEach
            void setUp() {
                pitId = "test-pit-id";
            }

            @Nested
            @DisplayName("When - PIT를 닫으면")
            class WhenClosingPit {

                @BeforeEach
                void setUp() {
                    service.closePit(pitId);
                }

                @Test
                @DisplayName("Then - PIT를 닫는다")
                void thenClosesPit() {
                    verify(candidateSearchPort).closePit(pitId);
                }
            }
        }
    }
}
