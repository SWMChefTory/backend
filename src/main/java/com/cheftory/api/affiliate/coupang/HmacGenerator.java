package com.cheftory.api.affiliate.coupang;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

public final class HmacGenerator {

  private static final String ALGORITHM = "HmacSHA256";
  private static final Charset STANDARD_CHARSET = StandardCharsets.UTF_8;

  /**
   * Generate HMAC signature
   *
   * @param method
   * @param uri http request uri
   * @param secretKey secret key that Coupang partner granted for calling open api
   * @param accessKey access key that Coupang partner granted for calling open api
   * @return HMAC signature
   */
  public static String generate(String method, String uri, String secretKey, String accessKey) {
    String[] parts = uri.split("\\?");
    if (parts.length > 2) {
      throw new RuntimeException("incorrect uri format");
    } else {
      String path = parts[0];
      String query = "";
      if (parts.length == 2) {
        query = parts[1];
      }

      DateTimeFormatter dateFormatGmt =
          DateTimeFormatter.ofPattern("yyMMdd'T'HHmmss'Z'")
              .withZone(ZoneOffset.UTC);
      String datetime = dateFormatGmt.format(Instant.now());

      String message = datetime + method + path + query;

      String signature;
      try {
        SecretKeySpec signingKey =
            new SecretKeySpec(secretKey.getBytes(STANDARD_CHARSET), ALGORITHM);
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(message.getBytes(STANDARD_CHARSET));
        signature = Hex.encodeHexString(rawHmac);
      } catch (GeneralSecurityException e) {
        throw new IllegalArgumentException(
            "Unexpected error while creating hash: " + e.getMessage(), e);
      }

      return String.format(
          "CEA algorithm=%s, access-key=%s, signed-date=%s, signature=%s",
          "HmacSHA256", accessKey, datetime, signature);
    }
  }
}
