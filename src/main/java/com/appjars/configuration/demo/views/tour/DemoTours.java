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
package com.appjars.configuration.demo.views.tour;

import com.appjars.configuration.demo.views.CurrentConfigurationsView;
import com.appjars.configurationmanager.flow.view.MyConfigurationsView;
import com.appjars.configurationmanager.flow.view.SystemConfigurationsListView;
import com.appjars.configurationmanager.flow.view.UsersConfigurationsListView;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.vaadin.addons.antlerflow.tour.EngineType;
import org.vaadin.addons.antlerflow.tour.Tour;
import org.vaadin.addons.antlerflow.tour.TourButton;
import org.vaadin.addons.antlerflow.tour.TourButtonType;
import org.vaadin.addons.antlerflow.tour.TourStep;

/**
 * Factory of the guided tours offered by the demo. The engine is always
 * {@link EngineType#DRIVER}, so every client-side helper below targets Driver's DOM.
 */
public final class DemoTours {

  /** Session attribute used to start a tour after navigating (and logging in) to its view. */
  public static final String PENDING_TOUR_ATTRIBUTE = DemoTours.class.getName() + ".pendingTour";

  static final String KEY_PREFIX = "appjars.configmanager.demo.tour.";

  /** Marker attribute the client-side resolver places on the element a step must point at. */
  private static final String TARGET_ATTR = "data-antler-target";

  /**
   * Anchors every step to the first <em>visible</em> match of its selector: the grids render their
   * filter fields twice and a raw {@code querySelector} can return the hidden duplicate.
   *
   * <p>{@code $0} is a JSON map of {@code {stepId: {sel: cssSelector, text: exactTextOrNull}}}.
   */
  private static final String RESOLVE_TARGETS_JS =
      """
      const MAP = JSON.parse($0);
      const ATTR = 'data-antler-target';
      const resolve = () => {
        Object.keys(MAP).forEach(id => {
          const spec = MAP[id];
          let pick = null;
          for (const el of document.querySelectorAll(spec.sel)) {
            const r = el.getBoundingClientRect();
            if (r.width <= 4 || r.height <= 4) { continue; }
            if (spec.text && el.textContent.trim() !== spec.text) { continue; }
            pick = el;
            break;
          }
          document.querySelectorAll("[" + ATTR + "='" + id + "']")
              .forEach(el => { if (el !== pick) { el.removeAttribute(ATTR); } });
          if (pick && pick.getAttribute(ATTR) !== id) { pick.setAttribute(ATTR, id); }
        });
      };
      if (window.__antlerResolver) { window.__antlerResolver.stop(); }
      let scheduled = false;
      const schedule = () => {
        if (scheduled) { return; }
        scheduled = true;
        requestAnimationFrame(() => { scheduled = false; resolve(); });
      };
      resolve();
      const obs = new MutationObserver(schedule);
      obs.observe(document.body, {childList: true, subtree: true, attributes: true,
          attributeFilter: ['hidden', 'style', 'class']});
      window.__antlerResolver = {
        stop() {
          obs.disconnect();
          document.querySelectorAll('[' + ATTR + ']').forEach(el => el.removeAttribute(ATTR));
          window.__antlerResolver = null;
        }
      };
      """;

  /**
   * Undoes Driver's {@code overflow: hidden} on the highlighted element's parent, which otherwise
   * clips the controls sitting next to it in the grid toolbar.
   */
  private static final String TOUR_CSS_JS =
      """
      if (!document.getElementById('demo-tour-css')) {
        const style = document.createElement('style');
        style.id = 'demo-tour-css';
        style.textContent =
            'body :not(body):has(> .driver-active-element) { overflow: visible !important; }';
        document.head.appendChild(style);
      }
      """;

