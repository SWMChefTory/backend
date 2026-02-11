package com.cheftory.api.auth.verifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.cheftory.api.auth.verifier.client.AppleTokenClient;
import com.cheftory.api.auth.verifier.exception.VerificationErrorCode;
import com.cheftory.api.auth.verifier.exception.VerificationException;
import com.cheftory.api.auth.verifier.property.AppleProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AppleTokenVerifier 테스트")
class AppleTokenVerifierTest {

    private AppleTokenVerifier appleTokenVerifier;
    private AppleProperties appleProperties;
    private RSAKey testRsaKey;
    private KeyPair testKeyPair;
    private AppleTokenClient mockAppleTokenClient;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트용 RSA 키 생성
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        testKeyPair = keyPairGenerator.generateKeyPair();

        String keyId = UUID.randomUUID().toString();
        testRsaKey = new RSAKey.Builder((RSAPublicKey) testKeyPair.getPublic())
                .privateKey((RSAPrivateKey) testKeyPair.getPrivate())
                .keyID(keyId)
                .build();

        // AppleProperties Mock
        appleProperties = mock(AppleProperties.class);
        org.mockito.Mockito.doReturn("com.cheftory.test").when(appleProperties).getAppId();
        org.mockito.Mockito.doReturn("com.cheftory.web").when(appleProperties).getServiceId();

        // AppleTokenClient Mock - 테스트용 JWKSet 반환
        mockAppleTokenClient = new AppleTokenClient() {
            @Override
            public JWKSet fetchJwks() throws VerificationException {
                return new JWKSet(testRsaKey);
            }
        };

        appleTokenVerifier = new AppleTokenVerifier(mockAppleTokenClient, appleProperties);
    }

    @Nested
    @DisplayName("getSubFromToken 메서드")
    class GetSubFromToken {

        @Nested
        @DisplayName("Given - 잘못된 형식의 토큰일 때")
        class GivenInvalidFormat {

            @Test
            @DisplayName("Then - APPLE_INVALID_FORMAT 예외를 던진다")
            void thenThrowsException() {
                assertThatThrownBy(() -> appleTokenVerifier.getSubFromToken("invalid-token"))
                        .isInstanceOf(VerificationException.class)
                        .extracting(e -> ((VerificationException) e).getError())
                        .isEqualTo(VerificationErrorCode.APPLE_INVALID_FORMAT);
            }
        }

        @Nested
        @DisplayName("Given - 잘못된 발급자일 때")
        class GivenInvalidIssuer {

            @Test
            @DisplayName("Then - APPLE_INVALID_ISSUER 예외를 던진다")
            void thenThrowsException() throws Exception {
                String jwt = createValidJwt("test-sub", "https://evil.com", "com.cheftory.test");

                assertThatThrownBy(() -> appleTokenVerifier.getSubFromToken(jwt))
                        .isInstanceOf(VerificationException.class)
                        .extracting(e -> ((VerificationException) e).getError())
                        .isEqualTo(VerificationErrorCode.APPLE_INVALID_ISSUER);
            }
        }

        @Nested
        @DisplayName("Given - 잘못된 audience일 때")
        class GivenInvalidAudience {

            @Test
            @DisplayName("Then - APPLE_INVALID_AUDIENCE 예외를 던진다")
            void thenThrowsException() throws Exception {
                String jwt = createValidJwt("test-sub", "https://appleid.apple.com", "com.evil.app");

                assertThatThrownBy(() -> appleTokenVerifier.getSubFromToken(jwt))
                        .isInstanceOf(VerificationException.class)
                        .extracting(e -> ((VerificationException) e).getError())
                        .isEqualTo(VerificationErrorCode.APPLE_INVALID_AUDIENCE);
            }
        }

        @Nested
        @DisplayName("Given - 만료된 토큰일 때")
        class GivenExpiredToken {

            @Test
            @DisplayName("Then - APPLE_TOKEN_EXPIRED 예외를 던진다")
            void thenThrowsException() throws Exception {
                String jwt = createExpiredJwt("test-sub", "https://appleid.apple.com", "com.cheftory.test");

                assertThatThrownBy(() -> appleTokenVerifier.getSubFromToken(jwt))
                        .isInstanceOf(VerificationException.class)
                        .extracting(e -> ((VerificationException) e).getError())
                        .isEqualTo(VerificationErrorCode.APPLE_TOKEN_EXPIRED);
            }
        }

        @Nested
        @DisplayName("Given - 유효한 토큰일 때")
        class GivenValidToken {

            @Test
            @DisplayName("Then - sub를 반환한다")
            void thenReturnsSub() throws Exception {
                String jwt = createValidJwt("test-sub-123", "https://appleid.apple.com", "com.cheftory.test");
                String result = appleTokenVerifier.getSubFromToken(jwt);
                assertThat(result).isEqualTo("test-sub-123");
            }
        }

        @Nested
        @DisplayName("Given - Web Service ID audience일 때")
        class GivenWebServiceIdAudience {

            @Test
            @DisplayName("Then - sub를 반환한다")
            void thenReturnsSub() throws Exception {
                String jwt = createValidJwt("test-sub-web", "https://appleid.apple.com", "com.cheftory.web");
                String result = appleTokenVerifier.getSubFromToken(jwt);
                assertThat(result).isEqualTo("test-sub-web");
            }
        }
    }

    private String createValidJwt(String sub, String issuer, String audience) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(sub)
                .issuer(issuer)
                .audience(audience)
                .expirationTime(new Date(System.currentTimeMillis() + 3600000)) // 1시간 후
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(testRsaKey.getKeyID())
                .build();

        SignedJWT signedJWT = new SignedJWT(header, claims);
        JWSSigner signer = new RSASSASigner(testKeyPair.getPrivate());
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    private String createExpiredJwt(String sub, String issuer, String audience) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(sub)
                .issuer(issuer)
                .audience(audience)
                .expirationTime(new Date(System.currentTimeMillis() - 3600000)) // 1시간 전
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(testRsaKey.getKeyID())
                .build();

        SignedJWT signedJWT = new SignedJWT(header, claims);
        JWSSigner signer = new RSASSASigner(testKeyPair.getPrivate());
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }
}
