// Генератор ассетов мода Aurum: Economy.
// Запуск: node tools/generate-assets.mjs
// Рисует пиксель-арт текстуры (анимированные монеты, купюру, кошелёк, блоки, частицы, скин Банкира)
// и создаёт все JSON-файлы: модели, blockstates, рецепты, лут, теги, достижения, lang.
import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const RES = path.join(ROOT, 'src/main/resources');
const ASSETS = path.join(RES, 'assets/aurum');
const DATA = path.join(RES, 'data/aurum');
const MC_DATA = path.join(RES, 'data/minecraft');
const WEB = path.resolve(ROOT, '../src/assets/tex');

// ---------------------------------------------------------------- PNG
const CRC = (() => {
  const t = new Uint32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    t[n] = c >>> 0;
  }
  return t;
})();
const crc32 = (buf) => {
  let c = 0xffffffff;
  for (const b of buf) c = CRC[(c ^ b) & 255] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
};
const chunk = (type, data) => {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const td = Buffer.concat([Buffer.from(type, 'ascii'), data]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(td));
  return Buffer.concat([len, td, crc]);
};
function encodePNG(img) {
  const { w, h, d } = img;
  const raw = Buffer.alloc((w * 4 + 1) * h);
  for (let y = 0; y < h; y++) {
    raw[y * (w * 4 + 1)] = 0;
    Buffer.from(d.buffer, y * w * 4, w * 4).copy(raw, y * (w * 4 + 1) + 1);
  }
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(w, 0);
  ihdr.writeUInt32BE(h, 4);
  ihdr[8] = 8; ihdr[9] = 6;
  return Buffer.concat([
    Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
    chunk('IHDR', ihdr),
    chunk('IDAT', zlib.deflateSync(raw, { level: 9 })),
    chunk('IEND', Buffer.alloc(0)),
  ]);
}

class Img {
  constructor(w, h) { this.w = w; this.h = h; this.d = new Uint8Array(w * h * 4); }
  set(x, y, c) {
    x = Math.floor(x); y = Math.floor(y);
    if (x < 0 || y < 0 || x >= this.w || y >= this.h || !c) return;
    const i = (y * this.w + x) * 4;
    this.d[i] = c[0]; this.d[i + 1] = c[1]; this.d[i + 2] = c[2]; this.d[i + 3] = c[3] ?? 255;
  }
  get(x, y) {
    if (x < 0 || y < 0 || x >= this.w || y >= this.h) return [0, 0, 0, 0];
    const i = (y * this.w + x) * 4;
    return [this.d[i], this.d[i + 1], this.d[i + 2], this.d[i + 3]];
  }
  rect(x0, y0, x1, y1, c) { for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) this.set(x, y, c); }
  blit(src, dx, dy) {
    for (let y = 0; y < src.h; y++) for (let x = 0; x < src.w; x++) {
      const c = src.get(x, y);
      if (c[3] > 0) this.set(dx + x, dy + y, c);
    }
  }
  scaled(k) {
    const o = new Img(this.w * k, this.h * k);
    for (let y = 0; y < o.h; y++) for (let x = 0; x < o.w; x++) o.set(x, y, this.get(Math.floor(x / k), Math.floor(y / k)));
    return o;
  }
}

// ---------------------------------------------------------------- helpers
const hex = (s, a = 255) => { const n = parseInt(s.slice(1), 16); return [(n >> 16) & 255, (n >> 8) & 255, n & 255, a]; };
const mix = (a, b, t) => [0, 1, 2].map((i) => Math.round(a[i] + (b[i] - a[i]) * t)).concat([255]);
const shade = (c, f) => [Math.min(255, Math.round(c[0] * f)), Math.min(255, Math.round(c[1] * f)), Math.min(255, Math.round(c[2] * f)), c[3] ?? 255];
const rng = (seed) => () => { seed |= 0; seed = (seed + 0x6d2b79f5) | 0; let t = Math.imul(seed ^ (seed >>> 15), 1 | seed); t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t; return ((t ^ (t >>> 14)) >>> 0) / 4294967296; };

const write = (file, content) => { fs.mkdirSync(path.dirname(file), { recursive: true }); fs.writeFileSync(file, content); };
const json = (file, obj) => write(file, JSON.stringify(obj, null, 2) + '\n');
const png = (file, img) => write(file, encodePNG(img));
const web = (name, img) => png(path.join(WEB, name + '.png'), img);

function stack(frames) {
  const out = new Img(frames[0].w, frames[0].h * frames.length);
  frames.forEach((f, i) => out.blit(f, 0, i * f.h));
  return out;
}

/** Классификация слоёв маски: 1 = контур, 2 = ободок, 3 = внутренность. */
function layers(mask, w, h) {
  const at = (x, y) => (x >= 0 && y >= 0 && x < w && y < h && mask[y][x]);
  const L = Array.from({ length: h }, () => new Array(w).fill(0));
  const nb = [[1, 0], [-1, 0], [0, 1], [0, -1]];
  for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) {
    if (mask[y][x] && nb.some(([dx, dy]) => !at(x + dx, y + dy))) L[y][x] = 1;
  }
  for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) {
    if (mask[y][x] && !L[y][x]) L[y][x] = nb.some(([dx, dy]) => L[y + dy]?.[x + dx] === 1) ? 2 : 3;
  }
  return L;
}

