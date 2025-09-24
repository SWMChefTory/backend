package com.cheftory.api.account.auth.verifier;

import com.cheftory.api.account.auth.verifier.exception.VerificationErrorCode;
import com.cheftory.api.account.auth.verifier.exception.VerificationException;
import com.cheftory.api.account.auth.verifier.property.AppleProperties;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppleTokenVerifier {

  private final AppleProperties appleProperties;

  private static final String APPLE_PUBLIC_KEYS_URL = "https://appleid.apple.com/auth/keys";
  private static final String APPLE_ISSUER = "https://appleid.apple.com";
  private static final AtomicReference<JWKSet> cachedJwkSet = new AtomicReference<>();
  private static long lastFetchTimeMillis = 0;
  private static final long CACHE_DURATION_MS = 60 * 60 * 1000;

  private JWKSet getCachedJwkSet() {
    long now = System.currentTimeMillis();
    if (cachedJwkSet.get() == null || now - lastFetchTimeMillis > CACHE_DURATION_MS) {
      try {
        URI uri = URI.create(APPLE_PUBLIC_KEYS_URL);
        JWKSet newSet = JWKSet.load(uri.toURL());
        cachedJwkSet.set(newSet);
        lastFetchTimeMillis = now;
      } catch (Exception e) {
        log.error("[AppleTokenVerifier] 공개키 로딩 실패", e);
        throw new VerificationException(VerificationErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND);
      }
    }
    return cachedJwkSet.get();
  }

  private JWTClaimsSet extractVerifiedClaims(String identityToken) {
    try {
      SignedJWT jwt = SignedJWT.parse(identityToken);
      JWSHeader header = jwt.getHeader();

      if (!"RS256".equals(header.getAlgorithm().getName())) {
        log.error("[AppleTokenVerifier] 잘못된 알고리즘: {}", header.getAlgorithm());
        throw new VerificationException(VerificationErrorCode.APPLE_INVALID_ALGORITHM);
      }

      JWKSet jwkSet = getCachedJwkSet();
      JWK jwk = jwkSet.getKeyByKeyId(header.getKeyID());

      if (!(jwk instanceof RSAKey rsaKey)) {
        log.error(
            "[AppleTokenVerifier] 공개키 타입 오류 - kid: {}, alg: {}",
            header.getKeyID(),
            header.getAlgorithm());
        throw new VerificationException(VerificationErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND);
      }

      JWSVerifier verifier = new RSASSAVerifier(rsaKey);
      if (!jwt.verify(verifier)) {
        log.error("[AppleTokenVerifier] 서명 검증 실패");
        throw new VerificationException(VerificationErrorCode.APPLE_SIGNATURE_VERIFICATION_FAILED);
      }

      JWTClaimsSet claims = jwt.getJWTClaimsSet();
      Date now = new Date();

      if (claims.getExpirationTime() == null || now.after(claims.getExpirationTime())) {
        log.error("[AppleTokenVerifier] 토큰 만료됨 - exp: {}", claims.getExpirationTime());
        throw new VerificationException(VerificationErrorCode.APPLE_TOKEN_EXPIRED);
      }

      if (!APPLE_ISSUER.equals(claims.getIssuer())) {
        log.error("[AppleTokenVerifier] 잘못된 iss: {}", claims.getIssuer());
        throw new VerificationException(VerificationErrorCode.APPLE_INVALID_ISSUER);
      }

      if (!claims.getAudience().contains(appleProperties.getClientId())) {
        log.error("[AppleTokenVerifier] 잘못된 aud: {}", claims.getAudience());
        throw new VerificationException(VerificationErrorCode.APPLE_INVALID_AUDIENCE);
      }

      return claims;

    } catch (ParseException e) {
      log.error("[AppleTokenVerifier] 토큰 파싱 실패", e);
      throw new VerificationException(VerificationErrorCode.APPLE_INVALID_FORMAT);
    } catch (VerificationException e) {
      throw e;
    } catch (Exception e) {
      log.error("[AppleTokenVerifier] 알 수 없는 예외 발생", e);
      throw new VerificationException(VerificationErrorCode.UNKNOWN_ERROR);
    }
  }

  public String getSubFromToken(String identityToken) {
    JWTClaimsSet claims = extractVerifiedClaims(identityToken);
    return claims.getSubject();
  }
}
