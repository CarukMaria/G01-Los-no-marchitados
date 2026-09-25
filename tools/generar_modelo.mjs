// Genera los modelos GLB a escala métrica real (1 unidad = 1 metro).
// Port fiel de buildHydro3DModel() de prototipo.html (materiales, colores y
// dimensiones idénticos). Salida: modelos/<tag>.glb
import * as THREE from 'three';
import { GLTFExporter } from 'three/examples/jsm/exporters/GLTFExporter.js';
import { writeFileSync, mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const OUT = resolve(__dirname, '..', 'modelos');
mkdirSync(OUT, { recursive: true });

// GLTFExporter usa Blob + FileReader (browser) para el chunk binario del GLB.
// Node >=18 trae Blob pero no FileReader: se polyfillea con su contraparte async.
if (typeof globalThis.FileReader === 'undefined') {
    globalThis.FileReader = class FileReader {
        readAsArrayBuffer(blob) {
            blob.arrayBuffer().then(buf => { this.result = buf; if (this.onloadend) this.onloadend(); });
        }
    };
}

// Presets accesibles desde el prototipo (mismos tags que AR_MODELOS en JS).
// La geometría se modela en metros: 1 unidad GLB = 1 m.
const PRESETS = [
    { tag: 'hidroponia-compact', L: 1.2, nN: 2, nL: 2 },
    { tag: 'hidroponia',         L: 2.4, nN: 3, nL: 3 },
    { tag: 'hidroponia-36',      L: 3.6, nN: 4, nL: 4 },
    { tag: 'hidroponia-48',      L: 4.8, nN: 4, nL: 6 },
];

// Constantes compartidas en metros (espejadas en tools/generar_usdz.py)
export const SEP_Y = 0.45; // separación entre líneas de canales
export const SEP_Z = 0.42; // altura entre niveles
export const BASE_Z = 0.45; // altura del primer nivel

function buildModel(L, nL, nN) {
    const group = new THREE.Group();
    const offsetX = -L / 2;
    const offsetZ = -((nL - 1) * SEP_Y) / 2;
    const topZ = BASE_Z + (nN - 1) * SEP_Z;

    const pipeMat = new THREE.MeshStandardMaterial({ color: 0xf1f5f9, roughness: 0.25, metalness: 0.1 });
    const waterMat = new THREE.MeshStandardMaterial({ color: 0x22d3ee, roughness: 0.1, metalness: 0.3, transparent: true, opacity: 0.8 });
    const frameMat = new THREE.MeshStandardMaterial({ color: 0x475569, roughness: 0.5, metalness: 0.7 });
    const plantMat = new THREE.MeshStandardMaterial({ color: 0x22c55e, roughness: 0.6 });
    const tankMat = new THREE.MeshStandardMaterial({ color: 0x1e293b, roughness: 0.4, metalness: 0.2 });
    const pumpMat = new THREE.MeshStandardMaterial({ color: 0x0284c7, roughness: 0.4, metalness: 0.3 });

    // 1. Postes verticales (estructura)
    const postGeom = new THREE.CylinderGeometry(0.025, 0.025, topZ + 0.3, 12);
    for (const xPos of [0, L]) {
        for (let i = 0; i < nL; i++) {
            const post = new THREE.Mesh(postGeom, frameMat);
            post.position.set(xPos + offsetX, (topZ + 0.3) / 2, i * SEP_Y + offsetZ);
            group.add(post);
        }
    }

    // 2. Travesaños horizontales por nivel
    for (let j = 0; j < nN; j++) {
        const y = BASE_Z + j * SEP_Z;
        const barGeom = new THREE.CylinderGeometry(0.018, 0.018, (nL - 1) * SEP_Y + 0.1, 8);
        for (const xPos of [0, L]) {
            const bar = new THREE.Mesh(barGeom, frameMat);
            bar.rotation.x = Math.PI / 2;
            bar.position.set(xPos + offsetX, y, ((nL - 1) * SEP_Y) / 2 + offsetZ);
            group.add(bar);
        }
    }

    // 3. Canales NFT + agua + plantines
    const channelGeom = new THREE.CylinderGeometry(0.045, 0.045, L, 16);
    const plantGeom = new THREE.SphereGeometry(0.04, 8, 8);
    for (let j = 0; j < nN; j++) {
        const y = BASE_Z + j * SEP_Z + 0.05;
        for (let i = 0; i < nL; i++) {
            const z = i * SEP_Y + offsetZ;

            const pipe = new THREE.Mesh(channelGeom, pipeMat);
            pipe.rotation.z = Math.PI / 2;
            pipe.position.set(0, y, z);
            group.add(pipe);

            const water = new THREE.Mesh(channelGeom, waterMat);
            water.scale.set(0.85, 0.99, 0.85);
            water.rotation.z = Math.PI / 2;
            water.position.set(0, y + 0.01, z);
            group.add(water);

            const plantCount = Math.max(3, Math.floor(L / 0.25));
            for (let k = 0; k < plantCount; k++) {
                const plantX = -L / 2 + 0.15 + (k * (L - 0.3)) / Math.max(1, plantCount - 1);
                const plant = new THREE.Mesh(plantGeom, plantMat);
                plant.scale.set(1.1, 1.4, 1.1);
                plant.position.set(plantX, y + 0.05, z);
                group.add(plant);
            }
        }
    }

    // 4. Tanque reservorio
    const tankW = 0.5, tankH = 0.55, tankD = (nL - 1) * SEP_Y + 0.35;
    const tank = new THREE.Mesh(new THREE.BoxGeometry(tankW, tankH, tankD), tankMat);
    tank.position.set(offsetX - tankW / 2 - 0.15, tankH / 2, 0);
    group.add(tank);

    // 5. Bomba sumergible
    const pump = new THREE.Mesh(new THREE.BoxGeometry(0.2, 0.2, 0.2), pumpMat);
    pump.position.set(offsetX - tankW / 2 - 0.15, 0.12, 0);
    group.add(pump);

    return group;
}

for (const { tag, L, nN, nL } of PRESETS) {
    const scene = new THREE.Scene();
    scene.add(buildModel(L, nL, nN));
    const exporter = new GLTFExporter();
    const ab = await new Promise((res, rej) => exporter.parse(scene, res, rej, { binary: true }));
    writeFileSync(resolve(OUT, `${tag}.glb`), Buffer.from(ab));
    console.log(`GLB OK  →  modelos/${tag}.glb  (${(ab.byteLength / 1024).toFixed(1)} KB, ` +
        `config ${L}m × ${nL} líneas × ${nN} niveles)`);
}