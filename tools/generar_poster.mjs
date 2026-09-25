// Genera posters SVG de preview por preset, con la misma proyección isométrica
// que usa render3D() en prototipo.html. Sin dependencias.
// Salida: modelos/<tag>-preview.svg
import { writeFileSync, mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const OUT = resolve(__dirname, '..', 'modelos');
mkdirSync(OUT, { recursive: true });

const PRESETS = [
    { tag: 'hidroponia-compact', L: 1.2, W: 1.2, H: 0.8, nN: 2, nL: 2 },
    { tag: 'hidroponia',         L: 2.4, W: 1.6, H: 1.3, nN: 3, nL: 3 },
    { tag: 'hidroponia-36',      L: 3.6, W: 2.0, H: 1.7, nN: 4, nL: 4 },
    { tag: 'hidroponia-48',      L: 4.8, W: 2.0, H: 2.6, nN: 4, nL: 6 },
];

const cosA = Math.cos(Math.PI / 6);
const sinA = Math.sin(Math.PI / 6);
const iso = (x, y, z) => [(x - y) * cosA, (x + y) * sinA - z];

function projectModel(L, modelW, modelH, nL, nN) {
    const offsetX = -L / 2;
    const rowSpacing = nL > 1 ? (modelW - 0.1) / (nL - 1) : 0;
    const levelSpacing = (modelH - 0.31) / (nN - 1);
    const firstLevel = 0.15;
    const pts = (X, Y, Z) => iso(X + 0.325, Y, Z);

    const posteLines = [];
    const travesLines = [];
    const canalLines = [];
    const aguaLines = [];
    const plants = [];

    for (const xPos of [0, L]) {
        for (let i = 0; i < nL; i++) {
            const x = xPos + offsetX, z = i * rowSpacing - (nL - 1) * rowSpacing / 2;
            posteLines.push([pts(x, 0, z), pts(x, modelH, z)]);
        }
        for (let j = 0; j < nN; j++) {
            const y = firstLevel + j * levelSpacing;
            const x1 = xPos + offsetX;
            const z1 = -(nL - 1) * rowSpacing / 2, z2 = (nL - 1) * rowSpacing / 2;
            travesLines.push([pts(x1, y, z1), pts(x1, y, z2)]);
        }
    }

    for (let j = 0; j < nN; j++) {
        const y = firstLevel + j * levelSpacing + 0.05;
        for (let i = 0; i < nL; i++) {
            const z = i * rowSpacing - (nL - 1) * rowSpacing / 2;
            const a = pts(-L / 2, y, z);
            const b = pts(L / 2, y, z);
            canalLines.push([a, b]);
            aguaLines.push([pts(-L / 2, y + 0.01, z), pts(L / 2, y + 0.01, z)]);
            const plantCount = Math.max(3, Math.floor(L / 0.25));
            for (let k = 0; k < plantCount; k++) {
                const px = -L / 2 + 0.15 + (k * (L - 0.3)) / Math.max(1, plantCount - 1);
                plants.push(pts(px, y + 0.05, z));
            }
        }
    }

    const tankW = 0.5, tankH = 0.55, tankD = modelW;
    const tankC = [offsetX - tankW / 2 - 0.175, tankH / 2, 0];
    const pumpC = [offsetX - tankW / 2 - 0.175, 0.12, 0];

    // Caja de 8 esquinas para el tanque (para el polígono de cara visible)
    const hw = tankW / 2, hh = tankH / 2, hd = tankD / 2;
    const [c0, c1, c2, c3] = [
        iso(tankC[0] + hw, tankC[1] + hh, tankC[2] + hd),
        iso(tankC[0] - hw, tankC[1] + hh, tankC[2] + hd),
        iso(tankC[0] - hw, tankC[1] - hh, tankC[2] + hd),
        iso(tankC[0] + hw, tankC[1] - hh, tankC[2] + hd),
    ];
    const tankPoly = [c0, c1, c2, c3];

    // Bomba -> pequeña caja
    const bh = 0.1;
    const [b0, b1, b2, b3] = [
        iso(pumpC[0] + bh, pumpC[1] + bh, pumpC[2] + bh),
        iso(pumpC[0] - bh, pumpC[1] + bh, pumpC[2] + bh),
        iso(pumpC[0] - bh, pumpC[1] - bh, pumpC[2] + bh),
        iso(pumpC[0] + bh, pumpC[1] - bh, pumpC[2] + bh),
    ];
    const pumpPoly = [b0, b1, b2, b3];

    return { posteLines, travesLines, canalLines, aguaLines, plants, tankPoly, pumpPoly };
}

function esc(s) {
    return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function makeSvg({ tag, L, W: modelW, H: modelH, nL, nN }) {
    const W = 800, H = 450;
    const m = projectModel(L, modelW, modelH, nL, nN);

    // Bounds del modelo para centrar + escala de ajuste
    const all = [
        ...m.posteLines.flat(),
        ...m.travesLines.flat(),
        ...m.canalLines.flat(),
        ...m.aguaLines.flat(),
        ...m.plants,
        ...m.tankPoly,
        ...m.pumpPoly,
    ];
    const xs = all.map(([x]) => x), ys = all.map(([, y]) => y);
    const minX = Math.min(...xs), maxX = Math.max(...xs);
    const minY = Math.min(...ys), maxY = Math.max(...ys);
    const cx = (minX + maxX) / 2, cy = (minY + maxY) / 2;
    const scale = Math.min((W - 180) / (maxX - minX), (H - 130) / (maxY - minY));
    const tx = (w, x) => W / 2 + (x - cx) * scale;
    const ty = h => H / 2 + 25 - (h - cy) * scale;

    const seg = (p, q, color, width, dash = '') =>
        `<line x1="${tx(0, p[0]).toFixed(1)}" y1="${ty(p[1]).toFixed(1)}" x2="${tx(0, q[0]).toFixed(1)}" y2="${ty(q[1]).toFixed(1)}" stroke="${color}" stroke-width="${width}"${dash ? ` stroke-dasharray="${dash}"` : ''} stroke-linecap="round"/>`;
    const poly = (pts, fill, strokeColor, strokeW) =>
        `<polygon points="${pts.map(p => `${tx(0, p[0]).toFixed(1)},${ty(p[1]).toFixed(1)}`).join(' ')}" fill="${fill}" stroke="${strokeColor}" stroke-width="${strokeW}"/>`;

    const dims = { compact: '1,2 m', hidroponia: '2,4 m', 'hidroponia-36': '3,6 m', 'hidroponia-48': '4,8 m' }[tag] || `${L} m`;
    const levels = ['compact', 'hidroponia', 'hidroponia-36', 'hidroponia-48'].indexOf(tag) + 1;
    const conf = tag === 'compact' ? `${L.toFixed(1)} m · 2×2 líneas · 2 niveles`
        : `${L.toFixed(1)} m · ${nL}×${nN} líneas · ${nN} niveles`;

    // Sombra elíptica en el piso
    const floorY = ty(0);
    const floorCX = tx(0, 0);

    const el = [];
    el.push(`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${W} ${H}" width="800" height="450">`);
    el.push(`<defs>
<linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
<stop offset="0" stop-color="#0b1329"/>
<stop offset="1" stop-color="#15233f"/>
</linearGradient>
<radialGradient id="glow" cx="0.5" cy="0.42" r="0.7">
<stop offset="0" stop-color="#22d3ee" stop-opacity="0.35"/>
<stop offset="1" stop-color="#22d3ee" stop-opacity="0"/>
</radialGradient>
</defs>`);
    el.push(`<rect width="${W}" height="${H}" fill="url(#bg)"/>`);
    el.push(`<rect width="${W}" height="${H}" fill="url(#glow)"/>`);

    // grilla de fondo sutil
    for (let i = 20; i < W; i += 40) el.push(`<line x1="${i}" y1="0" x2="${i}" y2="${H}" stroke="#22d3ee" stroke-opacity="0.05"/>`);
    for (let j = 20; j < H; j += 40) el.push(`<line x1="0" y1="${j}" x2="${W}" y2="${j}" stroke="#22d3ee" stroke-opacity="0.05"/>`);

    // marca
    el.push(`<text x="40" y="50" font-family="Inter,'Segoe UI',sans-serif" font-size="14" font-weight="700" letter-spacing="3" fill="#7dd3fc">HIDROPLAN</text>`);
    el.push(`<text x="40" y="70" font-family="'Segoe UI',sans-serif" font-size="10" letter-spacing="1" fill="#94a3b8">SISTEMA NFT · VISTA 3D</text>`);

    // sombra de piso
    const shadowRx = (W - 160) / 2;
    el.push(`<ellipse cx="${floorCX}" cy="${floorY}" rx="${(0.62 * shadowRx).toFixed(1)}" ry="14" fill="#020617" opacity="0.45"/>`);

    // postes y travesaños
    for (const [a, b] of [...m.posteLines, ...m.travesLines]) el.push(seg(a, b, '#64748b', 3));
    // canal (PVC)
    for (const [a, b] of m.canalLines) el.push(seg(a, b, '#e2e8f0', 9));
    // agua
    for (const [a, b] of m.aguaLines) el.push(seg(a, b, '#06b6d4', 4, '5,4'));
    // estructura encima
    for (const [a, b] of m.posteLines) el.push(seg(a, b, '#94a3b8', 1.5));

    // plantines
    for (const p of m.plants) {
        el.push(`<circle cx="${tx(0, p[0]).toFixed(1)}" cy="${ty(p[1]).toFixed(1)}" r="3.4" fill="#22c55e"/>`);
    }

    // tanque y bomba
    el.push(poly(m.tankPoly, '#334155', '#06b6d4', 1.5));
    el.push(poly(m.pumpPoly, '#0ea5e9', '#7dd3fc', 1));

    // caja de título al pie
    el.push(`<rect x="40" y="${H - 78}" width="${W - 80}" height="46" rx="10" fill="#020617" opacity="0.5"/>`);
    el.push(`<text x="60" y="${H - 48}" font-family="'Segoe UI',sans-serif" font-size="13" font-weight="600" fill="#e2e8f0">HidroPlan · Preview ${dims}</text>`);
    el.push(`<text x="60" y="${H - 30}" font-family="'Segoe UI',sans-serif" font-size="11" fill="#94a3b8">${esc(conf)} · Escala 1:1 · Anclaje a piso</text>`);
    el.push(`<circle cx="${W - 56}" cy="${H - 52}" r="3" fill="#22c55e"/>`);
    el.push(`<text x="${W - 46}" y="${H - 48}" font-family="'Segoe UI',sans-serif" font-size="10" fill="#4ade80">${levels}/4</text>`);

    el.push('</svg>');
    return el.join('\n');
}

for (const preset of PRESETS) {
    const file = resolve(OUT, `${preset.tag}-preview.svg`);
    writeFileSync(file, makeSvg(preset));
    console.log(`Poster OK  →  modelos/${preset.tag}-preview.svg`);
}
