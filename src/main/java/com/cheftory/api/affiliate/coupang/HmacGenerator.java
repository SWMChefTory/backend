package com.cheftory.api.affiliate.coupang;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

/**
 * 쿠팡 파트너스 API 인증을 위한 HMAC 서명 생성기.
 *
 * <p>HmacSHA256 알고리즘을 사용하여 API 요청에 필요한 서명을 생성합니다.</p>
 */
public final class HmacGenerator {

    private static final String ALGORITHM = "HmacSHA256";
    private static final Charset STANDARD_CHARSET = StandardCharsets.UTF_8;

    private static final DateTimeFormatter DATE_FORMAT_GMT =
            DateTimeFormatter.ofPattern("yyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    /**
     * 쿠팡 파트너스 API 인증을 위한 HMAC 서명을 생성합니다.
     *
     * @param method HTTP 메서드 (예: GET, POST)
     * @param uri HTTP 요청 URI
     * @param secretKey 쿠팡 파트너스에서 발급받은 시크릿 키
     * @param accessKey 쿠팡 파트너스에서 발급받은 액세스 키
     * @return HMAC 서명이 포함된 Authorization 헤더 값
     */
    public static String generate(String method, String uri, String secretKey, String accessKey) {
        String[] parts = uri.split("\\?");
        if (parts.length > 2) {
            throw new IllegalArgumentException("incorrect uri format");
        }

        String path = parts[0];
        String query = (parts.length == 2) ? parts[1] : "";

        String datetime = DATE_FORMAT_GMT.format(Instant.now());

        String message = datetime + method.toUpperCase(Locale.ROOT) + path + query;

        try {
            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(STANDARD_CHARSET), ALGORITHM);
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(STANDARD_CHARSET));
            String signature = Hex.encodeHexString(rawHmac);

            return String.format(
                    "CEA algorithm=%s, access-key=%s, signed-date=%s, signature=%s",
                    ALGORITHM, accessKey, datetime, signature);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Unexpected error while creating hash: " + e.getMessage(), e);
        }
    }
}
