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
import com.appjars.configurationmanager.flow.view.MyConfigurationsView;
import com.appjars.configurationmanager.flow.view.SystemConfigurationsListView;
import com.appjars.configurationmanager.flow.view.UsersConfigurationsListView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AccessDeniedException;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Flex;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@SuppressWarnings("serial")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MainLayout extends AppLayout implements BeforeEnterObserver, AfterNavigationObserver {

  H2 viewTitle;
  SubMenu tourSubMenu;

  final String username;
  final String role;

  public MainLayout() {
    username = (String) VaadinSession.getCurrent().getSession().getAttribute("loggedUsername");
    role = (String) VaadinSession.getCurrent().getSession().getAttribute("loggedRole");

    setPrimarySection(Section.DRAWER);
    addDrawerContent();
    addHeaderContent();
  }

  private void addHeaderContent() {
    DrawerToggle toggle = new DrawerToggle();
    toggle.getElement().setAttribute("aria-label", "Menu toggle");

    viewTitle = new H2();
    viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
    viewTitle.addClassName(Flex.GROW);

    addToNavbar(true, toggle, viewTitle, createTourMenu());
  }

  /** Tour menu in the navigation bar, so a tour can be started from any view. */
  private MenuBar createTourMenu() {
    MenuBar menu = new MenuBar();
    menu.addClassName("navbar-tour-menu");
    menu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
    menu.setOpenOnHover(true);
    tourSubMenu = menu.addItem(new Div(VaadinIcon.MAP_MARKER.create(),
        new Span(tourLabel("button")))).getSubMenu();
    refreshTourMenu();
    return menu;
  }

  /** Rebuilt on every navigation: "This page" is enabled only when the view has a tour. */
  private void refreshTourMenu() {
    tourSubMenu.removeAll();
    MenuItem thisPage =
        tourSubMenu.addItem(tourEntry("thispage", VaadinIcon.CROSSHAIRS), e -> startCurrentTour());
    thisPage.setEnabled(currentViewTour().isPresent());
    tourSubMenu.addSeparator();
    tourSubMenu.addItem(tourEntry("myconfigs", VaadinIcon.USER),
        e -> startTour(DemoTour.MY_CONFIGURATIONS));
    if (isAdmin()) {
      tourSubMenu.addItem(tourEntry("current", VaadinIcon.EYE),
          e -> startTour(DemoTour.CURRENT_CONFIGURATIONS));
      tourSubMenu.addItem(tourEntry("system", VaadinIcon.COG),
          e -> startTour(DemoTour.SYSTEM_CONFIGURATIONS));
      tourSubMenu.addItem(tourEntry("users", VaadinIcon.USERS),
          e -> startTour(DemoTour.USERS_CONFIGURATIONS));
    }
  }

  private Component tourEntry(String key, VaadinIcon icon) {
    Div entry = new Div(icon.create(), new Span(tourLabel(key)));
    entry.addClassName("tour-menu-entry");
    return entry;
  }

  /** The tour labels are shared with the landing page, which owns the {@code home.tour.*} keys. */
  private String tourLabel(String key) {
    return getTranslation("appjars.configmanager.demo.home.tour." + key);
  }

  /** Runs the tour now if its view is showing; otherwise navigates there and runs it on arrival. */
  private void startTour(DemoTour tour) {
    if (getContent() != null && tour.getViewClass().equals(getContent().getClass())) {
      runTour(tour);
    } else {
      VaadinSession.getCurrent().setAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE, tour);
      getUI().ifPresent(ui -> ui.navigate(tour.getViewClass()));
    }
  }

  /** "This page": runs the tour of the view currently shown, if it has one. */
  private void startCurrentTour() {
    currentViewTour().ifPresent(this::runTour);
  }

  private void runTour(DemoTour tour) {
    if (tour == DemoTour.USERS_CONFIGURATIONS) {
      DemoTours.start(tour, this, this::getTranslation, this::selectFirstUser, () -> {});
    } else {
      DemoTours.start(tour, this, this::getTranslation);
    }
  }

  /** The users configurations view is disabled until a user is picked, so the tour picks one. */
  private void selectFirstUser() {
    findUserPicker().filter(ComboBox::isEmpty).ifPresent(
        picker -> picker.getGenericDataView().getItems().findFirst().ifPresent(picker::setValue));
  }

  /** The user picker carries no id, so it is located by the label the appjar gives it. */
  @SuppressWarnings("unchecked")
  private Optional<ComboBox<String>> findUserPicker() {
    if (getContent() == null) {
      return Optional.empty();
    }
    String label = getTranslation("appjars.configmanager.userconfigview.configurationsUser");
    return descendants(getContent()).filter(ComboBox.class::isInstance)
        .map(component -> (ComboBox<String>) component)
        .filter(picker -> label.equals(picker.getLabel())).findFirst();
  }

  private static Stream<Component> descendants(Component component) {
    return component.getChildren()
        .flatMap(child -> Stream.concat(Stream.of(child), descendants(child)));
  }

  /** The tour of the view currently shown, or empty when that view has no tour of its own. */
  private Optional<DemoTour> currentViewTour() {
    if (getContent() == null) {
      return Optional.empty();
    }
    Class<?> current = getContent().getClass();
    return Arrays.stream(DemoTour.values()).filter(tour -> tour.getViewClass().equals(current))
        .findFirst();
  }

  private void addDrawerContent() {
    VerticalLayout drawerLayout = new VerticalLayout();
    drawerLayout.addClassNames(Margin.NONE, Padding.NONE, AlignItems.STRETCH, Gap.XSMALL);
    drawerLayout.setSizeFull();

    Image logo = new Image("/icons/icon.png", null);
    logo.setHeight("5vh");
    logo.setWidth("5vh");

    H3 title = new H3(getTranslation("appjars.configmanager.demo.layout.drawertitle"));

    Header header = new Header(logo, title);
    header.addClassNames(Display.FLEX, Gap.XSMALL, AlignItems.CENTER, Margin.MEDIUM);
    title.addClassName(Flex.GROW);

    Scroller scroller = new Scroller(createNavigation());

    Footer footer = createFooter();
    footer.getStyle().set("padding", "var(--lumo-space-s)");

    drawerLayout.add(header, scroller);
    drawerLayout.expand(scroller);

    addToDrawer(drawerLayout, footer);
  }

  private SideNav createNavigation() {
    SideNav nav = new SideNav();

    SideNavItem homeItem =
        new SideNavItem(getTranslation("appjars.configmanager.demo.menuitem.home"), HomeView.class);
    homeItem.setPrefixComponent(VaadinIcon.HOME.create());
    nav.addItem(homeItem);

    SideNavItem configurationsItem =
        new SideNavItem(getTranslation("appjars.configmanager.demo.menuitem.configurationsItem"));
    configurationsItem.setPrefixComponent(VaadinIcon.TOOLS.create());
    configurationsItem.setExpanded(true);

    configurationsItem
        .addItem(navItem("myConfigurations", VaadinIcon.USER, MyConfigurationsView.class));

    if (isAdmin()) {
      configurationsItem.addItem(
          navItem("currentConfigurations", VaadinIcon.EYE, CurrentConfigurationsView.class));
      configurationsItem.addItem(
          navItem("systemConfigurations", VaadinIcon.COG, SystemConfigurationsListView.class));
      configurationsItem.addItem(
          navItem("userConfigurations", VaadinIcon.USERS, UsersConfigurationsListView.class));
    }

    nav.addItem(configurationsItem);

    return nav;
  }

  private SideNavItem navItem(String key, VaadinIcon icon, Class<? extends Component> view) {
    SideNavItem item =
        new SideNavItem(getTranslation("appjars.configmanager.demo.menuitem." + key), view);
    item.setPrefixComponent(icon.create());
    return item;
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    viewTitle.setText(getCurrentPageTitle());
    refreshTourMenu();
    startPendingTour();
  }

  /** Starts the tour requested from another view, once its target view has rendered. */
  private void startPendingTour() {
    VaadinSession session = VaadinSession.getCurrent();
    if (session.getAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE) instanceof DemoTour pending
        && getContent() != null
        && pending.getViewClass().equals(getContent().getClass())) {
      session.setAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE, null);
      runTour(pending);
    }
  }

  private String getCurrentPageTitle() {
    return getContent() instanceof HasDynamicTitle dynamicTitle ? dynamicTitle.getPageTitle() : "";
  }

  private Footer createFooter() {
    Footer layout = new Footer();
    layout.getStyle().set("padding-inline-start", "var(--lumo-space-s)");

    Optional<String> userOpt = Optional.ofNullable(username);

    if (userOpt.isPresent()) {
      Avatar avatar = new Avatar(username);
      avatar.setAbbreviation(username.substring(0, 1).toUpperCase());
      avatar.setThemeName("xsmall");
      avatar.getElement().setAttribute("tabindex", "-1");

      MenuBar userMenu = new MenuBar();
      userMenu.setThemeName("tertiary-inline contrast");

      MenuItem userName = userMenu.addItem("");
      Div div = new Div();
      div.add(avatar);
      div.add(username);
      div.add(LumoIcon.DROPDOWN.create());
      div.getElement().getStyle().set("display", "flex");
      div.getElement().getStyle().set("align-items", "center");
      div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
      userName.add(div);

      Div signOutContent = new Div(VaadinIcon.SIGN_OUT.create(),
          new Span(getTranslation("appjars.configmanager.demo.layout.signout")));
      signOutContent.getStyle().set("display", "flex").set("align-items", "center")
          .set("gap", "var(--lumo-space-s)");
      userName.getSubMenu().addItem(signOutContent, e -> {
        VaadinSession.getCurrent().getSession().setAttribute("loggedUsername", null);
        VaadinSession.getCurrent().getSession().setAttribute("loggedRole", null);
        getUI().ifPresent(ui -> ui.navigate(LoginDemoView.class));
      });

      layout.add(userMenu);
    } else {
      Button signInBtn =
          new Button(getTranslation("appjars.configmanager.demo.layout.signin"),
              VaadinIcon.SIGN_IN.create());
      signInBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
      Anchor loginLink = new Anchor("login", signInBtn);
      loginLink.getStyle().set("text-decoration", "none");
      layout.add(loginLink);
    }

    return layout;
  }

  public void beforeEnter(BeforeEnterEvent event) {
    // The landing page is the public presentation of the demo: keep it reachable anonymously.
    if (role == null && !HomeView.class.equals(event.getNavigationTarget())) {
      event.forwardTo(LoginDemoView.class);
      return;
    }
    // The appjar leaves view-access control to the final application; this demo restricts the
    // system-wide and per-user configuration lists to administrators.
    Class<?> target = event.getNavigationTarget();
    boolean adminOnlyView = CurrentConfigurationsView.class.equals(target)
        || SystemConfigurationsListView.class.equals(target)
        || UsersConfigurationsListView.class.equals(target);
    if (adminOnlyView && !isAdmin()) {
      event.rerouteToError(AccessDeniedException.class);
    }
  }

  private boolean isAdmin() {
    return "ADMIN".equals(role);
  }

}
