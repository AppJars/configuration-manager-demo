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

import com.appjars.configuration.demo.views.tour.DemoTours;
import com.appjars.configuration.demo.views.tour.DemoTours.DemoTour;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

/** Fake login screen: the accounts are pre-created, so the visitor just picks a role. */
@SuppressWarnings("serial")
@Route(value = "login")
public class LoginDemoView extends VerticalLayout implements HasDynamicTitle {

  private static final String KEY_PREFIX = "appjars.configmanager.demo.loginview.";

  public LoginDemoView() {
    addClassName("login-view");
    setSizeFull();
    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.CENTER);

    H1 title = new H1(getTranslation(KEY_PREFIX + "title"));
    title.addClassName("login-title");
    Paragraph intro = new Paragraph(getTranslation(KEY_PREFIX + "intro"));
    intro.addClassName("login-intro");

    Div cards = new Div(roleCard(VaadinIcon.USER_STAR, "admin", "ADMIN"),
        roleCard(VaadinIcon.USER, "user", "USER"));
    cards.addClassName("login-card-row");

    Div container = new Div(title, intro, cards);
    container.addClassName("login-container");
    add(container);
  }

  private Card roleCard(VaadinIcon icon, String key, String username) {
    Card card = new Card();
    card.addClassName("login-card");
    Icon prefix = icon.create();
    prefix.addClassName("login-card-icon");
    card.setHeaderPrefix(prefix);
    card.setTitle(getTranslation(KEY_PREFIX + key + ".title"));
    card.add(new Paragraph(getTranslation(KEY_PREFIX + key + ".desc")));

    Button loginBtn = new Button(getTranslation(KEY_PREFIX + key + ".button"), e -> login(username));
    loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    loginBtn.setWidthFull();
    card.add(loginBtn);
    return card;
  }

  private void login(String username) {
    VaadinSession.getCurrent().getSession().setAttribute("loggedUsername", username);
    VaadinSession.getCurrent().getSession().setAttribute("loggedRole", username);
    // Resume a guided tour requested from the landing page; otherwise land on the home page.
    if (VaadinSession.getCurrent()
        .getAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE) instanceof DemoTour pending) {
      UI.getCurrent().navigate(pending.getViewClass());
    } else {
      UI.getCurrent().navigate(HomeView.class);
    }
  }

  @Override
  public String getPageTitle() {
    return getTranslation(KEY_PREFIX + "title");
  }
}