// ---------------------------------------------------------------- монеты
const PAL = {
  copper: { outline: '#4a2210', rim: '#a85a2c', rimLight: '#d98a54', face: '#c97a40', light: '#eba26a', dark: '#a4602f', emblem: '#7e3f1c' },
  silver: { outline: '#3f4652', rim: '#8f99a8', rimLight: '#dbe2ea', face: '#c3ccd8', light: '#f1f4f8', dark: '#9ba5b4', emblem: '#667082' },
  gold:   { outline: '#6b4300', rim: '#d19a00', rimLight: '#ffe58a', face: '#f6c618', light: '#fff1a6', dark: '#dba600', emblem: '#a56f00' },
};
const EMBLEM_FRONT = ['.###.', '#...#', '#...#', '#####', '#...#', '#...#', '#...#'];
const EMBLEM_BACK = ['..#..', '.###.', '#####', '.###.', '..#..'];

function coinFrame(name, deg) {
  const pal = Object.fromEntries(Object.entries(PAL[name]).map(([k, v]) => [k, hex(v)]));
  const S = 16, c = 8, R = 7.2;
  const img = new Img(S, S);
  const cs = Math.cos((deg * Math.PI) / 180);
  const back = cs < 0;
  const rx = R * Math.abs(cs);

  if (rx < 1.6) {
    // Ребро монеты (вид сбоку)
    for (let y = 1; y <= 14; y++) {
      const edge = y === 1 || y === 14;
      img.set(7, y, edge ? pal.outline : pal.rimLight);
      img.set(8, y, edge ? pal.outline : pal.rim);
      if (!edge && y > 2 && y < 13) { img.set(6, y, pal.outline); img.set(9, y, pal.outline); }
    }
    img.set(7, 0, null);
    return img;
  }

  const mask = Array.from({ length: S }, (_, y) => Array.from({ length: S }, (_, x) => {
    const px = x + 0.5 - c, py = y + 0.5 - c;
    return (px / rx) ** 2 + (py / R) ** 2 <= 1;
  }));
  const L = layers(mask, S, S);
  for (let y = 0; y < S; y++) for (let x = 0; x < S; x++) {
    const l = L[y][x];
    if (!l) continue;
    const px = x + 0.5 - c, py = y + 0.5 - c;
    const nx = px / rx, ny = py / R;
    const s = nx * 0.55 + ny * 0.85;
    if (l === 1) { img.set(x, y, pal.outline); continue; }
    if (l === 2) { img.set(x, y, s < -0.15 ? pal.rimLight : pal.rim); continue; }
    let col = pal.face;
    if (s < -0.5) col = pal.light; else if (s > 0.55) col = pal.dark;
    // Эмблема: «A» на аверсе, ромб на реверсе (в проекции с учётом сжатия)
    const ox = (back ? -nx : nx) * R;
    if (!back) {
      const col2 = Math.floor(ox + 2.5), row = Math.floor(py + 3.5);
      if (EMBLEM_FRONT[row]?.[col2] === '#') col = pal.emblem;
    } else {
      const col2 = Math.floor(ox + 2.5), row = Math.floor(py + 2.5);
      if (EMBLEM_BACK[row]?.[col2] === '#') col = pal.emblem;
    }
    img.set(x, y, col);
  }
  return img;
}

const COIN_FRAMES = 12;
const coinStrip = (name) => stack(Array.from({ length: COIN_FRAMES }, (_, k) => coinFrame(name, (k * 360) / COIN_FRAMES)));

function banknoteFrame(k) {
  const img = new Img(16, 16);
  const outline = hex('#1d4a29'), border = hex('#a8eeb0'), base = hex('#5cb86a'), base2 = hex('#52ab60');
  for (let y = 3; y <= 12; y++) for (let x = 0; x <= 15; x++) {
    let c;
    if (x === 0 || x === 15 || y === 3 || y === 12) c = outline;
    else if (x === 1 || x === 14 || y === 4 || y === 11) c = border;
    else c = (x + y) % 4 === 0 ? base2 : base;
    img.set(x, y, c);
  }
  // Медальон в центре
  for (let y = 5; y <= 10; y++) for (let x = 5; x <= 10; x++) {
    const d = Math.hypot(x + 0.5 - 8, y + 0.5 - 7.5);
    if (d <= 3.1) img.set(x, y, d > 2.2 ? hex('#2f7a40') : hex('#e4f8d6'));
  }
  img.rect(7, 7, 8, 8, hex('#f6c618'));
  img.set(7, 6, hex('#a56f00')); img.set(8, 6, hex('#a56f00'));
  // Номиналы по углам
  [[2, 5], [3, 5], [12, 10], [13, 10], [2, 10], [13, 5]].forEach(([x, y]) => img.set(x, y, hex('#1d4a29')));
  // Блик-голограмма скользит по диагонали
  const p = -2 + k * 5;
  for (let y = 3; y <= 12; y++) for (let x = 0; x <= 15; x++) {
    if (Math.abs(x + y - p) < 1.7) {
      const cur = img.get(x, y);
      if (cur[3]) img.set(x, y, mix(cur, [255, 255, 255, 255], 0.5));
    }
  }
  return img;
}
const BANK_FRAMES = 8;

function walletImg() {
  const img = new Img(16, 16);
  const mask = Array.from({ length: 16 }, (_, y) => Array.from({ length: 16 }, (_, x) => {
    const px = x + 0.5, py = y + 0.5;
    const body = ((px - 8) / 6.6) ** 2 + ((py - 10.4) / 5.1) ** 2 <= 1;
    const neck = py >= 3.5 && py <= 8 && Math.abs(px - 8) <= 2.2 + (py - 3.5) * 0.35;
    const frill = py >= 2 && py < 4.2 && Math.abs(px - 8) <= 3.8 - (py - 2) * 0.2;
    return body || neck || frill;
  }));
  const L = layers(mask, 16, 16);
  const c = { o: hex('#3a2012'), b: hex('#8a5a34'), l: hex('#b88050'), d: hex('#6a4026'), t: hex('#efdca8'), g: hex('#f6c618'), G: hex('#a56f00') };
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const l = L[y][x];
    if (!l) continue;
    if (l === 1) { img.set(x, y, c.o); continue; }
    const s = (x - 8) * 0.6 + (y - 10) * 0.8;
    img.set(x, y, s < -3 ? c.l : s > 3.5 ? c.d : c.b);
  }
  for (let x = 5; x <= 10; x++) { if (mask[6][x] && L[6][x] > 1) img.set(x, 6, c.t); if (mask[7][x] && L[7][x] > 1) img.set(x, 7, mix(c.t, c.d, 0.4)); }
  // Золотая застёжка
  for (let y = 9; y <= 13; y++) for (let x = 6; x <= 10; x++) {
    const d = Math.hypot(x + 0.5 - 8, y + 0.5 - 11);
    if (d <= 2.0) img.set(x, y, d > 1.35 ? c.G : c.g);
  }
  img.set(7, 10, hex('#fff1a6'));
  return img;
}

