package com.cheftory.api.account.auth.verifier.property;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "apple")
public class AppleProperties {
  private String clientId;

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }
}
