#!/usr/bin/env node
/**
 * CSS 허용 목록 근거 측정 — "빼봤더니 화면이 바뀌나"
 *
 * 어떤 속성을 그 속성을 쓰는 실제 문서에서 지우고, 지우기 전후를 뷰어 CSS 안에서
 * 렌더해 픽셀을 비교한다. 바뀌면 그 속성은 서식을 나르고 있다는 뜻이다.
 * 절차와 판단 근거는 같은 폴더의 README.
 *
 * 프론트 레포(cse.snu.ac.kr) 디렉터리에서 실행한다 — 브라우저(playwright), sharp,
 * 뷰어 CSS 가 거기 있다. 의존성은 현재 디렉터리의 node_modules 에서 찾는다.
 *
 *   node ../csereal-server/docs/css-allowlist/measure-css-impact.mjs <corpus.json> [--samples N] [--out dir/]
 *
 * --out 을 주면 속성마다 가장 많이 바뀐 문서의 before / after / diff(바뀐 픽셀만 빨강) 를 남긴다.
 *
 * corpus.json 형식 (extract-corpus.py 가 만든다):
 *   { "props": { "<속성>": { "total": 문서수, "sample": ["id", ...] } },
 *     "docs":  { "id": "<본문 HTML>" } }
 */
import fs from 'node:fs';
import { createRequire } from 'node:module';
import path from 'node:path';

const require = createRequire(path.join(process.cwd(), 'package.json'));
const { chromium } = require('@playwright/test');
const sharp = require('sharp');

const [corpusPath] = process.argv.slice(2).filter((a) => !a.startsWith('--'));
const flag = (name, def) => {
  const i = process.argv.indexOf(`--${name}`);
  return i === -1 ? def : process.argv[i + 1];
};
const outDir = flag('out', null);
// 기본은 전수. 빠르게 훑어볼 땐 --samples 로 줄인다.
const maxSamples = Number(flag('samples', Number.POSITIVE_INFINITY));
const renderTimeout = Number(flag('timeout', 15000));

// 프론트 레포의 뷰어 CSS. 본문은 이 스타일 안에서 렌더되므로 비교도 그 안에서 해야 한다.
// suneditor 원본 위에 우리 override 를 덧씌운 순서 그대로(HTMLViewer 의 import 순서).
const VIEWER_CSS = flag(
  'viewer-css',
  'node_modules/suneditor/src/assets/css/suneditor-contents.css,src/components/ui/assets/suneditor-contents.override.css',
);

const viewerCss = VIEWER_CSS.split(',')
  .map((f) => fs.readFileSync(f, 'utf8'))
  .join('\n');
const { props, docs } = JSON.parse(fs.readFileSync(corpusPath, 'utf8'));
if (outDir) fs.mkdirSync(outDir, { recursive: true });

/** style 속성에서 그 선언 하나만 지운다. 나머지는 그대로 둔다. */
const strip = (html, prop) =>
  html.replace(/style="([^"]*)"/g, (_, decls) => {
    const kept = decls
      .split(';')
      .filter((d) => {
        const i = d.indexOf(':');
        return i < 1 || d.slice(0, i).trim().toLowerCase() !== prop;
      })
      .join(';');
    return kept.trim() ? `style="${kept}"` : '';
  });

// 본문 칼럼 폭(데스크톱 ~900px)에 맞춘다. 좁으면 줄바꿈 차이가 과장된다.
const page = (body) => `<!doctype html><meta charset=utf8><style>${viewerCss}
 html,body{margin:0;background:#fff}.wrap{width:900px;padding:24px}</style>
<div class="wrap"><div class="sun-editor-editable">${body}</div></div>`;

const browser = await chromium.launch();
const tab = await browser.newPage({
  viewport: { width: 960, height: 800 },
  deviceScaleFactor: 1,
});

const shot = async (html) => {
  await tab.setContent(page(html), {
    waitUntil: 'load',
    timeout: renderTimeout,
  });
  await tab.evaluate(() => document.fonts.ready);
  return tab.locator('.wrap').screenshot({ timeout: renderTimeout });
};

/**
 * 세탁기의 전처리(`ContentSanitizer.prepare`)와 같은 것을 먼저 적용한다.
 *
 * ⚠️ **`strip` 보다 먼저** 해야 한다. 순서가 뒤바뀌면 `display` 를 지운 쪽에서
 * `display:none` 을 못 찾아 숨어 있던 잔재가 드러나고, 그 변화가 통째로
 * `display` 탓으로 잡혀 영향이 부풀려진다.
 *
 * 브라우저에게 시킨다 — `el.style` 은 파싱된 값이라 공백·대소문자를 안 따진다.
 */
const preprocess = async (html) => {
  await tab.setContent(page(html), {
    waitUntil: 'load',
    timeout: renderTimeout,
  });
  return tab.evaluate(() => {
    for (const el of document.querySelectorAll('[style]')) {
      if (el.style.display === 'none') el.remove();
    }
    for (const el of document.querySelectorAll('img:not([src])')) el.remove();
    return document.querySelector('.sun-editor-editable').innerHTML;
  });
};