// ---------------------------------------------------------------- блоки
const noise = (seed, amp = 0.07) => { const r = rng(seed); return () => 1 + (r() - 0.5) * 2 * amp; };

function tile(fn) { const img = new Img(16, 16); for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) img.set(x, y, fn(x, y)); return img; }

function mintTop() {
  const n = noise(11), base = hex('#7f8590');
  return tile((x, y) => {
    const e = Math.min(x, y, 15 - x, 15 - y);
    let c = shade(base, n());
    if (e === 0) c = hex('#434852');
    else if (e === 1) c = hex('#a9afb9');
    else if (e === 2) c = hex('#5e646e');
    const d = Math.hypot(x + 0.5 - 8, y + 0.5 - 8);
    if (d < 5.6) c = shade(hex('#3a3e47'), n());
    if (d < 5.6 && d > 4.4) c = hex('#e3b422');
    if (d <= 4.4 && d > 4.0) c = hex('#8a5f00');
    if (d < 3.2) c = d > 2.5 ? hex('#a56f00') : hex('#f6c618');
    if (d < 1.6) c = hex('#fff1a6');
    return c;
  });
}
function mintSide() {
  const n = noise(12), base = hex('#868c97');
  return tile((x, y) => {
    let c = shade(base, n());
    if (y <= 1) c = y === 0 ? hex('#8a5f00') : hex('#e3b422');
    else if (y === 2) c = hex('#5b606a');
    if (x === 0 || x === 15) c = hex('#555a64');
    if (y === 8 && y > 2) c = hex('#5b606a');
    if (y === 9) c = shade(base, 1.12);
    // заклёпки
    if (((x === 2 || x === 13) && (y === 5 || y === 12))) c = hex('#d6dbe2');
    // щель для монет
    if (x >= 5 && x <= 10 && y >= 5 && y <= 6) c = y === 5 ? hex('#16181d') : hex('#262a31');
    if (x >= 4 && x <= 11 && y === 4) c = hex('#c5cad3');
    if (x >= 4 && x <= 11 && y === 7) c = hex('#555a64');
    // пластина-таблица
    if (x >= 3 && x <= 12 && y >= 11 && y <= 14) c = (x + y) % 2 ? hex('#e3b422') : hex('#c99c14');
    if (x >= 3 && x <= 12 && y === 15) c = hex('#4b505a');
    return c;
  });
}
const mintBottom = () => { const n = noise(13, 0.1); return tile(() => shade(hex('#4b4e56'), n())); };

function planks(seed, vertical = false) {
  const n = noise(seed, 0.06), base = hex('#b8945a'), dark = hex('#8a6a3c'), light = hex('#c9a66a');
  return tile((x0, y0) => {
    const [x, y] = vertical ? [y0, x0] : [x0, y0];
    let c = shade(base, n());
    if (y % 4 === 0) c = dark;
    else if (y % 4 === 1) c = light;
    if ((x + (y >> 2) * 5) % 11 === 0 && y % 4 > 1) c = shade(dark, 1.1);
    return c;
  });
}
function tellerTop() {
  const p = planks(21);
  return tile((x, y) => {
    const e = Math.min(x, y, 15 - x, 15 - y);
    if (e === 0) return hex('#7a5a30');
    if (e === 1) return hex('#e3b422');
    if (x >= 3 && x <= 12 && y >= 3 && y <= 12) {
      const edge = x === 3 || x === 12 || y === 3 || y === 12;
      return edge ? hex('#3fa066') : ((x + y) % 3 === 0 ? hex('#2a7347') : hex('#2f7d4f'));
    }
    return p.get(x, y);
  });
}
function tellerFront() {
  const p = planks(22, true), n = noise(23, 0.05);
  return tile((x, y) => {
    if (y === 0) return hex('#6b4d28');
    if (y === 1 || y === 8) return y === 1 ? hex('#8a5f00') : hex('#e3b422');
    if (y >= 2 && y <= 7) {
      if (x <= 1 || x >= 14) return hex('#a9814a');
      if (x === 2 || x === 13) return hex('#e3b422');
      const bar = (x - 3) % 3 === 0;
      return bar ? hex('#e3b422') : shade(hex('#1d1510'), n());
    }
    if (y === 9) return hex('#6b4d28');
    // нижняя панель
    if (x >= 6 && x <= 9 && y >= 11 && y <= 12) return y === 11 ? hex('#f6c618') : hex('#c99c14');
    if (x === 0 || x === 15 || y === 15) return hex('#6b4d28');
    return p.get(x, y);
  });
}
const tellerSide = () => { const p = planks(24, true); return tile((x, y) => (y === 0 ? hex('#e3b422') : y === 1 ? hex('#8a5f00') : x === 0 || x === 15 ? hex('#6b4d28') : p.get(x, y))); };

