// Screenshot gate for warden. Serves a static site directory and captures it at
// desktop and mobile widths in both themes, cache busted. Exits non-zero if any
// shot fails, so it can be used as a blocking gate rather than an advisory one.
//
// Usage: node qa-shots.mjs <site-dir> [out-dir] [port]
// Needs playwright resolvable, e.g. NODE_PATH=$HOME/qa/node_modules

import { spawn } from 'node:child_process';
import { mkdir } from 'node:fs/promises';
import { chromium } from 'playwright';

const [siteDir, outDir = '/tmp/qa-shots', port = '8099'] = process.argv.slice(2);

if (!siteDir) {
  console.error('usage: node qa-shots.mjs <site-dir> [out-dir] [port]');
  process.exit(2);
}

const VIEWPORTS = [
  { name: 'desktop', width: 1440, height: 900 },
  { name: 'mobile', width: 390, height: 844 },
];
const THEMES = ['dark', 'light'];

// ponytail: python3 -m http.server instead of a static-server dependency. It is
// already on the box and this only ever serves a local directory.
const server = spawn('python3', ['-m', 'http.server', port, '--directory', siteDir], {
  stdio: 'ignore',
});
const stopServer = () => server.kill();
process.on('exit', stopServer);

await new Promise((r) => setTimeout(r, 1500));
await mkdir(outDir, { recursive: true });

const browser = await chromium.launch();
const failures = [];

for (const vp of VIEWPORTS) {
  for (const theme of THEMES) {
    const label = `${vp.name}-${theme}`;
    try {
      const context = await browser.newContext({
        viewport: { width: vp.width, height: vp.height },
        colorScheme: theme,
        deviceScaleFactor: 2,
        bypassCSP: true,
      });
      // Sites here key off a data-theme attribute rather than the media query, and
      // most persist the choice. Set both before any script runs.
      // ponytail: writes every *-theme key it finds rather than taking a per-site
      // config. If a site uses a stranger key, pass it in then.
      await context.addInitScript((mode) => {
        try {
          for (const k of Object.keys(localStorage)) {
            if (/theme/i.test(k)) localStorage.setItem(k, mode);
          }
          localStorage.setItem('theme', mode);
        } catch {}
        document.addEventListener('DOMContentLoaded', () => {
          document.documentElement.setAttribute('data-theme', mode);
        });
      }, theme);

      const page = await context.newPage();
      const errors = [];
      page.on('pageerror', (e) => errors.push(String(e)));

      const url = `http://127.0.0.1:${port}/?cb=${Date.now()}`;
      const res = await page.goto(url, { waitUntil: 'networkidle', timeout: 30000 });
      if (!res || !res.ok()) throw new Error(`HTTP ${res ? res.status() : 'no response'}`);

      // Re-assert after load, in case the site's own theme script ran last.
      await page.evaluate((mode) => {
        document.documentElement.setAttribute('data-theme', mode);
      }, theme);

      // Reveal-on-scroll sections start at opacity 0 and are only shown once an
      // IntersectionObserver fires. A fullPage screenshot does not scroll, so
      // without this pass everything below the hero photographs as blank and the
      // gate would reject good work. Scroll the whole page, then return to top.
      await page.evaluate(async () => {
        // body.scrollHeight is not the page height on every layout, it stops short
        // when the scrolling element is documentElement. Take the max and
        // re-measure each step, since revealed content changes the height.
        const pageHeight = () =>
          Math.max(
            document.body.scrollHeight,
            document.documentElement.scrollHeight,
            document.body.offsetHeight,
            document.documentElement.offsetHeight
          );
        const step = Math.round(window.innerHeight * 0.8);
        let y = 0;
        let guard = 0;
        while (y < pageHeight() && guard++ < 500) {
          window.scrollTo(0, y);
          await new Promise((r) => setTimeout(r, 120));
          y += step;
        }
        window.scrollTo(0, pageHeight());
        await new Promise((r) => setTimeout(r, 400));
        window.scrollTo(0, 0);
      });
      await page.waitForTimeout(1200);

      // Only count elements that are actually laid out. A .reveal that is
      // display:none at this width (a desktop-only variant on mobile, say) still
      // reports opacity 0 and is not a defect.
      const hidden = await page.evaluate(
        () =>
          [...document.querySelectorAll('[class*="reveal"]')].filter((el) => {
            const s = getComputedStyle(el);
            if (s.display === 'none' || s.visibility === 'hidden') return false;
            const r = el.getBoundingClientRect();
            if (r.width === 0 || r.height === 0) return false;
            return s.opacity === '0';
          }).length
      );

      const path = `${outDir}/${label}.png`;
      await page.screenshot({ path, fullPage: true });

      const applied = await page.evaluate(() => document.documentElement.getAttribute('data-theme'));
      console.log(
        `ok   ${label} -> ${path} (data-theme=${applied}, ${hidden} still-hidden reveal elements${errors.length ? `, ${errors.length} page errors` : ''})`
      );
      if (errors.length) failures.push(`${label}: ${errors[0]}`);
      // A shot full of invisible sections is worse than no shot: it looks like a
      // rendered page and is not one.
      if (hidden > 0) failures.push(`${label}: ${hidden} reveal elements still at opacity 0`);
      await context.close();
    } catch (err) {
      console.log(`FAIL ${label}: ${err.message}`);
      failures.push(`${label}: ${err.message}`);
    }
  }
}

await browser.close();
stopServer();

if (failures.length) {
  console.error(`\n${failures.length} failure(s):`);
  for (const f of failures) console.error(`  - ${f}`);
  process.exit(1);
}
console.log(`\nAll ${VIEWPORTS.length * THEMES.length} shots captured in ${outDir}`);