  /**
   * Promotes the Driver popover into the browser top layer while armed, so it keeps painting above
   * the native-popover overlay the per-row actions menu opens. Armed for that step only
   * ({@link #ROW_MENU_JS}), so any other overlay keeps its own controls reachable.
   */
  private static final String PROMOTE_TOP_LAYER_JS =
      """
      if (window.__demoTourTopLayer) { window.__demoTourTopLayer.stop(); }
      const promote = () => document.querySelectorAll('.driver-popover').forEach(el => {
        if (el.getAttribute('popover') !== 'manual') { el.setAttribute('popover', 'manual'); }
        el.style.margin = '0';
        try { if (!el.matches(':popover-open')) { el.showPopover(); } } catch (e) {}
      });
      const demote = () => document.querySelectorAll('.driver-popover[popover]').forEach(el => {
        try { el.hidePopover(); } catch (e) {}
        el.removeAttribute('popover');
        el.style.removeProperty('margin');
      });
      const reassert = () => {
        const el = document.querySelector('.driver-popover');
        if (el && el.matches(':popover-open')) {
          try { el.hidePopover(); el.showPopover(); } catch (e) {}
        }
      };
      const onToggle = (e) => {
        const t = e.target;
        if (e.newState === 'open' && t && t.classList
            && !t.classList.contains('driver-popover')) { reassert(); }
      };
      const tick = () => {
        window.dispatchEvent(new Event('resize'));
        frame = requestAnimationFrame(tick);
      };
      let obs = null;
      let frame = 0;
      window.__demoTourTopLayer = {
        arm() {
          if (obs) { return; }
          document.addEventListener('toggle', onToggle, true);
          obs = new MutationObserver(promote);
          obs.observe(document.body, {childList: true, subtree: true});
          promote();
          frame = requestAnimationFrame(tick);
        },
        disarm() {
          if (!obs) { return; }
          obs.disconnect();
          obs = null;
          cancelAnimationFrame(frame);
          document.removeEventListener('toggle', onToggle, true);
          demote();
        },
        stop() {
          this.disarm();
          window.__demoTourTopLayer = null;
        }
      };
      """;

  /**
   * Opens the per-row actions ({@code ⋮}) menu while a step about it is showing, so the visitor sees
   * the entries the step describes, and closes it again when the tour moves on. {@code $0} is the
   * JSON array of the step ids that open the menu.
   *
   * <p>Several elements can carry {@code .driver-active-element} at once (Driver clears the class
   * off the previous one only when its transition completes, which a mutating grid interrupts), so
   * the current one is the element the latest {@code class} mutation marked.
   */
  private static final String ROW_MENU_JS =
      """
      if (window.__demoTourRowMenu) { window.__demoTourRowMenu.stop(); }
      const IDS = JSON.parse($0);
      const ACTIVE = 'driver-active-element';
      const topLayer = () => window.__demoTourTopLayer;
      const closeMenu = () => document
          .querySelectorAll('vaadin-grid-cell-content vaadin-menu-bar-button[aria-expanded="true"]')
          .forEach(b => b.click());
      let marked = null;
      const activeElement = () => marked && marked.classList.contains(ACTIVE) ? marked
          : document.querySelector('.' + ACTIVE);
      const openMenu = () => {
        const active = activeElement();
        if (!active) { return; }
        const btn = active.querySelector('vaadin-menu-bar-button:not([slot="overflow"])');
        if (btn && btn.getAttribute('aria-expanded') !== 'true') { btn.click(); }
      };
      let current = null;
      const sync = (records) => {
        (records || []).forEach(rec => {
          if (rec.type === 'attributes' && rec.attributeName === 'class'
              && rec.target.classList.contains(ACTIVE)) { marked = rec.target; }
        });
        const active = activeElement();
        const id = active ? active.getAttribute('data-antler-target') : null;
        if (id === current) { return; }
        current = id;
        closeMenu();
        if (id && IDS.includes(id)) {
          if (topLayer()) { topLayer().arm(); }
          setTimeout(openMenu, 200);
        } else if (topLayer()) {
          topLayer().disarm();
        }
      };
      const obs = new MutationObserver(sync);
      obs.observe(document.body, {childList: true, subtree: true, attributes: true,
          attributeFilter: ['class', 'data-antler-target']});
      sync();
      window.__demoTourRowMenu = {
        stop() { obs.disconnect(); closeMenu(); window.__demoTourRowMenu = null; }
      };
      """;