function pedestalTex() {
  const n = noise(31, 0.05);
  return tile((x, y) => {
    let c = shade(hex('#b9b9b2'), n());
    if (y % 8 === 0) c = hex('#8c8c86');
    if ((y < 8 ? x : x + 4) % 8 === 0 && y % 8 !== 0) c = hex('#8c8c86');
    if (y === 7 || y === 8) c = (x % 2 ? hex('#e3b422') : hex('#f6c618'));
    return c;
  });
}

function stoneTile(seed = 41) { const n = noise(seed, 0.09); return tile(() => shade(hex('#8b8b8b'), n())); }
function stoneBricks() {
  const n = noise(42, 0.06);
  return tile((x, y) => {
    let c = shade(hex('#7d7d7d'), n());
    if (y % 8 === 7) c = hex('#555555');
    const off = y < 8 ? 0 : 4;
    if ((x + off) % 8 === 7 && y % 8 !== 7) c = hex('#555555');
    return c;
  });
}
function barrelSide() {
  const p = planks(43, true);
  return tile((x, y) => ((y >= 2 && y <= 3) || (y >= 12 && y <= 13) ? (y % 2 ? hex('#4a4a4f') : hex('#7b7b82')) : p.get(x, y)));
}
function barrelTop() {
  const n = noise(44, 0.06);
  return tile((x, y) => {
    const d = Math.hypot(x + 0.5 - 8, y + 0.5 - 8), e = Math.min(x, y, 15 - x, 15 - y);
    if (e === 0) return hex('#6b4d28');
    if (d < 4.5) return shade(hex('#2a1c10'), n());
    if (d < 5.5) return hex('#5a5a60');
    return shade(hex('#a98550'), n());
  });
}

/** Изометрический кубик-иконка 32x32 (как предметы-блоки в инвентаре). */
function isoCube(top, left, right) {
  const img = new Img(32, 32);
  const put = (x, y, c, f) => img.set(Math.round(x), Math.round(y), shade(c, f));
  const N = 64;
  for (let a = 0; a <= N; a++) for (let b = 0; b <= N; b++) {
    const u = a / N, v = b / N;
    const tu = Math.min(15, Math.floor(u * 16)), tv = Math.min(15, Math.floor(v * 16));
    put(16 + 14 * u - 14 * v, 2 + 7 * u + 7 * v, top.get(tu, tv), 1.0);
    put(2 + 14 * u, 9 + 7 * u + 14 * v, left.get(tu, tv), 0.8);
    put(16 + 14 * u, 16 - 7 * u + 14 * v, right.get(tu, tv), 0.62);
  }
  return img;
}

// ---------------------------------------------------------------- ингредиенты (для сайта)
function ingot(base, light, dark) {
  const img = new Img(16, 16);
  const mask = Array.from({ length: 16 }, () => new Array(16).fill(false));
  for (let i = 0; i < 4; i++) for (let x = 5 - i; x <= 14 - i; x++) mask[5 + i][x] = true;
  for (let j = 0; j < 3; j++) for (let x = 1; x <= 10; x++) mask[9 + j][x] = true;
  for (let y = 9; y <= 11; y++) for (let x = 11; x <= 13; x++) if (x - 11 <= 11 - y + 1) mask[y][x] = true;
  const L = layers(mask, 16, 16);
  const [b, l, d] = [hex(base), hex(light), hex(dark)];
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    if (!L[y][x]) continue;
    if (L[y][x] === 1) { img.set(x, y, shade(d, 0.55)); continue; }
    if (y <= 8) img.set(x, y, y === 5 || y === 6 ? l : b);
    else if (x >= 11) img.set(x, y, d);
    else img.set(x, y, y === 11 ? d : b);
  }
  return img;
}
function gem(base, light, dark) {
  const img = new Img(16, 16);
  const mask = Array.from({ length: 16 }, (_, y) => Array.from({ length: 16 }, (_, x) => Math.abs(x + 0.5 - 8) / 6.4 + Math.abs(y + 0.5 - 8) / 6.8 <= 1));
  const L = layers(mask, 16, 16);
  const [b, l, d] = [hex(base), hex(light), hex(dark)];
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    if (!L[y][x]) continue;
    if (L[y][x] === 1) { img.set(x, y, shade(d, 0.5)); continue; }
    const s = (x - 8) * 0.6 + (y - 8) * 0.8;
    img.set(x, y, s < -2.5 ? l : s > 2.5 ? d : b);
  }
  return img;
}
function leather() {
  const img = new Img(16, 16);
  const n = noise(51, 0.08);
  const mask = Array.from({ length: 16 }, (_, y) => Array.from({ length: 16 }, (_, x) => ((x + 0.5 - 8) / 6.2) ** 2 + ((y + 0.5 - 8) / 5.2) ** 2 <= 1 + Math.sin(x * 1.7 + y) * 0.08));
  const L = layers(mask, 16, 16);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    if (!L[y][x]) continue;
    img.set(x, y, L[y][x] === 1 ? hex('#3a2012') : shade(hex(L[y][x] === 2 ? '#a06a3e' : '#8a5a34'), n()));
  }
  [[5, 8], [7, 8], [9, 8], [11, 8]].forEach(([x, y]) => img.set(x, y, hex('#d8b88a')));
  return img;
}
function stringIcon() {
  const img = new Img(16, 16);
  [[2, 13], [3, 12], [4, 11], [5, 10], [6, 9], [7, 8], [7, 7], [8, 6], [9, 5], [10, 4], [11, 3], [12, 2]].forEach(([x, y], i) => {
    img.set(x, y, hex(i % 2 ? '#f2f2f2' : '#cfcfcf'));
    img.set(x + 1, y, hex('#9a9a9a'));
  });
  return img;
}

