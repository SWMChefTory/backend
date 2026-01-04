package com.cheftory.api.auth.verifier.property;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "apple")
public class AppleProperties {
  // iOS/Android App ID
  private String appId;

  // Web Service ID
  private String serviceId;

  // Deprecated: backward compatibility를 위해 유지
  @Deprecated
  private String clientId;

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  /**
   * Backward compatibility를 위한 setter
   * client-id가 설정되면 app-id로 자동 매핑
   */
  @Deprecated
  public void setClientId(String clientId) {
    this.clientId = clientId;
    // backward compatibility: clientId가 있으면 appId로 사용
    if (this.appId == null) {
      this.appId = clientId;
    }
  }
}
