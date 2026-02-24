package com.cheftory.api.search.history;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api._common.Clock;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@DataRedisTest
@ActiveProfiles("test")
@DisplayName("RecipeSearchHistoryRepository 통합 테스트")
class SearchHistoryRepositoryTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private SearchHistoryRepository repository;
    private Clock clock;

    @BeforeEach
    void setUp() {
        repository = new SearchHistoryRepository(redisTemplate);
        clock = new Clock();

        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Nested
    @DisplayName("save 메서드 테스트")
    class SaveTest {

        @Test
        @DisplayName("검색어를 저장할 수 있다")
        void shouldSaveSearchText() {
            String key = "test:search:history";
            String searchText = "김치찌개";

            repository.save(key, searchText, clock);

            List<String> result = repository.findRecent(key, 10);
            assertThat(result).contains(searchText);
        }

        @Test
        @DisplayName("여러 검색어를 저장할 수 있다")
        void shouldSaveMultipleSearchTexts() {
            String key = "test:search:history";
            String searchText1 = "김치찌개";
            String searchText2 = "된장찌개";
            String searchText3 = "부대찌개";

            repository.save(key, searchText1, clock);
            repository.save(key, searchText2, clock);
            repository.save(key, searchText3, clock);

            List<String> result = repository.findRecent(key, 10);
            assertThat(result).contains(searchText1, searchText2, searchText3);
        }

        @Test
        @DisplayName("동일한 검색어를 중복 저장할 수 있다")
        void shouldSaveDuplicateSearchText() {
            String key = "test:search:history";
            String searchText = "김치찌개";

            repository.save(key, searchText, clock);
            repository.save(key, searchText, clock);

            List<String> result = repository.findRecent(key, 10);
            assertThat(result).contains(searchText);
        }

        @Test
        @DisplayName("빈 문자열을 저장할 수 있다")
        void shouldSaveEmptyString() {
            String key = "test:search:history";
            String searchText = "";

            repository.save(key, searchText, clock);

            List<String> result = repository.findRecent(key, 10);
            assertThat(result).contains(searchText);
        }

        @Test
        @DisplayName("특수 문자가 포함된 검색어를 저장할 수 있다")
        void shouldSaveSearchTextWithSpecialCharacters() {
            String key = "test:search:history";
            String searchText = "김치찌개!@#$%^&*()";

            repository.save(key, searchText, clock);

            List<String> result = repository.findRecent(key, 10);
            assertThat(result).contains(searchText);
        }
    }

    @Nested
    @DisplayName("findRecent 메서드 테스트")
    class FindRecentTest {

        @Test
        @DisplayName("최근 검색어를 조회할 수 있다")
        void shouldFindRecentSearchTexts() {
            String key = "test:search:history";
            String searchText1 = "김치찌개";
            String searchText2 = "된장찌개";
            String searchText3 = "부대찌개";

            repository.save(key, searchText1, clock);
            repository.save(key, searchText2, clock);
            repository.save(key, searchText3, clock);

            List<String> result = repository.findRecent(key, 2);

            assertThat(result).hasSize(2);
            assertThat(result.get(0)).isEqualTo(searchText3);
            assertThat(result.get(1)).isEqualTo(searchText2);
        }

        @Test
        @DisplayName("요청한 개수만큼 반환한다")
        void shouldReturnRequestedNumberOfItems() {
            String key = "test:search:history";
            for (int i = 0; i < 10; i++) {
                repository.save(key, "검색어" + i, clock);
            }

            List<String> result = repository.findRecent(key, 5);

            assertThat(result).hasSize(5);
        }

        @Test
        @DisplayName("저장된 개수보다 적게 요청하면 저장된 개수만큼 반환한다")
        void shouldReturnAvailableItemsWhenRequestedMoreThanStored() {
            String key = "test:search:history";
            repository.save(key, "검색어1", clock);
            repository.save(key, "검색어2", clock);

            List<String> result = repository.findRecent(key, 10);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("존재하지 않는 키로 조회하면 빈 목록을 반환한다")
        void shouldReturnEmptyListForNonExistentKey() {
            String key = "test:nonexistent";

            List<String> result = repository.findRecent(key, 10);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 키로 조회하면 빈 목록을 반환한다")
        void shouldReturnEmptyListForEmptyKey() {
            String key = "test:empty";

            List<String> result = repository.findRecent(key, 10);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("limit이 0이면 빈 목록을 반환한다")
        void shouldReturnEmptyListWhenLimitIsZero() {
            String key = "test:search:history";
            repository.save(key, "검색어1", clock);

            List<String> result = repository.findRecent(key, 0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("limit이 음수면 빈 목록을 반환한다")
        void shouldReturnEmptyListWhenLimitIsNegative() {
            String key = "test:search:history";
            repository.save(key, "검색어1", clock);

            List<String> result = repository.findRecent(key, -1);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("remove 메서드 테스트")
    class RemoveTest {

        @Test
        @DisplayName("검색어를 삭제할 수 있다")
        void shouldRemoveSearchText() {
            String key = "test:search:history";
            String searchText = "김치찌개";
            repository.save(key, searchText, clock);

            repository.remove(key, searchText);

            List<String> result = repository.findRecent(key, 10);
            assertThat(result).doesNotContain(searchText);
        }

        @Test
        @DisplayName("존재하지 않는 검색어를 삭제해도 예외가 발생하지 않는다")
        void shouldNotThrowExceptionWhenRemovingNonExistentSearchText() {
            String key = "test:search:history";
            String searchText = "존재하지않는검색어";

            repository.remove(key, searchText);
        }

        @Test
        @DisplayName("여러 검색어 중 특정 검색어만 삭제할 수 있다")
        void shouldRemoveSpecificSearchTextFromMultiple() {
            String key = "test:search:history";
            String searchText1 = "김치찌개";
            String searchText2 = "된장찌개";
            String searchText3 = "부대찌개";

            repository.save(key, searchText1, clock);
            repository.save(key, searchText2, clock);
            repository.save(key, searchText3, clock);

            repository.remove(key, searchText2);

            List<String> result = repository.findRecent(key, 10);
            assertThat(result).contains(searchText1, searchText3);
            assertThat(result).doesNotContain(searchText2);
        }

        @Test
        @DisplayName("빈 문자열을 삭제할 수 있다")
        void shouldRemoveEmptyString() {
            String key = "test:search:history";
            String searchText = "";
            repository.save(key, searchText, clock);

            repository.remove(key, searchText);

            List<String> result = repository.findRecent(key, 10);
            assertThat(result).doesNotContain(searchText);
        }
    }

    @Nested
    @DisplayName("removeOldEntries 메서드 테스트")
    class RemoveOldEntriesTest {

        @Test
        @DisplayName("최대 개수를 초과하면 오래된 항목을 삭제한다")
        void shouldRemoveOldEntriesWhenExceedingMaxSize() {
            String key = "test:search:history";
            int maxSize = 3;

            for (int i = 0; i < 5; i++) {
                repository.save(key, "검색어" + i, clock);
            }

            repository.removeOldEntries(key, maxSize);

            List<String> result = repository.findRecent(key, 10);
            assertThat(result).hasSize(maxSize);
        }

        @Test
        @DisplayName("최대 개수 이하면 삭제하지 않는다")
        void shouldNotRemoveEntriesWhenWithinMaxSize() {
            String key = "test:search:history";
            int maxSize = 5;

            for (int i = 0; i < 3; i++) {
                repository.save(key, "검색어" + i, clock);
            }

            repository.removeOldEntries(key, maxSize);

            List<String> result = repository.findRecent(key, 10);
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("최대 개수가 0이면 모든 항목을 삭제한다")
        void shouldRemoveAllEntriesWhenMaxSizeIsZero() {
            String key = "test:search:history";
            int maxSize = 0;

            repository.save(key, "검색어1", clock);
            repository.save(key, "검색어2", clock);

            repository.removeOldEntries(key, maxSize);

            List<String> result = repository.findRecent(key, 10);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 키에 대해 삭제해도 예외가 발생하지 않는다")
        void shouldNotThrowExceptionWhenRemovingFromNonExistentKey() {
            String key = "test:nonexistent";
            int maxSize = 5;

            repository.removeOldEntries(key, maxSize);
        }
    }

    @Nested
    @DisplayName("setExpire 메서드 테스트")
    class SetExpireTest {

        @Test
        @DisplayName("키에 만료 시간을 설정할 수 있다")
        void shouldSetExpireTime() throws InterruptedException {
            String key = "test:search:history";
            String searchText = "김치찌개";
            Duration ttl = Duration.ofSeconds(1);

            repository.save(key, searchText, clock);
            repository.setExpire(key, ttl);

            List<String> result = repository.findRecent(key, 10);
            assertThat(result).contains(searchText);

            Thread.sleep(1100);

            result = repository.findRecent(key, 10);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("긴 만료 시간을 설정할 수 있다")
        void shouldSetLongExpireTime() {
            String key = "test:search:history";
            String searchText = "김치찌개";
            Duration ttl = Duration.ofHours(1);

            repository.save(key, searchText, clock);
            repository.setExpire(key, ttl);

            List<String> result = repository.findRecent(key, 10);
            assertThat(result).contains(searchText);
        }

        @Test
        @DisplayName("존재하지 않는 키에 만료 시간을 설정해도 예외가 발생하지 않는다")
        void shouldNotThrowExceptionWhenSettingExpireForNonExistentKey() {
            String key = "test:nonexistent";
            Duration ttl = Duration.ofHours(1);

            repository.setExpire(key, ttl);
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("전체 검색 히스토리 관리 시나리오를 테스트할 수 있다")
        void shouldHandleCompleteSearchHistoryScenario() {
            String key = "test:search:history";
            String searchText1 = "김치찌개";
            String searchText2 = "된장찌개";
            String searchText3 = "부대찌개";
            String searchText4 = "순두부찌개";
            String searchText5 = "해물찌개";

            repository.save(key, searchText1, clock);
            repository.save(key, searchText2, clock);
            repository.save(key, searchText3, clock);
            repository.save(key, searchText4, clock);
            repository.save(key, searchText5, clock);

            List<String> recent3 = repository.findRecent(key, 3);
            assertThat(recent3).hasSize(3);
            assertThat(recent3.get(0)).isEqualTo(searchText5);
            assertThat(recent3.get(1)).isEqualTo(searchText4);
            assertThat(recent3.get(2)).isEqualTo(searchText3);

            repository.remove(key, searchText3);

            List<String> afterRemoval = repository.findRecent(key, 10);
            assertThat(afterRemoval).doesNotContain(searchText3);
            assertThat(afterRemoval).contains(searchText1, searchText2, searchText4, searchText5);

            repository.removeOldEntries(key, 3);

            List<String> finalResult = repository.findRecent(key, 10);
            assertThat(finalResult).hasSize(3);
            assertThat(finalResult).contains(searchText2, searchText4, searchText5);
        }
    }
}
