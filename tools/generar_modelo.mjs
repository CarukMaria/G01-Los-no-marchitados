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
    { tag: 'hidroponia-compact', L: 1.2, W: 1.2, H: 0.8, nN: 2, nL: 2 },
    { tag: 'hidroponia',         L: 2.4, W: 1.6, H: 1.3, nN: 3, nL: 3 },
    { tag: 'hidroponia-36',      L: 3.6, W: 2.0, H: 1.7, nN: 4, nL: 4 },
    { tag: 'hidroponia-48',      L: 4.8, W: 2.0, H: 2.6, nN: 4, nL: 6 },
];

function buildModel(L, W, H, nL, nN) {
    const group = new THREE.Group();
    const offsetX = -L / 2;
    const rowSpacing = nL > 1 ? (W - 0.1) / (nL - 1) : 0;
    const levelSpacing = (H - 0.31) / (nN - 1);
    const firstLevel = 0.15;

    const pipeMat = new THREE.MeshStandardMaterial({ color: 0xf8fafc, roughness: 0.65, metalness: 0.02 });
    const waterMat = new THREE.MeshStandardMaterial({ color: 0x06b6d4, roughness: 0.35, metalness: 0, transparent: true, opacity: 0.9 });
    const frameMat = new THREE.MeshStandardMaterial({ color: 0x64748b, roughness: 0.75, metalness: 0.05 });
    const plantMat = new THREE.MeshStandardMaterial({ color: 0x22c55e, roughness: 0.75, metalness: 0 });
    const tankMat = new THREE.MeshStandardMaterial({ color: 0x334155, roughness: 0.65, metalness: 0.03 });
    const pumpMat = new THREE.MeshStandardMaterial({ color: 0x0ea5e9, roughness: 0.55, metalness: 0.03 });

    // 1. Postes verticales (estructura)
    const postGeom = new THREE.CylinderGeometry(0.025, 0.025, H, 12);
    for (const xPos of [0, L]) {
        for (let i = 0; i < nL; i++) {
            const post = new THREE.Mesh(postGeom, frameMat);
            post.position.set(xPos + offsetX, H / 2, i * rowSpacing - (nL - 1) * rowSpacing / 2);
            group.add(post);
        }
    }

    // 2. Travesaños horizontales por nivel
    for (let j = 0; j < nN; j++) {
        const y = firstLevel + j * levelSpacing;
        const barGeom = new THREE.CylinderGeometry(0.018, 0.018, (nL - 1) * rowSpacing + 0.1, 8);
        for (const xPos of [0, L]) {
            const bar = new THREE.Mesh(barGeom, frameMat);
            bar.rotation.x = Math.PI / 2;
            bar.position.set(xPos + offsetX, y, 0);
            group.add(bar);
        }
    }

    // 3. Canales NFT + agua + plantines
    const channelGeom = new THREE.CylinderGeometry(0.045, 0.045, L, 16);
    const plantGeom = new THREE.SphereGeometry(0.04, 8, 8);
    for (let j = 0; j < nN; j++) {
        const y = firstLevel + j * levelSpacing + 0.05;
        for (let i = 0; i < nL; i++) {
            const z = i * rowSpacing - (nL - 1) * rowSpacing / 2;

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
    const tankW = 0.5, tankH = 0.55, tankD = W;
    const tank = new THREE.Mesh(new THREE.BoxGeometry(tankW, tankH, tankD), tankMat);
    tank.position.set(offsetX - tankW / 2 - 0.175, tankH / 2, 0);
    group.add(tank);

    // 5. Bomba sumergible
    const pump = new THREE.Mesh(new THREE.BoxGeometry(0.2, 0.2, 0.2), pumpMat);
    pump.position.set(offsetX - tankW / 2 - 0.175, 0.12, 0);
    group.add(pump);

    group.position.x = 0.325;
    return group;
}

for (const { tag, L, W, H, nN, nL } of PRESETS) {
    const scene = new THREE.Scene();
    scene.add(buildModel(L, W, H, nL, nN));
    const exporter = new GLTFExporter();
    const ab = await new Promise((res, rej) => exporter.parse(scene, res, rej, { binary: true }));
    writeFileSync(resolve(OUT, `${tag}.glb`), Buffer.from(ab));
    console.log(`GLB OK  →  modelos/${tag}.glb  (${(ab.byteLength / 1024).toFixed(1)} KB, ` +
        `config ${L}m × ${nL} líneas × ${nN} niveles)`);
}
