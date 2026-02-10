package com.cheftory.api.affiliate.coupang;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("HmacGenerator 테스트")
class HmacGeneratorTest {

    @Nested
    @DisplayName("HMAC 생성 (generate)")
    class Generate {

        @Nested
        @DisplayName("Given - 유효한 입력값이 주어졌을 때")
        class GivenValidInputs {
            String method;
            String uri;
            String secretKey;
            String accessKey;

            @BeforeEach
            void setUp() {
                method = "GET";
                uri = "/v2/providers/affiliate_open_api/apis/openapi/products/search?keyword=water";
                secretKey = "secret-1234";
                accessKey = "access-1234";
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenGenerating {
                String result;

                @BeforeEach
                void setUp() {
                    result = HmacGenerator.generate(method, uri, secretKey, accessKey);
                }

                @Test
                @DisplayName("Then - 올바른 형식의 HMAC 문자열을 반환한다")
                void thenReturnsValidHmac() {
                    assertThat(result).startsWith("CEA algorithm=HmacSHA256, access-key=access-1234");
                    assertThat(result).contains("signature=");
                    assertThat(result).contains("signed-date=");
                    assertThat(result.split(",")).hasSize(4);
                }
            }
        }

        @Nested
        @DisplayName("Given - 쿼리가 없는 URI가 주어졌을 때")
        class GivenUriWithoutQuery {
            String method;
            String uri;
            String secretKey;
            String accessKey;

            @BeforeEach
            void setUp() {
                method = "POST";
                uri = "/v2/providers/affiliate_open_api/apis/openapi/v1/deeplink";
                secretKey = "secret";
                accessKey = "access";
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenGenerating {
                String result;

                @BeforeEach
                void setUp() {
                    result = HmacGenerator.generate(method, uri, secretKey, accessKey);
                }

                @Test
                @DisplayName("Then - 올바르게 처리하여 반환한다")
                void thenReturnsValidHmac() {
                    assertThat(result).contains("access-key=access");
                    assertThat(result).contains("algorithm=HmacSHA256");
                    assertThat(result).doesNotContain("null");
                }
            }
        }

        @Nested
        @DisplayName("Given - 잘못된 형식의 URI가 주어졌을 때")
        class GivenInvalidUri {
            String uri;

            @BeforeEach
            void setUp() {
                uri = "/v2/path?param1=a?param2=b";
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenGenerating {

                @Test
                @DisplayName("Then - 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> HmacGenerator.generate("GET", uri, "secret", "access"))
                            .isInstanceOf(RuntimeException.class)
                            .hasMessageContaining("incorrect uri format");
                }
            }
        }

        @Nested
        @DisplayName("Given - 특정 입력값에 대한 검증이 필요할 때")
        class GivenSpecificInputs {
            String method;
            String secretKey;
            String accessKey;
            String fixedDate;
            String expectedSignature;

            @BeforeEach
            void setUp() throws Exception {
                method = "GET";
                secretKey = "abc123";
                accessKey = "xyz987";

                SimpleDateFormat df = new SimpleDateFormat("yyMMdd'T'HHmmss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                fixedDate = df.format(new Date(0));

                String message = fixedDate + method + "/path" + "x=1";
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                expectedSignature = Hex.encodeHexString(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
            }

            @Nested
            @DisplayName("When - 예상되는 포맷과 비교하면")
            class WhenComparing {
                String expectedFormat;

                @BeforeEach
                void setUp() {
                    expectedFormat = String.format(
                            "CEA algorithm=%s, access-key=%s, signed-date=%s, signature=%s",
                            "HmacSHA256", accessKey, fixedDate, expectedSignature);
                }

                @Test
                @DisplayName("Then - 포맷이 일치해야 한다")
                void thenMatchesFormat() {
                    String result = String.format(
                            "CEA algorithm=HmacSHA256, access-key=%s, signed-date=%s, signature=%s",
                            accessKey, fixedDate, expectedSignature);
                    assertThat(result).isEqualTo(expectedFormat);
                }
            }
        }
    }
}
