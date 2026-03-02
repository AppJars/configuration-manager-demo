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
package com.appjars.configuration.demo.views;

import com.appjars.configuration.demo.service.SystemPropertiesSnapshot;
import com.appjars.configurationmanager.flow.util.LocalizedDateTimeFormatter;
import com.appjars.configurationmanager.model.Configuration;
import com.appjars.configurationmanager.model.UserConfigurationDto;
import com.appjars.configurationmanager.service.UserConfigurationService;
import com.appjars.configurationmanager.service.UserProvider;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;

@Route(value = "cfg/user/current-configs", layout = MainLayout.class)
public class CurrentConfigurationsView extends VerticalLayout
    implements BeforeEnterObserver, HasDynamicTitle {

  private final UserConfigurationService userService;
  private final UserProvider principalUserProvider;
  private final SystemPropertiesSnapshot systemPropertiesSnapshot;

  @Value("${com.appjars.configmanager.demo.system_string:DEFAULT VALUE}")
  String value1;

  @Value("${com.appjars.configmanager.demo.system_integer:-1}")
  Integer value2;

  @Value("${com.appjars.configmanager.demo.system_date:1970-01-01}")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  LocalDate value3;

  @Value("${com.appjars.configmanager.demo.system_time:00:00:00}")
  @DateTimeFormat(pattern = "HH:mm:ss", fallbackPatterns = {"HH:mm"})
  LocalTime value4;

  @Value("${com.appjars.configmanager.demo.system_boolean:false}")
  Boolean value5;

  @Value("${com.appjars.configmanager.demo.system_decimal:0.0}")
  BigDecimal value6;

  TextField config1 = new TextField(
      getTranslation("appjars.configmanager.demo.currentconfigurationsview.valuesystemstring"));
  TextField config2 = new TextField(
      getTranslation("appjars.configmanager.demo.currentconfigurationsview.valuesysteminteger"));
  TextField config3 = new TextField(
      getTranslation("appjars.configmanager.demo.currentconfigurationsview.valuesystemdate"));
  TextField config4 = new TextField(
      getTranslation("appjars.configmanager.demo.currentconfigurationsview.valuesystemtime"));
  TextField config5 = new TextField(
      getTranslation("appjars.configmanager.demo.currentconfigurationsview.valuesystemboolfalse"));
  TextField config6 = new TextField(
      getTranslation("appjars.configmanager.demo.currentconfigurationsview.valuesystemdecimal"));

  Span snapshotCapturedAt = new Span();
  TextField snapshot1 = new TextField(
      getTranslation("appjars.configmanager.demo.currentconfigurationsview.valuesystemstring"));
  TextField snapshot2 = new TextField(
      getTranslation("appjars.configmanager.demo.currentconfigurationsview.valuesysteminteger"));
  TextField snapshot3 = new TextField(
      getTranslation("appjars.configmanager.demo.currentconfigurationsview.valuesystemdate"));
  TextField snapshot4 = new TextField(
      getTranslation("appjars.configmanager.demo.currentconfigurationsview.valuesystemtime"));
  TextField snapshot5 = new TextField(
      getTranslation("appjars.configmanager.demo.currentconfigurationsview.valuesystemboolfalse"));
  TextField snapshot6 = new TextField(
      getTranslation("appjars.configmanager.demo.currentconfigurationsview.valuesystemdecimal"));

  FormLayout userFl = new FormLayout();

  public CurrentConfigurationsView(UserConfigurationService userService,
      UserProvider principalUserProvider, SystemPropertiesSnapshot systemPropertiesSnapshot) {
    this.userService = userService;
    this.principalUserProvider = principalUserProvider;
    this.systemPropertiesSnapshot = systemPropertiesSnapshot;

    configLayout();
  }

  private void configLayout() {
    config1.setReadOnly(true);
    config2.setReadOnly(true);
    config3.setReadOnly(true);
    config4.setReadOnly(true);
    config5.setReadOnly(true);
    config6.setReadOnly(true);

    snapshot1.setReadOnly(true);
    snapshot2.setReadOnly(true);
    snapshot3.setReadOnly(true);
    snapshot4.setReadOnly(true);
    snapshot5.setReadOnly(true);
    snapshot6.setReadOnly(true);

    prefixLabels("appjars.configmanager.demo.currentconfigurationsview.liveprefix", config1, config2,
        config3, config4, config5, config6);
    prefixLabels("appjars.configmanager.demo.currentconfigurationsview.frozenprefix", snapshot1,
        snapshot2, snapshot3, snapshot4, snapshot5, snapshot6);

    add(sectionHeader("appjars.configmanager.demo.currentconfigurationsview.systemproperties"));
    add(compactParagraph("appjars.configmanager.demo.currentconfigurationsview.intro"));

    VerticalLayout liveColumn = column(
        "appjars.configmanager.demo.currentconfigurationsview.currentsystemproperties",
        "appjars.configmanager.demo.currentconfigurationsview.currentsystempropertiesdescription",
        null, config1, config2, config3, config4, config5, config6);
    // Ids consumed by the guided tour of this view (see DemoTours).
    liveColumn.setId("current-live-column");

    snapshotCapturedAt.getStyle().set("color", "var(--lumo-secondary-text-color)")
        .set("font-size", "var(--lumo-font-size-s)").set("font-style", "italic");
    VerticalLayout frozenColumn =
        column("appjars.configmanager.demo.currentconfigurationsview.singletonproperties",
            "appjars.configmanager.demo.currentconfigurationsview.singletonpropertiesdescription",
            snapshotCapturedAt, snapshot1, snapshot2, snapshot3, snapshot4, snapshot5, snapshot6);
    frozenColumn.setId("current-frozen-column");

    HorizontalLayout systemRow = new HorizontalLayout(liveColumn, frozenColumn);
    systemRow.setWidthFull();
    systemRow.setFlexGrow(1, liveColumn, frozenColumn);
    systemRow.getStyle().set("flex-wrap", "wrap").setPaddingTop("8px");
    add(systemRow);

    Hr separator = new Hr();
    separator.setWidthFull();
    add(separator);

    // User properties: resolved per user through the service API.
    Button loadPropertiesBtn = new Button(
        getTranslation("appjars.configmanager.demo.currentconfigurationsview.loadpropertiesbtn"));
    ComboBox<String> usersCb = new ComboBox<>(
        getTranslation("appjars.configmanager.demo.currentconfigurationsview.userscblabel"));
    usersCb.setPlaceholder(getTranslation("appjars.configmanager.userconfigview.selectUser"));
    ArrayList<String> items = new ArrayList<>(principalUserProvider.getAllUsernames());
    String defaultItemsText = getTranslation(
        "appjars.configmanager.demo.currentconfigurationsview.defaultuserconfigurations");
    items.add(defaultItemsText);
    usersCb.setItems(items);
    usersCb.addValueChangeListener(vce -> loadPropertiesBtn.setEnabled(vce.getValue() != null));
    usersCb.setValue(defaultItemsText);

    loadPropertiesBtn.addClickListener(c -> {
      String username = defaultItemsText.equals(usersCb.getValue()) ? "" : usersCb.getValue();
      loadUserConfigs(username);
    });

    H4 userHeader =
        sectionHeader("appjars.configmanager.demo.currentconfigurationsview.userproperties");
    userHeader.getStyle().set("margin-top", "var(--lumo-space-l)");
    add(userHeader);
    add(compactParagraph(
        "appjars.configmanager.demo.currentconfigurationsview.userpropertiesdescription"));
    HorizontalLayout hl = new HorizontalLayout(usersCb, loadPropertiesBtn);
    hl.setAlignItems(Alignment.END);
    hl.setId("current-user-picker");
    add(hl);
    userFl.setWidthFull();
    add(userFl);
  }

  /** Prepends the translated prefix ("Live"/"Frozen") to each field's label. */
  private void prefixLabels(String prefixKey, TextField... fields) {
    String prefix = getTranslation(prefixKey);
    for (TextField field : fields) {
      field.setLabel(prefix + " " + field.getLabel());
    }
  }

  private H4 sectionHeader(String titleKey) {
    H4 header = new H4(getTranslation(titleKey));
    header.getStyle().set("margin-top", "0");
    return header;
  }

  private Paragraph compactParagraph(String descKey) {
    Paragraph paragraph = new Paragraph(getTranslation(descKey));
    paragraph.getStyle().set("margin-top", "0").set("margin-bottom", "0");
    return paragraph;
  }

  /** A titled column with an optional hint and a single-column form of fields. */
  private VerticalLayout column(String titleKey, String descKey, Component hint,
      Component... fields) {
    VerticalLayout col = new VerticalLayout();
    col.setPadding(false);
    col.setSpacing(false);
    // Width 0 as the flex-basis, so the parent's flexGrow splits the row evenly.
    col.setWidth("0px");
    col.getStyle().set("min-width", "18em");

    VerticalLayout header = new VerticalLayout();
    header.setPadding(false);
    header.setSpacing(false);
    header.setMinHeight("5em");
    H5 title = new H5(getTranslation(titleKey));
    title.getStyle().set("margin-top", "0");
    header.add(title, compactParagraph(descKey));
    if (hint != null) {
      header.add(hint);
    }
    col.add(header);

    FormLayout form = new FormLayout(fields);
    form.setResponsiveSteps(new ResponsiveStep("0", 1));
    form.setWidthFull();
    col.add(form);
    return col;
  }

  @PostConstruct
  public void init() {
    config1.setValue(value1);
    config2.setValue(value2.toString());
    config3.setValue(LocalizedDateTimeFormatter.format(value3, getLocale()));
    config4.setValue(LocalizedDateTimeFormatter.format(value4, getLocale()));
    config5.setValue(value5.toString());
    config6.setValue(value6.toString());

    snapshotCapturedAt
        .setText(
            getTranslation(
                "appjars.configmanager.demo.currentconfigurationsview.singletoncapturedat")
                + " "
                + LocalizedDateTimeFormatter.format(LocalDateTime
                    .ofInstant(systemPropertiesSnapshot.getCapturedAt(), ZoneId.systemDefault()),
                    getLocale()));
    snapshot1.setValue(systemPropertiesSnapshot.getStringValue());
    snapshot2.setValue(systemPropertiesSnapshot.getIntegerValue().toString());
    snapshot3.setValue(
        LocalizedDateTimeFormatter.format(systemPropertiesSnapshot.getDateValue(), getLocale()));
    snapshot4.setValue(
        LocalizedDateTimeFormatter.format(systemPropertiesSnapshot.getTimeValue(), getLocale()));
    snapshot5.setValue(systemPropertiesSnapshot.getBooleanValue().toString());
    snapshot6.setValue(systemPropertiesSnapshot.getDecimalValue().toString());

    loadUserConfigs("");
  }

  /** Rebuilds the user-properties form from the configurations currently in the database. */
  private void loadUserConfigs(String username) {
    userFl.removeAll();
    List<UserConfigurationDto> configs = userService.findAll();
    configs.sort(Comparator.comparing(Configuration::getName,
        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
    if (configs.isEmpty()) {
      userFl.add(compactParagraph(
          "appjars.configmanager.demo.currentconfigurationsview.userpropertiesempty"));
      return;
    }
    for (UserConfigurationDto config : configs) {
      TextField field = new TextField(config.getName());
      field.setReadOnly(true);
      // findConfigValueByUser falls back to the config's default value when the user has no value.
      field.setValue(userService.findConfigValueByUser(config.getName(), username)
          .map(value -> formatConfigValue(value.getValue())).orElse(""));
      userFl.add(field);
    }
  }

  /** Localizes date and time values; other types render as-is. */
  private String formatConfigValue(Object value) {
    if (value instanceof LocalDate date) {
      return LocalizedDateTimeFormatter.format(date, getLocale());
    }
    if (value instanceof LocalTime time) {
      return LocalizedDateTimeFormatter.format(time, getLocale());
    }
    return String.valueOf(value);
  }

  public void beforeEnter(BeforeEnterEvent event) {
    if (event.isRefreshEvent()) {
      init();
    }
  }

  @Override
  public String getPageTitle() {
    return getTranslation("appjars.configmanager.demo.currentconfigurationsview.title");
  }
}
