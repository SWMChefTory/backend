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

public final class HmacGenerator {

    private static final String ALGORITHM = "HmacSHA256";
    private static final Charset STANDARD_CHARSET = StandardCharsets.UTF_8;

    private static final DateTimeFormatter DATE_FORMAT_GMT =
            DateTimeFormatter.ofPattern("yyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    /**
     * Generate HMAC signature
     *
     * @param method HTTP method (e.g. GET, POST)
     * @param uri http request uri
     * @param secretKey secret key that Coupang partner granted for calling open api
     * @param accessKey access key that Coupang partner granted for calling open api
     * @return HMAC signature
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