/**
 * 바뀐 픽셀을 빨갛게 칠한 diff 이미지. 나머지는 before 를 연하게 깐다.
 * 높이가 다르면 짧은 쪽을 흰색으로 채워 맞춘다. 0.1% 짜리 차이는 이걸로만 보인다.
 */
async function diffImage(a, b) {
  const [ma, mb] = await Promise.all([
    sharp(a).metadata(),
    sharp(b).metadata(),
  ]);
  const W = Math.max(ma.width, mb.width);
  const H = Math.max(ma.height, mb.height);
  const pad = (buf, m) =>
    sharp(buf)
      .extend({
        top: 0,
        left: 0,
        bottom: H - m.height,
        right: W - m.width,
        background: '#fff',
      })
      .raw()
      .toBuffer();
  const [ra, rb] = await Promise.all([pad(a, ma), pad(b, mb)]);
  const out = Buffer.alloc(W * H * 3);
  for (let i = 0; i < ra.length; i += 3) {
    const changed =
      Math.abs(ra[i] - rb[i]) > 8 ||
      Math.abs(ra[i + 1] - rb[i + 1]) > 8 ||
      Math.abs(ra[i + 2] - rb[i + 2]) > 8;
    if (changed) {
      out[i] = 220;
      out[i + 1] = 30;
      out[i + 2] = 30;
    } else {
      // before 를 흰색 쪽으로 65% 섞어 배경으로 깐다.
      const g = (ra[i] * 0.299 + ra[i + 1] * 0.587 + ra[i + 2] * 0.114) | 0;
      const v = (255 - (255 - g) * 0.35) | 0;
      out[i] = out[i + 1] = out[i + 2] = v;
    }
  }
  return sharp(out, { raw: { width: W, height: H, channels: 3 } })
    .png()
    .toBuffer();
}

/** 바뀐 픽셀 비율(%). 높이가 다르면 100 으로 본다 — 레이아웃이 통째로 밀린 것이다. */
async function diff(a, b) {
  const [ma, mb] = await Promise.all([
    sharp(a).metadata(),
    sharp(b).metadata(),
  ]);
  if (ma.width !== mb.width || ma.height !== mb.height) return 100;
  const [ra, rb] = await Promise.all([
    sharp(a).raw().toBuffer(),
    sharp(b).raw().toBuffer(),
  ]);
  let changed = 0;
  for (let i = 0; i < ra.length; i += 3) {
    // 8 은 안티에일리어싱 잡음을 걸러내는 문턱값이다.
    if (
      Math.abs(ra[i] - rb[i]) > 8 ||
      Math.abs(ra[i + 1] - rb[i + 1]) > 8 ||
      Math.abs(ra[i + 2] - rb[i + 2]) > 8
    ) {
      changed++;
    }
  }
  return (changed / (ra.length / 3)) * 100;
}

const results = [];
for (const [prop, info] of Object.entries(props)) {
  let worst = 0;
  let worstId = null;
  let changed = 0;
  let n = 0;

  let failed = 0;
  let firstError = null;

  for (const id of info.sample.slice(0, maxSamples)) {
    const html = docs[id];
    if (!html) continue;
    let before;
    let after;
    try {
      const base = await preprocess(html);
      before = await shot(base);
      after = await shot(strip(base, prop));
    } catch (e) {
      // 한 건 때문에 전체를 멈추지 않는다. 다만 사유는 남긴다 —
      // 삼키면 스크립트 버그가 "렌더 실패"로 둔갑한다.
      failed++;
      if (!firstError) firstError = e.message.split('\n')[0];
      continue;
    }
    const pct = await diff(before, after);
    n++;
    if (pct > 0.005) changed++;
    if (pct > worst) {
      worst = pct;
      worstId = id;
      if (outDir) {
        fs.writeFileSync(path.join(outDir, `${prop}-before.png`), before);
        fs.writeFileSync(path.join(outDir, `${prop}-after.png`), after);
        fs.writeFileSync(
          path.join(outDir, `${prop}-diff.png`),
          await diffImage(before, after),
        );
      }
    }
  }
  results.push({
    prop,
    total: info.total,
    samples: n,
    changed,
    worst,
    worstId,
    failed,
    firstError,
  });
  process.stderr.write('.');
}
await browser.close();
process.stderr.write('\n');

results.sort((a, b) => b.worst - a.worst);
console.log('  최악변화   바뀐문서  대상문서  속성 (최악 문서 id)');
for (const r of results) {
  const skipped = r.failed ? `  ⚠️ 실패 ${r.failed} (${r.firstError})` : '';
  console.log(
    `  ${r.worst.toFixed(2).padStart(7)}%  ${String(`${r.changed}/${r.samples}`).padStart(9)}  ` +
      `${String(r.total).padStart(6)}  ${r.prop}${r.worstId ? ` (${r.worstId})` : ''}${skipped}`,
  );
}