// ---------------------------------------------------------------- жители
function villagerFace(banker) {
  const U = 2;
  const img = new Img(22, 30);
  const skin = hex('#c08a63'), skinD = hex('#a2694a'), brow = hex('#3d2616'), robe = banker ? hex('#1f6b4a') : hex('#6e4a2c');
  const px = (x, y, w, h, c) => img.rect(x * U + 1, y * U + 6, (x + w) * U + 1 - 1, (y + h) * U + 6 - 1, c);
  px(0, 0, 8, 10, skin);
  px(0, 0, 8, 1, shade(skin, 1.08));
  px(3, 4, 2, 4, skinD);          // нос
  px(3, 8, 2, 1, shade(skinD, 0.85));
  px(0, 3, 8, 1, brow);           // брови
  px(1, 4, 2, 1, hex('#ffffff')); px(5, 4, 2, 1, hex('#ffffff'));
  px(1, 4, 1, 1, hex('#3f8a46')); px(6, 4, 1, 1, hex('#3f8a46'));
  px(0, 10, 8, 1, robe);
  if (banker) {
    img.rect(2, 0, 19, 5, hex('#3a3a54'));          // цилиндр
    img.rect(2, 0, 19, 0, hex('#6a6a8c'));
    img.rect(2, 1, 3, 3, hex('#4d4d6c'));
    img.rect(2, 4, 19, 5, hex('#e3b422'));          // лента
    img.rect(0, 6, 21, 7, hex('#2c2c42'));          // поля
    img.rect(0, 6, 21, 6, hex('#5a5a7a'));
  } else {
    img.rect(1, 6, 16, 8, hex('#d9c8a0'));          // лысина-блик
    img.rect(1, 6, 16, 6, skin);
  }
  return img;
}

function bankerSkin() {
  // 64x64 оверлей профессии (раскладка стандартного скина жителя)
  const img = new Img(64, 64);
  const robe = hex('#1f6b4a'), robeD = hex('#17553a'), robeL = hex('#2a8a60'), gold = hex('#e3b422'), goldD = hex('#a56f00');
  const hat = hex('#16161e'), hatL = hex('#262633');
  // Халат/жилет: jacket uv (0,38) 8x20x6
  const jacket = (x0, y0, w, h, front) => {
    for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) {
      let c = (x + y) % 5 === 0 ? robeL : robe;
      if (y >= h - 2) c = y === h - 2 ? goldD : gold;       // золотая кайма снизу
      if (front && (x === 3 || x === 4) && y > 1 && y < h - 3 && (y - 2) % 4 === 0) c = gold; // пуговицы
      if (front && (x === 0 || x === w - 1) && y > 0 && y < h - 2) c = robeD;
      img.set(x0 + x, y0 + y, c);
    }
  };
  jacket(6, 38, 8, 6, false);   // top
  jacket(14, 38, 8, 6, false);  // bottom
  jacket(0, 44, 6, 20, false);  // right
  jacket(6, 44, 8, 20, true);   // front
  jacket(14, 44, 6, 20, false); // left
  jacket(20, 44, 8, 20, false); // back
  // Цилиндр: hat uv (32,0) 8x10x8 — красим только верхнюю часть
  const hatBox = (x0, y0, w, h) => { for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) img.set(x0 + x, y0 + y, (x + y) % 6 === 0 ? hatL : hat); };
  hatBox(40, 0, 8, 8);          // top
  [[32, 8], [40, 8], [48, 8], [56, 8]].forEach(([x0, y0]) => {
    hatBox(x0, y0, 8, 3);
    for (let x = 0; x < 8; x++) { img.set(x0 + x, y0 + 2, gold); }  // золотая лента
  });
  return img;
}

// ---------------------------------------------------------------- ВЫПОЛНЕНИЕ
const T = path.join(ASSETS, 'textures');

// Предметы
for (const name of ['copper', 'silver', 'gold']) {
  const strip = coinStrip(name);
  png(path.join(T, `item/${name}_coin.png`), strip);
  json(path.join(T, `item/${name}_coin.png.mcmeta`), {
    animation: { frametime: 2, frames: [{ index: 0, time: 60 }, ...Array.from({ length: COIN_FRAMES - 1 }, (_, i) => i + 1)] },
  });
  web(`${name}_coin_strip`, strip);
  web(`${name}_coin`, coinFrame(name, 0));
}
const bankStrip = stack(Array.from({ length: BANK_FRAMES }, (_, k) => banknoteFrame(k)));
png(path.join(T, 'item/banknote.png'), bankStrip);
json(path.join(T, 'item/banknote.png.mcmeta'), {
  animation: { frametime: 2, frames: [{ index: 0, time: 50 }, ...Array.from({ length: BANK_FRAMES - 1 }, (_, i) => i + 1)] },
});
web('banknote_strip', bankStrip);
web('banknote', banknoteFrame(0));
png(path.join(T, 'item/wallet.png'), walletImg());
web('wallet', walletImg());

// Блоки
const tex = {
  mint_top: mintTop(), mint_side: mintSide(), mint_bottom: mintBottom(),
  teller_desk_top: tellerTop(), teller_desk_front: tellerFront(), teller_desk_side: tellerSide(),
  coin_pedestal: pedestalTex(),
};
for (const [k, v] of Object.entries(tex)) png(path.join(T, `block/${k}.png`), v);

// Частицы: 8 кадров золотой монеты
for (let i = 0; i < 8; i++) png(path.join(T, `particle/coin_${i}.png`), coinFrame('gold', i * 45));
json(path.join(ASSETS, 'particles/coin.json'), { textures: Array.from({ length: 8 }, (_, i) => `aurum:coin_${i}`) });

// Скин Банкира
png(path.join(T, 'entity/villager/profession/banker.png'), bankerSkin());
json(path.join(T, 'entity/villager/profession/banker.png.mcmeta'), { villager: { hat: 'partial' } });

