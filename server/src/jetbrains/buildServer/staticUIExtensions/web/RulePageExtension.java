/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.staticUIExtensions.web;

import jetbrains.buildServer.staticUIExtensions.model.Rule;
import jetbrains.buildServer.staticUIExtensions.model.StaticContent;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 16.11.11 19:44
 */
public class RulePageExtension extends SimplePageExtension {
  @NotNull
  private final Rule myRule;

  public RulePageExtension(@NotNull final PagePlaces pagePlaces,
                           @NotNull final PluginDescriptor descriptor,
                           @NotNull final ControllerPaths paths,
                           @NotNull final Rule rule) {
    super(pagePlaces);
    myRule = rule;

    setPlaceId(rule.getPlace());
    setPluginName(descriptor.getPluginName() + rule.getRuleId());


    final StaticContent content = rule.getContent();
    final String html = content.getHTML();
    if (html != null) {
      setIncludeUrl(paths.getResourceControllerPath(html));
    } else {
      setIncludeUrl(paths.getResourceControllerPathEmpty());
    }

    final String js = content.getJS();
    if (js != null) {
      addJsFile(paths.getResourceControllerPath(js));
    }

    final String css = content.getCSS();
    if (css != null) {
      addCssFile(paths.getResourceControllerPath(css));
    }
  }

  @Override
  public boolean isAvailable(@NotNull HttpServletRequest request) {
    if (!super.isAvailable(request)) return false;

    List<String> urls = new ArrayList<String>(4);
    final Object oUrl = request.getAttribute("pageUrl");
    if (oUrl instanceof String) {
      final String url = WebUtil.getPathWithoutContext(request, (String) oUrl);
      if (url.startsWith(WebUtil.HTTP_AUTH_PREFIX)) {
        urls.add(url.substring(WebUtil.HTTP_AUTH_PREFIX.length()));
      }
      if (url.startsWith(WebUtil.GUEST_AUTH_PREFIX)) {
        urls.add(url.substring(WebUtil.GUEST_AUTH_PREFIX.length()));
      }
      urls.add(url);
    }
    urls.add(WebUtil.getPathWithoutAuthenticationType(request));

    for (String _url : urls) {
      String url = _url;
      if (url.startsWith("/")) url = url.substring(1);

      if (myRule.getUrlMatcher().matches(url)) return true;
    }

    return false;
  }
}
