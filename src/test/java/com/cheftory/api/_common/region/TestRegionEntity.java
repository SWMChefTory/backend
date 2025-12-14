package com.cheftory.api._common.region;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "test_region_entity")
public class TestRegionEntity extends MarketScope {
  @Id private UUID id;

  @Column(nullable = false)
  private String name;

  protected TestRegionEntity() {}

  private TestRegionEntity(UUID id, String name) {
    this.id = id;
    this.name = name;
  }

  public static TestRegionEntity of(String name) {
    return new TestRegionEntity(UUID.randomUUID(), name);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