// Иконка мода
{
  const icon = new Img(128, 128);
  for (let y = 0; y < 128; y++) for (let x = 0; x < 128; x++) {
    const t = y / 127, d = Math.hypot(x - 64, y - 64) / 90;
    icon.set(x, y, mix(mix(hex('#2b1c46'), hex('#0f0a1c'), t), hex('#6b3fa0'), Math.max(0, 0.35 - d * 0.35)));
  }
  icon.blit(coinFrame('gold', 0).scaled(7), 8, 8);
  png(path.join(ASSETS, 'icon.png'), icon);
}

// Картинки для сайта
web('mint', isoCube(tex.mint_top, tex.mint_side, tex.mint_side));
web('teller_desk', isoCube(tex.teller_desk_top, tex.teller_desk_front, tex.teller_desk_side));
web('coin_pedestal', isoCube(tex.coin_pedestal, tex.coin_pedestal, tex.coin_pedestal));
web('barrel', isoCube(barrelTop(), barrelSide(), barrelSide()));
web('piston', isoCube(planks(45), stoneTile(46), stoneTile(47)));
web('planks', planks(48));
web('stone', stoneTile());
web('stone_bricks', stoneBricks());
web('iron_ingot', ingot('#d8d8d8', '#ffffff', '#9a9a9a'));
web('gold_ingot', ingot('#f3c935', '#fff0a0', '#c79a14'));
web('copper_ingot', ingot('#c97a40', '#eba26a', '#9a4f26'));
web('netherite_ingot', ingot('#4a4046', '#6b5f66', '#2a2428'));
web('diamond', gem('#4fe3d6', '#b8fff6', '#1fa89c'));
web('emerald', gem('#2fd45f', '#a6ffbe', '#12902f'));
web('leather', leather());
web('string', stringIcon());
web('villager', villagerFace(false));
web('banker', villagerFace(true));
web('icon', fs.existsSync(path.join(ASSETS, 'icon.png')) ? (() => { const i = new Img(32, 32); i.blit(coinFrame('gold', 0), 8, 8); return coinFrame('gold', 0).scaled(2); })() : new Img(1, 1));

// ---------------------------------------------------------------- модели
const M = path.join(ASSETS, 'models');
for (const n of ['copper_coin', 'silver_coin', 'gold_coin', 'banknote', 'wallet']) {
  json(path.join(M, `item/${n}.json`), { parent: 'minecraft:item/generated', textures: { layer0: `aurum:item/${n}` } });
}
for (const n of ['mint', 'teller_desk', 'coin_pedestal']) json(path.join(M, `item/${n}.json`), { parent: `aurum:block/${n}` });

json(path.join(M, 'block/mint.json'), {
  parent: 'minecraft:block/cube_bottom_top',
  textures: { top: 'aurum:block/mint_top', side: 'aurum:block/mint_side', bottom: 'aurum:block/mint_bottom' },
});
json(path.join(M, 'block/teller_desk.json'), {
  parent: 'minecraft:block/orientable',
  textures: { top: 'aurum:block/teller_desk_top', front: 'aurum:block/teller_desk_front', side: 'aurum:block/teller_desk_side' },
});
{
  const box = (from, to) => {
    const [x0, y0, z0] = from, [x1, y1, z1] = to;
    const f = (a, b, c, d, face) => ({ uv: [a, b, c, d], texture: '#0', ...(face ? { cullface: face } : {}) });
    return {
      from, to,
      faces: {
        north: f(x0, 16 - y1, x1, 16 - y0),
        south: f(x0, 16 - y1, x1, 16 - y0),
        east: f(z0, 16 - y1, z1, 16 - y0),
        west: f(z0, 16 - y1, z1, 16 - y0),
        up: f(x0, z0, x1, z1),
        down: f(x0, z0, x1, z1),
      },
    };
  };
  json(path.join(M, 'block/coin_pedestal.json'), {
    textures: { '0': 'aurum:block/coin_pedestal', particle: 'aurum:block/coin_pedestal' },
    elements: [box([3, 0, 3], [13, 2, 13]), box([5, 2, 5], [11, 12, 11]), box([3, 12, 3], [13, 14, 13])],
    display: {
      gui: { rotation: [30, 225, 0], scale: [0.85, 0.85, 0.85] },
      ground: { scale: [0.5, 0.5, 0.5] },
      fixed: { scale: [0.7, 0.7, 0.7] },
    },
  });
}
const BS = path.join(ASSETS, 'blockstates');
json(path.join(BS, 'mint.json'), { variants: { '': { model: 'aurum:block/mint' } } });
json(path.join(BS, 'coin_pedestal.json'), { variants: { '': { model: 'aurum:block/coin_pedestal' } } });
json(path.join(BS, 'teller_desk.json'), {
  variants: {
    'facing=north': { model: 'aurum:block/teller_desk' },
    'facing=east': { model: 'aurum:block/teller_desk', y: 90 },
    'facing=south': { model: 'aurum:block/teller_desk', y: 180 },
    'facing=west': { model: 'aurum:block/teller_desk', y: 270 },
  },
});

// ---------------------------------------------------------------- рецепты
const R = path.join(DATA, 'recipe');
const A = (id) => ({ item: `aurum:${id}` });
const V = (id) => ({ item: `minecraft:${id}` });
const shaped = (name, pattern, key, result, count = 1) => json(path.join(R, `${name}.json`), {
  type: 'minecraft:crafting_shaped', category: 'misc', pattern, key, result: { id: result, count },
});
const shapeless = (name, ingredients, result, count = 1) => json(path.join(R, `${name}.json`), {
  type: 'minecraft:crafting_shapeless', category: 'misc', ingredients, result: { id: result, count },
});

