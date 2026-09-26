// Genera GLBs auxiliares para la app Android (SceneView/Filament) sin GLTFExporter
// (el exporter oficial usa FileReader, que no existe en Node). Writer GLB mínimo.
// Uso: node tools/generar_objetos_ar.mjs
import { writeFileSync, mkdirSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const OUT = resolve(dirname(fileURLToPath(import.meta.url)), '../apps/android/app/src/main/assets/models/ar');
mkdirSync(OUT, { recursive: true });

function float32(buf, off, v) {
    buf.writeFloatLE(v, off);
}
function uint16(buf, off, v) {
    buf.writeUInt16LE(v, off);
}
function uint32(buf, off, v) {
    buf.writeUInt32LE(v, off);
}

// Geometría UV sphere (Y up) de radio r, con normales.
function genSphere(r, slices, stacks) {
    const pos = [], nrm = [];
    for (let i = 0; i <= stacks; i++) {
        const lat = Math.PI * (i / stacks);
        const sy = Math.cos(lat), sr = Math.sin(lat);
        for (let j = 0; j <= slices; j++) {
            const lon = 2 * Math.PI * (j / slices);
            const x = Math.cos(lon) * sr, y = sy, z = Math.sin(lon) * sr;
            pos.push(x * r, y * r, z * r);
            nrm.push(x, y, z);
        }
    }
    const idx = [];
    for (let i = 0; i < stacks; i++) {
        for (let j = 0; j < slices; j++) {
            const a = i * (slices + 1) + j;
            const b = a + slices + 1;
            idx.push(a, b, a + 1, a + 1, b, b + 1);
        }
    }
    return { pos, nrm, idx };
}

// Plano 1x1 en XZ, normal +Y.
function genPlane() {
    const pos = [
        -0.5, 0, -0.5,
        0.5, 0, -0.5,
        0.5, 0, 0.5,
        -0.5, 0, 0.5,
    ];
    const nrm = [0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0];
    const idx = [0, 1, 2, 0, 2, 3];
    return { pos, nrm, idx };
}

function buildGlb(geo, color, alpha, doubleSided) {
    const posBuf = Buffer.from(Float32Array.from(geo.pos).buffer);
    const nrmBuf = Buffer.from(Float32Array.from(geo.nrm).buffer);
    const idxBuf = Buffer.from(Uint16Array.from(geo.idx).buffer);
    const vCount = geo.pos.length / 3;
    const iCount = geo.idx.length;

    const P = posBuf.length, N = nrmBuf.length, I = idxBuf.length;
    const binLen = P + N + I;
    const binAligned = binLen + (binLen % 4 ? 4 - (binLen % 4) : 0);

    const minX = Math.min(...geo.pos.filter((_, k) => k % 3 === 0)); // no per-axis min/8
    // min/max por eje:
    let mn = [Infinity, Infinity, Infinity], mx = [-Infinity, -Infinity, -Infinity];
    for (let k = 0; k < geo.pos.length; k += 3) {
        for (let e = 0; e < 3; e++) {
            const v = geo.pos[k + e];
            if (v < mn[e]) mn[e] = v;
            if (v > mx[e]) mx[e] = v;
        }
    }

    const json = {
        asset: { version: '2.0' },
        scene: 0,
        scenes: [{ nodes: [0] }],
        nodes: [{ mesh: 0, name: 'root' }],
        meshes: [{
            name: 'ar_obj',
            primitives: [{
                attributes: { POSITION: 0, NORMAL: 1 },
                indices: 2,
                material: 0,
            }],
        }],
        materials: [{
            name: 'cyan',
            pbrMetallicRoughness: {
                baseColorFactor: color,
                metallicFactor: 0,
                roughnessFactor: 0.5,
            },
            doubleSided: doubleSided,
            alphaMode: alpha < 1 ? 'BLEND' : 'OPAQUE',
        }],
        buffers: [{ byteLength: binAligned }],
        bufferViews: [
            { buffer: 0, byteOffset: 0, byteLength: P, target: 34962 },
            { buffer: 0, byteOffset: P, byteLength: N, target: 34962 },
            { buffer: 0, byteOffset: P + N, byteLength: I, target: 34963 },
        ],
        accessors: [
            { bufferView: 0, componentType: 5126, count: vCount, type: 'VEC3', min: mn, max: mx },
            { bufferView: 1, componentType: 5126, count: vCount, type: 'VEC3' },
            { bufferView: 2, componentType: 5123, count: iCount, type: 'SCALAR' },
        ],
    };

    let jsonStr = JSON.stringify(json);
    const jsonBuf = Buffer.from(jsonStr, 'utf8');
    const jsonChunk = jsonBuf.length;
    const jsonAligned = jsonChunk + (jsonChunk % 4 ? 4 - (jsonChunk % 4) : 0);

    const total = 12 + 8 + jsonAligned + 8 + binAligned;
    const glb = Buffer.alloc(total);
    uint32(glb, 0, 0x46546c67); // 'glTF'
    uint32(glb, 4, 2);
    uint32(glb, 8, total);
    uint32(glb, 12, jsonAligned);
    glb.write('JSON', 16, 4, 'ascii');
    glb.write(jsonStr, 20, 'utf8');
    uint32(glb, 20 + jsonAligned, binAligned);
    glb.write('BIN\0', 20 + jsonAligned + 4, 4, 'ascii');
    const binStart = 20 + jsonAligned + 8;
    posBuf.copy(glb, binStart);
    nrmBuf.copy(glb, binStart + P);
    idxBuf.copy(glb, binStart + P + N);
    return glb;
}

const sphere = genSphere(0.03, 16, 10);
writeFileSync(resolve(OUT, 'punto.glb'), buildGlb(sphere, [0.13, 0.83, 0.93, 1], 1, false));
console.log(`GLB OK  →  apps/.../models/ar/punto.glb`);

const plane = genPlane();
writeFileSync(resolve(OUT, 'plano.glb'), buildGlb(plane, [0.13, 0.83, 0.93, 0.35], 0.35, true));
console.log(`GLB OK  →  apps/.../models/ar/plano.glb`);

console.log('Listo.');