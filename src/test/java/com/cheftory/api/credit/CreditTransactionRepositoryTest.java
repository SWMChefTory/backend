package com.cheftory.api.credit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.DbContextTest;
import com.cheftory.api.credit.entity.CreditReason;
import com.cheftory.api.credit.entity.CreditTransaction;
import com.cheftory.api.credit.entity.CreditTransactionType;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

@DisplayName("CreditTransactionRepositoryTest")
class CreditTransactionRepositoryTest extends DbContextTest {

    @Autowired
    private CreditTransactionRepository txRepository;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("existsByIdempotencyKey")
    class ExistsByIdempotencyKey {

        @Test
        @DisplayName("저장된 idempotencyKey면 true")
        void exists_true() {
            String idem = "idem-" + UUID.randomUUID();
            CreditTransaction tx = newTx(idem, 100L);

            txRepository.saveAndFlush(tx);
            em.clear();

            assertThat(txRepository.existsByIdempotencyKey(idem)).isTrue();
        }

        @Test
        @DisplayName("없는 idempotencyKey면 false")
        void exists_false() throws Exception {
            assertThat(txRepository.existsByIdempotencyKey("nope-" + UUID.randomUUID()))
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("유니크 제약(uq_credit_tx_idempotency_key)")
    class UniqueIdempotencyKey {

        @Test
        @DisplayName("같은 idempotencyKey로 두 번 저장하면 DataIntegrityViolationException")
        void duplicate_idempotencyKey_throws() {
            String idem = "idem-dup-" + UUID.randomUUID();

            txRepository.saveAndFlush(newTx(idem, 100L));
            em.clear();

            assertThatThrownBy(() -> txRepository.saveAndFlush(newTx(idem, 50L)))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    private static CreditTransaction newTx(String idempotencyKey, long amount) {
        try {
            Constructor<CreditTransaction> ctor = CreditTransaction.class.getDeclaredConstructor(
                    UUID.class,
                    UUID.class,
                    CreditTransactionType.class,
                    CreditReason.class,
                    long.class,
                    String.class,
                    LocalDateTime.class);
            ctor.setAccessible(true);

            return ctor.newInstance(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    CreditTransactionType.GRANT,
                    CreditReason.values()[0],
                    amount,
                    idempotencyKey,
                    LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
