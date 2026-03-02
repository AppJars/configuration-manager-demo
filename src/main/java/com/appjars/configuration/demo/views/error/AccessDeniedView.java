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
package com.appjars.configuration.demo.views.error;

import com.appjars.configuration.demo.views.MainLayout;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AccessDeniedException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@AnonymousAllowed
@ParentLayout(MainLayout.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccessDeniedView extends VerticalLayout
    implements HasErrorParameter<AccessDeniedException> {

  static final String KEY_TITLE = "appjars.configmanager.demo.accessdenied.title";
  static final String KEY_DESC = "appjars.configmanager.demo.accessdenied.desc";

  @Override
  public int setErrorParameter(BeforeEnterEvent beforeEnterEvent,
      ErrorParameter<AccessDeniedException> errorParameter) {
    this.addClassNames(AlignItems.CENTER, JustifyContent.CENTER);
    H3 title = new H3(getTranslation(KEY_TITLE));
    add(title, new Text(getTranslation(KEY_DESC)));
    return HttpServletResponse.SC_FORBIDDEN;
  }
}