// Сжатие 9 -> 1
shaped('silver_coin_from_copper', ['CCC', 'CCC', 'CCC'], { C: A('copper_coin') }, 'aurum:silver_coin');
shaped('gold_coin_from_silver', ['CCC', 'CCC', 'CCC'], { C: A('silver_coin') }, 'aurum:gold_coin');
shaped('banknote_from_gold', ['CCC', 'CCC', 'CCC'], { C: A('gold_coin') }, 'aurum:banknote');
// Размен 1 -> 9
shapeless('copper_coin_from_silver', [A('silver_coin')], 'aurum:copper_coin', 9);
shapeless('silver_coin_from_gold', [A('gold_coin')], 'aurum:silver_coin', 9);
shapeless('gold_coin_from_banknote', [A('banknote')], 'aurum:gold_coin', 9);
// Кошелёк, Монетный двор, Банковская стойка, Витрина
shaped('wallet', ['LSL', 'LCL', 'LLL'], { L: V('leather'), S: V('string'), C: A('silver_coin') }, 'aurum:wallet');
shaped('mint', ['III', 'GPG', 'SSS'], { I: V('iron_ingot'), G: V('gold_ingot'), P: V('piston'), S: V('stone') }, 'aurum:mint');
shaped('teller_desk', ['CCC', 'PBP', 'PPP'], { C: V('copper_ingot'), P: { tag: 'minecraft:planks' }, B: V('barrel') }, 'aurum:teller_desk');
shaped('coin_pedestal', [' G ', ' B ', 'BBB'], { G: V('gold_ingot'), B: V('stone_bricks') }, 'aurum:coin_pedestal');

// ---------------------------------------------------------------- лут и теги
for (const n of ['mint', 'teller_desk', 'coin_pedestal']) {
  json(path.join(DATA, `loot_table/blocks/${n}.json`), {
    type: 'minecraft:block',
    pools: [{ rolls: 1, bonus_rolls: 0, conditions: [{ condition: 'minecraft:survives_explosion' }], entries: [{ type: 'minecraft:item', name: `aurum:${n}` }] }],
  });
}
json(path.join(MC_DATA, 'tags/block/mineable/pickaxe.json'), { replace: false, values: ['aurum:mint', 'aurum:coin_pedestal'] });
json(path.join(MC_DATA, 'tags/block/mineable/axe.json'), { replace: false, values: ['aurum:teller_desk'] });

// ---------------------------------------------------------------- достижения
const ADV = path.join(DATA, 'advancement');
const adv = (name, parent, icon, frame, item, extra = {}) => json(path.join(ADV, `${name}.json`), {
  ...(parent ? { parent: `aurum:${parent}` } : {}),
  display: {
    icon: { id: `aurum:${icon}` },
    title: { translate: `advancement.aurum.${name}.title` },
    description: { translate: `advancement.aurum.${name}.description` },
    frame, show_toast: true, announce_to_chat: frame !== 'task', hidden: false,
    ...extra,
  },
  criteria: { has_item: { trigger: 'minecraft:inventory_changed', conditions: { items: [{ items: `aurum:${item}` }] } } },
});
adv('root', null, 'gold_coin', 'task', 'copper_coin', { background: 'minecraft:textures/gui/advancements/backgrounds/adventure.png', announce_to_chat: false });
adv('silver', 'root', 'silver_coin', 'task', 'silver_coin');
adv('wallet', 'root', 'wallet', 'task', 'wallet');
adv('gold', 'silver', 'gold_coin', 'goal', 'gold_coin');
adv('banknote', 'gold', 'banknote', 'challenge', 'banknote');
adv('mint', 'silver', 'mint', 'goal', 'mint');
adv('pedestal', 'mint', 'coin_pedestal', 'task', 'coin_pedestal');