  /**
   * Ends the tour as soon as a modal dialog opens: the two contend for the browser top layer, and
   * whichever ends up underneath becomes unreachable behind Vaadin's modality curtain.
   *
   * <p>A dialog is matched on the {@code opened} attribute of a {@code *-dialog} or
   * {@code *-dialog-overlay} element, which excludes the non-modal overlays (the row-actions menu
   * the tour opens itself, combo-box dropdowns).
   */
  private static final String MODAL_GUARD_JS =
      """
      if (window.__demoTourModalGuard) { window.__demoTourModalGuard.stop(); }
      const guard = this;
      const isModal = (el) => {
        const name = el.localName || '';
        return name.endsWith('-dialog') || name.endsWith('-dialog-overlay');
      };
      const anyModal = () => Array.from(document.querySelectorAll('[opened]')).some(isModal);
      const check = () => {
        if (!window.__demoTourModalGuard || !anyModal()) { return; }
        window.__demoTourModalGuard.stop();
        guard.$server.modalOpened();
      };
      let scheduled = 0;
      const schedule = () => {
        if (scheduled) { return; }
        scheduled = setTimeout(() => { scheduled = 0; check(); }, 0);
      };
      const obs = new MutationObserver(schedule);
      obs.observe(document.body, {childList: true, subtree: true, attributes: true,
          attributeFilter: ['opened']});
      window.__demoTourModalGuard = {
        stop() {
          obs.disconnect();
          clearTimeout(scheduled);
          window.__demoTourModalGuard = null;
        }
      };
      """;

  /** Tears down every {@code window.__*} helper installed for the tour. */
  private static final String STOP_JS =
      """
      if (window.__antlerResolver) { window.__antlerResolver.stop(); }
      if (window.__demoTourTopLayer) { window.__demoTourTopLayer.stop(); }
      if (window.__demoTourRowMenu) { window.__demoTourRowMenu.stop(); }
      if (window.__demoTourModalGuard) { window.__demoTourModalGuard.stop(); }
      const css = document.getElementById('demo-tour-css');
      if (css) { css.remove(); }
      """;

  /** Guided tours, one per navigable view; each carries the view it navigates to and runs on. */
  public enum DemoTour {
    SYSTEM_CONFIGURATIONS(SystemConfigurationsListView.class),
    USERS_CONFIGURATIONS(UsersConfigurationsListView.class),
    MY_CONFIGURATIONS(MyConfigurationsView.class),
    CURRENT_CONFIGURATIONS(CurrentConfigurationsView.class);

    private final Class<? extends Component> viewClass;

    DemoTour(Class<? extends Component> viewClass) {
      this.viewClass = viewClass;
    }

    public Class<? extends Component> getViewClass() {
      return viewClass;
    }
  }

  /**
   * A tour step. The step attaches to the marker the resolver places on the first visible match of
   * {@code selector}; a {@code null} selector renders the step centered. {@code textKey} narrows
   * the match to the element with exactly that text, needed for a grid column header.
   */
  private record StepDef(String key, String selector, String textKey, String position, boolean first,
      boolean last) {

    StepDef(String key, String selector, String position, boolean first, boolean last) {
      this(key, selector, null, position, first, last);
    }

    /** Whether this step targets the per-row actions menu. */
    boolean opensRowMenu() {
      return ROW_ACTIONS.equals(selector);
    }
  }

  private DemoTours() {}

  public static Tour create(DemoTour tour, SerializableFunction<String, String> translator) {
    return build(steps(tour), translator);
  }

  /**
   * Creates the tour, attaches it to {@code host} and starts it, detaching it again once it is
   * completed or canceled.
   */
  public static void start(DemoTour tour, Component host,
      SerializableFunction<String, String> translator) {
    start(tour, host, translator, () -> {}, () -> {});
  }

