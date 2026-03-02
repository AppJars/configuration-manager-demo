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

import com.appjars.configurationmanager.model.ConfigurationType;
import com.appjars.configurationmanager.model.ConfigurationValueDto;
import com.appjars.configurationmanager.model.SystemConfigurationDto;
import com.appjars.configurationmanager.model.UserConfigurationDto;
import com.appjars.configurationmanager.service.SystemConfigurationService;
import com.appjars.configurationmanager.service.UserConfigurationService;
import com.appjars.configurationmanager.service.UserProvider;
import com.appjars.exception.FreeLimitReachedException;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@SuppressWarnings("serial")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Service
public class DemoService implements VaadinServiceInitListener, UserProvider {

  final SystemConfigurationService systemConfigurationService;
  final UserConfigurationService userConfigurationService;
  final String usernameDemo = "USER";

  public DemoService(SystemConfigurationService configurationService,
          UserConfigurationService userConfigurationService) {
    this.systemConfigurationService = configurationService;
    this.userConfigurationService = userConfigurationService;
  }

  @Override
  public void serviceInit(ServiceInitEvent event) {
    // The demo ships English copy only, so pin the UI locale: a browser locale the message bundle
    // does not provide leaves the UI with a language-less locale, which the date and time pickers
    // of the configuration list views reject.
    event.getSource().addUIInitListener(uiEvent -> uiEvent.getUI().setLocale(Locale.ENGLISH));
    if (systemConfigurationService.findAll().isEmpty()) {
      generateDemoData();
    }
  }

  // The appjar excludes names matching com.appjars.% from the free-tier count, which is why the 12
  // seeded configurations below do not consume the 5 creations a free-mode visitor gets.
  private void generateDemoData() {
    generateUserConfigurations(usernameDemo);
    generateSystemConfigurations();
  }

  private void generateUserConfigurations(String ownerUsername) {

    ConfigurationValueDto userConfigValue1 =
        ConfigurationValueDto.builder().created(Instant.now()).type(ConfigurationType.STRING)
            .stringValue("Hello world").created(Instant.now()).build();
    ConfigurationValueDto userConfigValue2 =
        ConfigurationValueDto.builder().created(Instant.now()).type(ConfigurationType.INTEGER)
            .intValue(100).created(Instant.now()).build();
    ConfigurationValueDto userConfigValue3 =
        ConfigurationValueDto.builder().created(Instant.now()).type(ConfigurationType.DATE)
            .dateValue(LocalDate.of(2025, 6, 21)).created(Instant.now()).build();
    ConfigurationValueDto userConfigValue4 =
        ConfigurationValueDto.builder().created(Instant.now()).type(ConfigurationType.TIME)
            .timeValue(LocalTime.of(5, 24, 13)).created(Instant.now()).build();
    ConfigurationValueDto userConfigValue5 =
        ConfigurationValueDto.builder().created(Instant.now()).type(ConfigurationType.BOOLEAN)
            .boolValue(false).created(Instant.now()).build();
    ConfigurationValueDto userConfigValue6 = ConfigurationValueDto.builder()
        .created(Instant.now()).type(ConfigurationType.BIG_DECIMAL)
        .decimalValue(BigDecimal.valueOf(1)).created(Instant.now()).build();

    UserConfigurationDto userConfig1;
    UserConfigurationDto userConfig2;
    UserConfigurationDto userConfig3;
    UserConfigurationDto userConfig4;
    UserConfigurationDto userConfig5;
    UserConfigurationDto userConfig6;
    userConfig1 = UserConfigurationDto.builder().name("com.appjars.configmanager.demo.user_string")
        .description("String configuration").type(ConfigurationType.STRING)
        .defaultValue(userConfigValue1).created(Instant.now()).build();

    userConfig2 = UserConfigurationDto.builder().name("com.appjars.configmanager.demo.user_integer")
        .description("Integer configuration").type(ConfigurationType.INTEGER)
        .defaultValue(userConfigValue2).created(Instant.now()).build();

    userConfig3 = UserConfigurationDto.builder().name("com.appjars.configmanager.demo.user_date").type(ConfigurationType.DATE)
        .description("Date configuration").defaultValue(userConfigValue3)
        .created(Instant.now()).build();

    userConfig4 = UserConfigurationDto.builder().name("com.appjars.configmanager.demo.user_time").type(ConfigurationType.TIME)
        .description("Time configuration").defaultValue(userConfigValue4)
        .created(Instant.now()).build();

    userConfig5 = UserConfigurationDto.builder().name("com.appjars.configmanager.demo.user_boolean")
        .type(ConfigurationType.BOOLEAN).description("Boolean configuration")
        .defaultValue(userConfigValue5).created(Instant.now()).build();

    userConfig6 = UserConfigurationDto.builder().name("com.appjars.configmanager.demo.user_decimal")
        .type(ConfigurationType.BIG_DECIMAL).description("BigDecimal configuration")
        .defaultValue(userConfigValue6).created(Instant.now()).build();
    try {
      userConfig1.setId(userConfigurationService.save(userConfig1));
      userConfig2.setId(userConfigurationService.save(userConfig2));
      userConfig3.setId(userConfigurationService.save(userConfig3));
      userConfig4.setId(userConfigurationService.save(userConfig4));
      userConfig5.setId(userConfigurationService.save(userConfig5));
      userConfig6.setId(userConfigurationService.save(userConfig6));
    } catch (FreeLimitReachedException fle) {
      log.warn("Could not seed the demo user configurations: {}", fle.getMessage());
    }
  }