// ---------------------------------------------------------------- язык
const L10N = {
  ru_ru: {
    'itemGroup.aurum.main': 'Aurum: Экономика',
    'item.aurum.copper_coin': 'Медная монета',
    'item.aurum.silver_coin': 'Серебряная монета',
    'item.aurum.gold_coin': 'Золотая монета',
    'item.aurum.banknote': 'Банкнота',
    'item.aurum.wallet': 'Кошелёк',
    'block.aurum.mint': 'Монетный двор',
    'block.aurum.teller_desk': 'Банковская стойка',
    'block.aurum.coin_pedestal': 'Витрина монет',
    'entity.minecraft.villager.aurum.banker': 'Банкир',
    'entity.minecraft.villager.banker': 'Банкир',
    'tooltip.aurum.value': 'Номинал: %s AU',
    'tooltip.aurum.stack_value': 'В стопке: %s AU',
    'tooltip.aurum.flip': 'ПКМ — подбросить монету',
    'tooltip.aurum.wallet.balance': 'Баланс: %s AU',
    'tooltip.aurum.wallet.deposit': 'ПКМ — убрать все монеты из инвентаря',
    'tooltip.aurum.wallet.withdraw': 'Shift + ПКМ — достать всё наличными',
    'message.aurum.flip.heads': 'Орёл!',
    'message.aurum.flip.tails': 'Решка!',
    'message.aurum.wallet.nothing_to_deposit': 'В инвентаре нет монет для кошелька',
    'message.aurum.wallet.deposit': 'В кошелёк: +%s AU (всего %s AU)',
    'message.aurum.wallet.empty': 'Кошелёк пуст',
    'message.aurum.wallet.withdraw': 'Выдано наличными: %s AU',
    'message.aurum.mint.help': 'Клади слиток: медь → 3 мед., железо → 1 сер., золото → 1 зол., алмаз → 2 зол. (Shift — всю стопку)',
    'key.aurum.toggle_hud': 'Закрепить баланс на экране',
    'key.categories.aurum': 'Aurum: Экономика',
    'advancement.aurum.root.title': 'Звон монет',
    'advancement.aurum.root.description': 'Получите свою первую медную монету',
    'advancement.aurum.silver.title': 'Серебряный век',
    'advancement.aurum.silver.description': 'Получите серебряную монету',
    'advancement.aurum.wallet.title': 'Деньги любят счёт',
    'advancement.aurum.wallet.description': 'Сделайте кошелёк',
    'advancement.aurum.gold.title': 'Золотая лихорадка',
    'advancement.aurum.gold.description': 'Получите золотую монету',
    'advancement.aurum.banknote.title': 'Купюра в 729',
    'advancement.aurum.banknote.description': 'Получите банкноту — самую крупную денежную единицу',
    'advancement.aurum.mint.title': 'Свой монетный двор',
    'advancement.aurum.mint.description': 'Постройте Монетный двор и начните чеканить',
    'advancement.aurum.pedestal.title': 'Витрина богатства',
    'advancement.aurum.pedestal.description': 'Сделайте Витрину монет, чтобы хвастаться деньгами',
  },
  en_us: {
    'itemGroup.aurum.main': 'Aurum: Economy',
    'item.aurum.copper_coin': 'Copper Coin',
    'item.aurum.silver_coin': 'Silver Coin',
    'item.aurum.gold_coin': 'Gold Coin',
    'item.aurum.banknote': 'Banknote',
    'item.aurum.wallet': 'Wallet',
    'block.aurum.mint': 'Mint',
    'block.aurum.teller_desk': 'Teller Desk',
    'block.aurum.coin_pedestal': 'Coin Pedestal',
    'entity.minecraft.villager.aurum.banker': 'Banker',
    'entity.minecraft.villager.banker': 'Banker',
    'tooltip.aurum.value': 'Value: %s AU',
    'tooltip.aurum.stack_value': 'Stack value: %s AU',
    'tooltip.aurum.flip': 'Right-click to flip the coin',
    'tooltip.aurum.wallet.balance': 'Balance: %s AU',
    'tooltip.aurum.wallet.deposit': 'Right-click: deposit all coins from inventory',
    'tooltip.aurum.wallet.withdraw': 'Shift + Right-click: withdraw everything as cash',
    'message.aurum.flip.heads': 'Heads!',
    'message.aurum.flip.tails': 'Tails!',
    'message.aurum.wallet.nothing_to_deposit': 'No coins in your inventory to deposit',
    'message.aurum.wallet.deposit': 'Deposited: +%s AU (total %s AU)',
    'message.aurum.wallet.empty': 'The wallet is empty',
    'message.aurum.wallet.withdraw': 'Withdrawn as cash: %s AU',
    'message.aurum.mint.help': 'Insert an ingot: copper → 3 copper, iron → 1 silver, gold → 1 gold, diamond → 2 gold (Shift — whole stack)',
    'key.aurum.toggle_hud': 'Pin balance on screen',
    'key.categories.aurum': 'Aurum: Economy',
    'advancement.aurum.root.title': 'Clink of Coins',
    'advancement.aurum.root.description': 'Get your very first copper coin',
    'advancement.aurum.silver.title': 'Silver Age',
    'advancement.aurum.silver.description': 'Obtain a silver coin',
    'advancement.aurum.wallet.title': 'Money Loves Counting',
    'advancement.aurum.wallet.description': 'Craft a wallet',
    'advancement.aurum.gold.title': 'Gold Rush',
    'advancement.aurum.gold.description': 'Obtain a gold coin',
    'advancement.aurum.banknote.title': 'A 729 Bill',
    'advancement.aurum.banknote.description': 'Obtain a banknote — the biggest denomination',
    'advancement.aurum.mint.title': 'Your Own Mint',
    'advancement.aurum.mint.description': 'Build a Mint and start striking coins',
    'advancement.aurum.pedestal.title': 'Showcase of Wealth',
    'advancement.aurum.pedestal.description': 'Craft a Coin Pedestal to show off your money',
  },
};
for (const [lang, obj] of Object.entries(L10N)) json(path.join(ASSETS, `lang/${lang}.json`), obj);

console.log('Aurum assets generated.');

// ---------------------------------------------------------------- превью (PREVIEW=1)
if (process.env.PREVIEW) {
  const sheet = new Img(16 * 12 + 40, 16 * 5 + 110);
  const frames = (name) => Array.from({ length: COIN_FRAMES }, (_, k) => coinFrame(name, (k * 360) / COIN_FRAMES));
  ['copper', 'silver', 'gold'].forEach((n, row) => frames(n).forEach((f, i) => sheet.blit(f, i * 16, row * 16)));
  for (let k = 0; k < BANK_FRAMES; k++) sheet.blit(banknoteFrame(k), k * 16, 48);
  sheet.blit(walletImg(), 8 * 16, 48);
  [tex.mint_top, tex.mint_side, tex.mint_bottom, tex.teller_desk_top, tex.teller_desk_front, tex.teller_desk_side, tex.coin_pedestal].forEach((t, i) => sheet.blit(t, i * 16, 64));
  const icons = [isoCube(tex.mint_top, tex.mint_side, tex.mint_side), isoCube(tex.teller_desk_top, tex.teller_desk_front, tex.teller_desk_side), isoCube(tex.coin_pedestal, tex.coin_pedestal, tex.coin_pedestal), villagerFace(false), villagerFace(true)];
  icons.forEach((t, i) => sheet.blit(t, i * 34, 80));
  const ing = [ingot('#d8d8d8', '#ffffff', '#9a9a9a'), ingot('#f3c935', '#fff0a0', '#c99a14'), gem('#4fe3d6', '#b8fff6', '#1fa89c'), leather(), stringIcon(), stoneBricks()];
  ing.forEach((t, i) => sheet.blit(t, i * 16, 112));
  png('/tmp/preview.png', sheet.scaled(5));
  const skin = bankerSkin();
  png('/tmp/skin.png', skin.scaled(6));
}