  /**
   * Same as {@link #start(DemoTour, Component, SerializableFunction)}, running {@code onStart} once
   * the tour has started and {@code onStop} when it is completed or canceled.
   */
  public static void start(DemoTour tour, Component host,
      SerializableFunction<String, String> translator, Runnable onStart, Runnable onStop) {
    List<StepDef> defs = steps(tour);
    Tour t = build(defs, translator);
    host.getElement().appendChild(t.getElement());
    AtomicBoolean stopped = new AtomicBoolean();
    TourGuard guard =
        new TourGuard(g -> cancelForModal(t, g, host, translator, onStop, stopped));
    host.getElement().appendChild(guard.getElement());
    host.getElement().executeJs(TOUR_CSS_JS);
    host.getElement().executeJs(RESOLVE_TARGETS_JS, targetJson(defs, translator));
    t.addTourCompletedListener(e -> stop(t, guard, host, onStop, stopped));
    t.addTourCanceledListener(e -> stop(t, guard, host, onStop, stopped));
    t.start();
    guard.getElement().executeJs(MODAL_GUARD_JS);
    List<StepDef> rowMenuSteps = defs.stream().filter(StepDef::opensRowMenu).toList();
    if (!rowMenuSteps.isEmpty()) {
      host.getElement().executeJs(PROMOTE_TOP_LAYER_JS);
      host.getElement().executeJs(ROW_MENU_JS, stepIdJson(rowMenuSteps));
    }
    onStart.run();
  }

  /** Undoes everything {@link #start} put in place. Runs at most once. */
  private static void stop(Tour tour, TourGuard guard, Component host, Runnable onStop,
      AtomicBoolean stopped) {
    if (!stopped.compareAndSet(false, true)) {
      return;
    }
    onStop.run();
    host.getElement().executeJs(STOP_JS);
    tour.getElement().removeFromParent();
    guard.getElement().removeFromParent();
  }

  /**
   * Ends the tour because a modal dialog opened over it, and tells the visitor why. The teardown is
   * run here because a tour destroyed mid-transition leaves no canceled event behind.
   */
  private static void cancelForModal(Tour tour, TourGuard guard, Component host,
      SerializableFunction<String, String> translator, Runnable onStop, AtomicBoolean stopped) {
    Notification notification = Notification.show(translator.apply(KEY_PREFIX + "canceled.modal"),
        6000, Notification.Position.BOTTOM_END);
    notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
    tour.cancel();
    stop(tour, guard, host, onStop, stopped);
  }

  /**
   * {@code disableActiveInteraction} makes the highlighted control inert for the duration of its
   * step, so the visitor cannot open a dialog that would deadlock against the tour chrome.
   */
  private static Tour build(List<StepDef> defs,
      SerializableFunction<String, String> translator) {
    List<TourStep> steps = defs.stream().map(def -> step(translator, def)).toList();
    return Tour.builder().engineType(EngineType.DRIVER).steps(steps).showCancelButton(true)
        .allowClose(true).options(Map.of("disableActiveInteraction", true)).build();
  }

  /**
   * Invisible companion of the tour: gives {@link #MODAL_GUARD_JS} the {@code $server} endpoint it
   * needs to report that a modal dialog has opened.
   */
  @Tag("demo-tour-guard")
  private static final class TourGuard extends Component {

    private static final long serialVersionUID = 1L;

    private final SerializableConsumer<TourGuard> onModalOpened;

    TourGuard(SerializableConsumer<TourGuard> onModalOpened) {
      this.onModalOpened = onModalOpened;
    }

    @ClientCallable
    public void modalOpened() {
      onModalOpened.accept(this);
    }
  }

  private static List<StepDef> steps(DemoTour tour) {
    return switch (tour) {
      case SYSTEM_CONFIGURATIONS -> systemConfigurationsSteps();
      case USERS_CONFIGURATIONS -> usersConfigurationsSteps();
      case MY_CONFIGURATIONS -> myConfigurationsSteps();
      case CURRENT_CONFIGURATIONS -> currentConfigurationsSteps();
    };
  }

  /** The per-row actions ({@code ⋮}) menu bar of a grid row. */
  private static final String ROW_ACTIONS = "vaadin-grid-cell-content vaadin-menu-bar";

  /** The filter row of the users/my configurations views, which carries no id of its own. */
  private static final String USER_FILTERS =
      "vaadin-horizontal-layout:has(> #name-filter-textfield)";

  /** i18n key of the "Value" grid column header, used to single out that column. */
  private static final String USER_VALUE_HEADER_KEY = "appjars.configmanager.userconfigview.value";

  // Steps on a toolbar button are placed below it, so the step box does not cover it.
  private static List<StepDef> systemConfigurationsSteps() {
    return List.of(
        new StepDef("system.intro", null, null, true, false),
        new StepDef("system.grid", "#config-grid", "top", false, false),
        new StepDef("system.filters", "#top-filters", "bottom", false, false),
        new StepDef("system.new", "#new-group-button", "bottom", false, false),
        new StepDef("system.reload", "#reload-context-button", "bottom", false, false),
        new StepDef("system.actions", ROW_ACTIONS, "top", false, false),
        new StepDef("system.license", null, null, false, true));
  }

