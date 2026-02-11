package com.cheftory.api.auth.verifier.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Apple JWKS 응답 DTO
 *
 * @param keys 공개키 목록
 */
public record AppleJwksResponse(List<AppleJwk> keys) {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * JSON 문자열로부터 AppleJwksResponse를 파싱합니다.
     *
     * @param json JSON 문자열
     * @return 파싱된 AppleJwksResponse
     */
    public static AppleJwksResponse fromJson(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode keysNode = root.get("keys");

            List<AppleJwk> keys = StreamSupport.stream(keysNode.spliterator(), false)
                    .map(AppleJwk::fromJsonNode)
                    .toList();

            return new AppleJwksResponse(keys);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JWKS JSON", e);
        }
    }

    /**
     * Nimbusds JWKSet으로 변환합니다.
     *
     * @return JWKSet 객체
     */
    public JWKSet toJwkSet() {
        List<JWK> jwks = keys().stream().map(AppleJwk::toRsaKey).collect(Collectors.toList());

        return new JWKSet(jwks);
    }

    /**
     * 단일 JWKS 키 정보
     *
     * @param kty 키 타입 (RSA)
     * @param kid 키 ID
     * @param use 사용 용도 (sig)
     * @param alg 알고리즘 (RS256)
     * @param n Modulus (Base64 URL encoded)
     * @param e Exponent (Base64 URL encoded)
     */
    public record AppleJwk(String kty, String kid, String use, String alg, String n, String e) {

        public static AppleJwk fromJsonNode(JsonNode node) {
            return new AppleJwk(
                    node.get("kty").asText(),
                    node.get("kid").asText(),
                    node.get("use").asText(),
                    node.get("alg").asText(),
                    node.get("n").asText(),
                    node.get("e").asText());
        }

        public RSAKey toRsaKey() {
            try {
                Base64URL modulusB64 = new Base64URL(n);
                Base64URL exponentB64 = new Base64URL(e);

                return new com.nimbusds.jose.jwk.RSAKey.Builder(modulusB64, exponentB64)
                    .keyID(kid())
                    .algorithm(com.nimbusds.jose.JWSAlgorithm.parse(alg))
                    .build();
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to convert to RSAKey: " + kid, e);
            }
        }

        /**
         * Base64URL 값으로부터 AppleJwk 생성 (테스트용)
         *
         * @param kty 키 타입
         * @param kid 키 ID
         * @param use 사용 용도
         * @param alg 알고리즘
         * @param modulusB64 Modulus Base64URL 인코딩된 값
         * @param exponentB64 Exponent Base64URL 인코딩된 값
         * @return AppleJwk 객체
         */
        public static AppleJwk fromBase64URL(
                String kty,
                String kid,
                String use,
                String alg,
                com.nimbusds.jose.util.Base64URL modulusB64,
                com.nimbusds.jose.util.Base64URL exponentB64) {
            return new AppleJwk(kty, kid, use, alg, modulusB64.toString(), exponentB64.toString());
        }
    }
}
