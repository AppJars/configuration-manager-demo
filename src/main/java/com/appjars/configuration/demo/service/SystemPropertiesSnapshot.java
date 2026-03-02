/*-
 * #%L
 * Configuration Manager - Demo
 * %%
 * Copyright (C) 2023 - 2026 AppJars
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.appjars.configuration.demo.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

/**
 * Singleton holding the {@code system_*} configuration values injected at startup. They stay frozen
 * until the Spring context is restarted, which is what the "Reload configuration" button does.
 */
@Service
public class SystemPropertiesSnapshot {

  private final String stringValue;
  private final Integer integerValue;
  private final LocalDate dateValue;
  private final LocalTime timeValue;
  private final Boolean booleanValue;
  private final BigDecimal decimalValue;

  /** When this singleton was created: startup, or the last context restart. */
  private final Instant capturedAt;

  public SystemPropertiesSnapshot(
      @Value("${com.appjars.configmanager.demo.system_string:DEFAULT VALUE}") String stringValue,
      @Value("${com.appjars.configmanager.demo.system_integer:-1}") Integer integerValue,
      @Value("${com.appjars.configmanager.demo.system_date:1970-01-01}") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateValue,
      @Value("${com.appjars.configmanager.demo.system_time:00:00:00}") @DateTimeFormat(pattern = "HH:mm:ss",
          fallbackPatterns = {"HH:mm"}) LocalTime timeValue,
      @Value("${com.appjars.configmanager.demo.system_boolean:false}") Boolean booleanValue,
      @Value("${com.appjars.configmanager.demo.system_decimal:0.0}") BigDecimal decimalValue) {
    this.stringValue = stringValue;
    this.integerValue = integerValue;
    this.dateValue = dateValue;
    this.timeValue = timeValue;
    this.booleanValue = booleanValue;
    this.decimalValue = decimalValue;
    this.capturedAt = Instant.now();
  }

  public String getStringValue() {
    return stringValue;
  }

  public Integer getIntegerValue() {
    return integerValue;
  }

  public LocalDate getDateValue() {
    return dateValue;
  }

  public LocalTime getTimeValue() {
    return timeValue;
  }

  public Boolean getBooleanValue() {
    return booleanValue;
  }

  public BigDecimal getDecimalValue() {
    return decimalValue;
  }

  public Instant getCapturedAt() {
    return capturedAt;
  }
}