  // The user picker is the only combo box in the view with no id, hence the negated selector.
  private static List<StepDef> usersConfigurationsSteps() {
    return List.of(
        new StepDef("users.intro", null, null, true, false),
        // Beside the picker, so its dropdown stays visible if the visitor opens it.
        new StepDef("users.user", "#user-config-list-view vaadin-combo-box:not(#type-filter-combobox)",
            "right", false, false),
        new StepDef("users.grid", "#config-grid", "top", false, false),
        new StepDef("users.filters", USER_FILTERS, "bottom", false, false),
        new StepDef("users.new", "#new-group-button", "bottom", false, false),
        new StepDef("users.actions", ROW_ACTIONS, "top", false, false),
        new StepDef("users.finish", null, null, false, true));
  }

  private static List<StepDef> myConfigurationsSteps() {
    return List.of(
        new StepDef("my.intro", null, null, true, false),
        new StepDef("my.grid", "#config-grid", "top", false, false),
        // Left of the Value header, so the popover does not cover the column it describes.
        new StepDef("my.value", "#config-grid vaadin-grid-cell-content", USER_VALUE_HEADER_KEY,
            "left", false, false),
        new StepDef("my.filters", USER_FILTERS, "bottom", false, false),
        new StepDef("my.actions", ROW_ACTIONS, "top", false, false),
        new StepDef("my.finish", null, null, false, true));
  }

  private static List<StepDef> currentConfigurationsSteps() {
    return List.of(
        new StepDef("current.intro", null, null, true, false),
        new StepDef("current.live", "#current-live-column", "bottom", false, false),
        new StepDef("current.frozen", "#current-frozen-column", "bottom", false, false),
        new StepDef("current.reload", null, null, false, false),
        new StepDef("current.user", "#current-user-picker", "bottom", false, false),
        new StepDef("current.finish", null, null, false, true));
  }

  private static TourStep step(SerializableFunction<String, String> t, StepDef def) {
    List<TourButton> buttons = new ArrayList<>();
    if (!def.first()) {
      buttons.add(TourButton.builder().label(t.apply(KEY_PREFIX + "btn.back")).secondary(true)
          .type(TourButtonType.PREVIOUS).build());
    }
    buttons
        .add(TourButton.builder().label(t.apply(KEY_PREFIX + (def.last() ? "btn.done" : "btn.next")))
            .type(TourButtonType.NEXT).build());
    String id = stepId(def.key());
    String attachTo = def.selector() == null ? null : "[" + TARGET_ATTR + "='" + id + "']";
    return TourStep.builder().id(id).attachTo(attachTo).position(def.position())
        .title(t.apply(KEY_PREFIX + def.key() + ".title"))
        .content(t.apply(KEY_PREFIX + def.key() + ".desc")).buttons(buttons).build();
  }

  private static String stepId(String key) {
    return key.replace('.', '-');
  }

  /** Builds the {@code {stepId: {sel, text}}} map consumed by {@link #RESOLVE_TARGETS_JS}. */
  private static String targetJson(List<StepDef> defs,
      SerializableFunction<String, String> translator) {
    StringBuilder json = new StringBuilder("{");
    for (StepDef def : defs) {
      if (def.selector() == null) {
        continue;
      }
      if (json.length() > 1) {
        json.append(',');
      }
      String text =
          def.textKey() == null ? "null" : '"' + escape(translator.apply(def.textKey())) + '"';
      json.append('"').append(stepId(def.key())).append("\":{\"sel\":\"")
          .append(escape(def.selector())).append("\",\"text\":").append(text).append('}');
    }
    return json.append('}').toString();
  }

  /** Builds the JSON array of step ids consumed by {@link #ROW_MENU_JS}. */
  private static String stepIdJson(List<StepDef> defs) {
    return defs.stream().map(def -> '"' + stepId(def.key()) + '"')
        .collect(Collectors.joining(",", "[", "]"));
  }

  private static String escape(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
