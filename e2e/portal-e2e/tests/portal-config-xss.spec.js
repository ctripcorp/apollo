/*
 * Copyright 2026 Apollo Authors
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
const { test, expect } = require('@playwright/test');
const fs = require('node:fs/promises');
const path = require('node:path');

const staticRoot = path.resolve(__dirname, '../../../apollo-portal/src/main/resources/static');
const appId = 'xss-regression-missing-app';
const payload = '<script>window.clusterXssHits.push("script")</script>';
const customTip = 'div[ng-show="pageContext.clusterName != \'default\'"] span';
const defaultTip = 'div[ng-show="pageContext.clusterName == \'default\'"] span';
const contentTypes = {
  '.html': 'text/html', '.js': 'application/javascript', '.json': 'application/json',
  '.css': 'text/css', '.png': 'image/png', '.svg': 'image/svg+xml',
};

// Serve the real Portal page and vendor scripts. The app deliberately does not exist:
// neither a successful backend lookup nor a visible tip is required for DOM rendering.
async function servePortal(page) {
  await page.addInitScript(() => { window.clusterXssHits = []; });
  await page.route('**/*', async route => {
    const { pathname } = new URL(route.request().url());
    if (pathname === '/openapi/v1/user' || pathname === '/user') {
      await route.fulfill({ json: { userId: 'test-user' } });
      return;
    }
    if (pathname === '/prefix-path') {
      await route.fulfill({ contentType: 'text/plain', body: '' });
      return;
    }
    const file = path.resolve(staticRoot, '.' + pathname);
    if (file.startsWith(staticRoot + path.sep)) {
      try {
        const body = await fs.readFile(file);
        await route.fulfill({ body, contentType: contentTypes[path.extname(file)]
            || 'application/octet-stream' });
        return;
      } catch (error) {
        if (error.code !== 'ENOENT' && error.code !== 'ENOTDIR') {
          throw error;
        }
      }
    }
    await route.fulfill({ status: 404, json: { message: 'Not found' } });
  });
}

async function openPage(page, cluster) {
  await page.goto('/config.html?#/appid=' + appId + '&env=LOCAL'
      + (cluster === undefined ? '' : '&cluster=' + encodeURIComponent(cluster)));
  await page.waitForFunction(() => {
    const injector = window.angular && angular.element(document.documentElement).injector();
    return injector && injector.get('$rootScope').pageContext && injector.get('$translate').isReady();
  });
}

async function scene(page) {
  return page.evaluate(id => ({
    cluster: angular.element(document.documentElement).injector().get('$rootScope').pageContext.clusterName,
    saved: JSON.parse(sessionStorage.getItem(id)).cluster,
    hits: window.clusterXssHits,
  }), appId);
}

async function renderTip(page, language, cluster, clusterNames) {
  await page.evaluate(async ({ language, cluster, clusterNames }) => {
    const injector = angular.element(document.documentElement).injector();
    await injector.get('$translate').use(language);
    const root = injector.get('$rootScope');
    root.$apply(() => {
      root.pageContext.clusterName = cluster;
      root.envMapClusters = { LOCAL: clusterNames };
    });
  }, { language, cluster, clusterNames });
}

test.describe('@regression Portal cluster tip XSS', () => {
  test.beforeEach(async ({ page }) => { await servePortal(page); });

  test('renders a malicious URL cluster as inert text even when the app is missing', async ({ page }) => {
    await openPage(page, payload);
    expect(await scene(page)).toEqual({ cluster: payload, saved: payload, hits: [] });
    await expect(page.locator(customTip)).toContainText(payload);
    await expect(page.locator(customTip + ' script, ' + customTip + ' img')).toHaveCount(0);
    // A subsequent visit in the same tab replays the saved payload as inert text.
    await page.goto('/config.html?revisit#/appid=' + appId + '&env=LOCAL');
    await page.waitForFunction(() => angular.element(document.documentElement).injector()
        .get('$translate').isReady());
    expect(await scene(page)).toEqual({ cluster: payload, saved: payload, hits: [] });
    await expect(page.locator(customTip)).toContainText(payload);
    await expect(page.locator(customTip + ' script, ' + customTip + ' img')).toHaveCount(0);
  });

  test('renders a malicious cluster restored from sessionStorage as inert text', async ({ page }) => {
    await page.addInitScript(({ id, cluster }) => {
      if (window === window.top) {
        sessionStorage.setItem(id, JSON.stringify({ env: 'LOCAL', cluster }));
      }
    }, { id: appId, cluster: payload });
    await openPage(page);
    expect(await scene(page)).toEqual({ cluster: payload, saved: payload, hits: [] });
    await expect(page.locator(customTip)).toContainText(payload);
    await expect(page.locator(customTip + ' script, ' + customTip + ' img')).toHaveCount(0);
  });

  for (const language of ['en', 'zh-CN']) {
    for (const cluster of ['default', 'some.cluster-_name.123']) {
      test(language + ' renders the ' + cluster + ' tip as readable text', async ({ page }) => {
        await openPage(page, cluster);
        await renderTip(page, language, cluster, 'cluster-a,cluster-b');
        const dictionary = JSON.parse(await fs.readFile(path.join(staticRoot, 'i18n', language + '.json')));
        const key = cluster === 'default' ? 'Config.ClusterIsDefaultTipContent' : 'Config.ClusterIsCustomTipContent';
        const name = cluster === 'default' ? 'cluster-a,cluster-b' : cluster;
        await expect(page.locator(cluster === 'default' ? defaultTip : customTip))
            .toHaveText(dictionary[key].replace(/\{\{name\}\}/g, name));
        expect((await scene(page)).hits).toEqual([]);
      });
    }

    test(language + ' escapes HTML at both tip sinks independently of input validation', async ({ page }) => {
      await openPage(page, 'default');
      const html = payload + '<img src="/missing-image" onerror="window.clusterXssHits.push(\'image\')">';
      await renderTip(page, language, html, html);
      await expect(page.locator(customTip)).toContainText(html);
      await expect(page.locator(defaultTip)).toContainText(html);
      await expect(page.locator(customTip + ' script, ' + customTip + ' img, '
          + defaultTip + ' script, ' + defaultTip + ' img')).toHaveCount(0);
      expect((await scene(page)).hits).toEqual([]);
    });
  }
});