  private void generateSystemConfigurations() {

    ConfigurationValueDto systemConfigValue1 =
        ConfigurationValueDto.builder().created(Instant.now()).type(ConfigurationType.STRING)
            .stringValue("Hello System").created(Instant.now()).build();
    ConfigurationValueDto systemConfigValue2 =
        ConfigurationValueDto.builder().created(Instant.now()).type(ConfigurationType.INTEGER)
            .intValue(700).created(Instant.now()).build();
    ConfigurationValueDto systemConfigValue3 =
        ConfigurationValueDto.builder().created(Instant.now()).type(ConfigurationType.DATE)
            .dateValue(LocalDate.of(2022, 6, 21)).created(Instant.now()).build();
    ConfigurationValueDto systemConfigValue4 =
        ConfigurationValueDto.builder().created(Instant.now()).type(ConfigurationType.TIME)
            .timeValue(LocalTime.of(5, 24, 13)).created(Instant.now()).build();
    ConfigurationValueDto systemConfigValue5 =
        ConfigurationValueDto.builder().created(Instant.now()).type(ConfigurationType.BOOLEAN)
            .boolValue(true).created(Instant.now()).build();
    ConfigurationValueDto systemConfigValue6 =
        ConfigurationValueDto.builder().created(Instant.now())
            .type(ConfigurationType.BIG_DECIMAL).decimalValue(BigDecimal.valueOf(365.656))
            .created(Instant.now()).build();

    SystemConfigurationDto systemConfig1 =
        SystemConfigurationDto.builder().name("com.appjars.configmanager.demo.system_string").description("String configuration")
            .type(ConfigurationType.STRING).defaultValue(systemConfigValue1)
            .created(Instant.now()).build();

    SystemConfigurationDto systemConfig2 =
        SystemConfigurationDto.builder().name("com.appjars.configmanager.demo.system_integer").description("Integer configuration")
            .type(ConfigurationType.INTEGER).defaultValue(systemConfigValue2)
            .created(Instant.now()).build();

    SystemConfigurationDto systemConfig3 =
        SystemConfigurationDto.builder().name("com.appjars.configmanager.demo.system_date").type(ConfigurationType.DATE)
            .description("Date configuration").defaultValue(systemConfigValue3)
            .valuesHistory(new ArrayList<>(List.of(systemConfigValue3.getCopy())))
            .created(Instant.now()).build();

    SystemConfigurationDto systemConfig4 =
        SystemConfigurationDto.builder().name("com.appjars.configmanager.demo.system_time").type(ConfigurationType.TIME)
            .description("Time configuration").defaultValue(systemConfigValue4)
            .valuesHistory(new ArrayList<>(List.of(systemConfigValue4.getCopy())))
            .created(Instant.now()).build();

    SystemConfigurationDto systemConfig5 =
        SystemConfigurationDto.builder().name("com.appjars.configmanager.demo.system_boolean").type(ConfigurationType.BOOLEAN)
            .description("Boolean configuration").defaultValue(systemConfigValue5)
            .valuesHistory(new ArrayList<>(List.of(systemConfigValue5.getCopy())))
            .created(Instant.now()).build();

    SystemConfigurationDto systemConfig6 =
        SystemConfigurationDto.builder().name("com.appjars.configmanager.demo.system_decimal").type(ConfigurationType.BIG_DECIMAL)
            .description("BigDecimal configuration").defaultValue(systemConfigValue6)
            .valuesHistory(new ArrayList<>(List.of(systemConfigValue6.getCopy())))
            .created(Instant.now()).build();
    try {
      systemConfig1.setId(systemConfigurationService.save(systemConfig1));
      systemConfig2.setId(systemConfigurationService.save(systemConfig2));
      systemConfig3.setId(systemConfigurationService.save(systemConfig3));
      systemConfig4.setId(systemConfigurationService.save(systemConfig4));
      systemConfig5.setId(systemConfigurationService.save(systemConfig5));
      systemConfig6.setId(systemConfigurationService.save(systemConfig6));
    } catch (FreeLimitReachedException fle) {
      log.warn("Could not seed the demo system configurations: {}", fle.getMessage());
    }
  }

  @Override
  public String getPrincipalUsername() {
    return (String) VaadinSession.getCurrent().getSession().getAttribute("loggedUsername");
  }

  @Override
  public List<String> getAllUsernames() {
    return new ArrayList<>(List.of("USER", "ADMIN"));
  }
}
