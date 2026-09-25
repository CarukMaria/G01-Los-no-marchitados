# Genera modelos USDZ para AR Quick Look (iOS), a escala métrica real.
# Espeja la geometría de tools/generar_modelo.mjs (mismos colores y constantes).
# Estructura: cada prim ancla = Xform con solo translate; la geometría hija lleva
# scale/radio, así la composición de transformaciones queda sin ambigüedad.
# Salida: modelos/<tag>.usdz (zip que contiene model.usdc, formato Crate).
import os
import shutil
import sys
import tempfile
import zipfile

# Asegurarse de que el .venv (que tiene usd-core) esté en el path cuando el
# script se corra como `python3 tools/generar_usdz.py` desde la raíz.
from pxr import Sdf, Usd, UsdGeom, UsdShade, UsdUtils

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(os.path.dirname(HERE), "modelos")
os.makedirs(OUT, exist_ok=True)

# Compartidas con generar_modelo.mjs (metros)
SEP_Y = 0.45
SEP_Z = 0.42
BASE_Z = 0.45

PRESETS = [
    # (tag, L, nN, nL)
    ("hidroponia-compact", 1.2, 2, 2),
    ("hidroponia", 2.4, 3, 3),
    ("hidroponia-36", 3.6, 4, 4),
    ("hidroponia-48", 4.8, 4, 6),
]

MATERIALS = {
    # nombre: (color rgb 0-1, metalness, roughness, opacity)
    "pipe":   ((0.945, 0.961, 0.976), 0.1, 0.25, 1.0),
    "water":  ((0.133, 0.827, 0.933), 0.3, 0.1, 0.8),
    "frame":  ((0.278, 0.333, 0.412), 0.7, 0.5, 1.0),
    "plant":  ((0.133, 0.773, 0.369), 0.0, 0.6, 1.0),
    "tank":   ((0.118, 0.161, 0.231), 0.2, 0.4, 1.0),
    "pump":   ((0.008, 0.518, 0.780), 0.3, 0.4, 1.0),
}


def _material(stage, name):
    """Crea (o reutiliza por nombre) un material UsdPreviewSurface."""
    mat_path = f"/HidroPlan/_mats/{name}"
    if stage.GetPrimAtPath(mat_path).IsValid():
        return UsdShade.Material(stage.GetPrimAtPath(mat_path))
    rgb, metal, rough, opacity = MATERIALS[name]
    mat = UsdShade.Material.Define(stage, mat_path)
    pbr = UsdShade.Shader.Define(stage, mat_path + "/PBR")
    pbr.CreateIdAttr("UsdPreviewSurface")
    pbr.CreateInput("diffuseColor", Sdf.ValueTypeNames.Color3f).Set(rgb)
    pbr.CreateInput("metallic", Sdf.ValueTypeNames.Float).Set(metal)
    pbr.CreateInput("roughness", Sdf.ValueTypeNames.Float).Set(rough)
    if opacity < 1.0:
        pbr.CreateInput("opacity", Sdf.ValueTypeNames.Float).Set(opacity)
    mat.CreateSurfaceOutput().ConnectToSource(pbr.ConnectableAPI(), "surface")
    return mat


def _bind(prim, mat):
    UsdShade.MaterialBindingAPI.Apply(prim.GetPrim()).Bind(mat)


def _quad_faces(nu, nv):
    """Faces en formato quads para una grilla nu×nv (las 2 primeras columnas
    van numeradas como [i, i+1, i+nu+2, i+nu+1] respetando winding CCW +Y)."""
    faces = []
    for v in range(nv):
        for u in range(nu):
            a = v * (nu + 1) + u
            b = a + 1
            c = a + nu + 2
            d = a + nu + 1
            faces.append((a, b, c, d))
    return faces, [4] * len(faces)


def _mesh_ellipsoid(stage, path, radii=(0.044, 0.056, 0.044), nu=8, nv=6):
    """Elipsoide (esfera de elipsoide) como Mesh exacto, sin scale por xform.
    Normales por vértice = posición normalizada. Subdivision bilinear para que
    ComputeExtent devuelva bounds exactos."""
    import math
    rx, ry, rz = radii
    pts = []
    norms = []
    for v in range(nv + 1):
        phi = v * math.pi / nv
        for u in range(nu + 1):
            theta = u * 2 * math.pi / nu
            px = math.cos(theta) * math.sin(phi)
            py = math.cos(phi)
            pz = math.sin(theta) * math.sin(phi)
            pts.append((px * rx, py * ry, pz * rz))
            norms.append((px, py, pz))
    faces, counts = _quad_faces(nu, nv)
    mesh = UsdGeom.Mesh.Define(stage, path)
    mesh.CreatePointsAttr(pts)
    mesh.CreateFaceVertexCountsAttr(counts)
    mesh.CreateFaceVertexIndicesAttr([i for f in faces for i in f])
    mesh.CreateNormalsAttr(norms)
    mesh.SetNormalsInterpolation(UsdGeom.Tokens.varying)
    mesh.CreateSubdivisionSchemeAttr().Set("bilinear")
    return mesh


def _mesh_box(stage, path, half):
    hx, hy, hz = half
    pts = [(x * hx, y * hy, z * hz) for x in (-1, 1) for y in (-1, 1) for z in (-1, 1)]
    faces = [
        (0, 1, 3, 2),  # +x
        (4, 6, 7, 5),  # -x
        (0, 4, 5, 1),  # +y
        (2, 3, 7, 6),  # -y
        (0, 2, 6, 4),  # +z
        (1, 5, 7, 3),  # -z
    ]
    face_normals = [
        (1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1),
    ]
    mesh = UsdGeom.Mesh.Define(stage, path)
    mesh.CreatePointsAttr(pts)
    mesh.CreateFaceVertexCountsAttr([4] * 6)
    mesh.CreateFaceVertexIndicesAttr([i for f in faces for i in f])
    mesh.CreateNormalsAttr(face_normals * 4)
    mesh.SetNormalsInterpolation(UsdGeom.Tokens.faceVarying)
    mesh.CreateSubdivisionSchemeAttr().Set("bilinear")
    return mesh


def _anchor(stage, root_path):
    """Xform padre con translate (único op). La geometría va de hija."""
    xf = UsdGeom.Xform.Define(stage, root_path)
    return xf


def build(stage, tag, L, nN, nL):
    offsetX = -L / 2
    offsetZ = -((nL - 1) * SEP_Y) / 2
    topZ = BASE_Z + (nN - 1) * SEP_Z

    root = UsdGeom.Xform.Define(stage, "/HidroPlan")

    # 1. Postes verticales
    for i, xPos in enumerate([0, L] * nL):
        z = (i % nL) * SEP_Y + offsetZ
        pid = f"/HidroPlan/postes/p_{i}"
        parent = _anchor(stage, pid)
        parent.AddTranslateOp().Set((xPos + offsetX, (topZ + 0.3) / 2, z))
        cyl = UsdGeom.Cylinder.Define(stage, pid + "/geom")
        cyl.GetRadiusAttr().Set(0.025)
        cyl.GetHeightAttr().Set(topZ + 0.3)
        cyl.GetAxisAttr().Set("Y")
        _bind(cyl, _material(stage, "frame"))

    # 2. Travesaños horizontales por nivel
    idx = 0
    for j in range(nN):
        y = BASE_Z + j * SEP_Z
        for xPos in (0, L):
            pid = f"/HidroPlan/travesanos/t_{idx}"
            idx += 1
            parent = _anchor(stage, pid)
            parent.AddTranslateOp().Set((xPos + offsetX, y, ((nL - 1) * SEP_Y) / 2 + offsetZ))
            cyl = UsdGeom.Cylinder.Define(stage, pid + "/geom")
            cyl.GetRadiusAttr().Set(0.018)
            cyl.GetHeightAttr().Set((nL - 1) * SEP_Y + 0.1)
            cyl.GetAxisAttr().Set("Z")
            _bind(cyl, _material(stage, "frame"))

    # 3. Canales NFT + agua + plantines
    plant_idx = 0
    for j in range(nN):
        y = BASE_Z + j * SEP_Z + 0.05
        for i in range(nL):
            z = i * SEP_Y + offsetZ

            # canal (cilindro eje X: en USD "X" es el eje del cilindro)
            pid = f"/HidroPlan/canales/c_{j}_{i}"
            parent = _anchor(stage, pid)
            parent.AddTranslateOp().Set((0, y, z))
            cyl = UsdGeom.Cylinder.Define(stage, pid + "/geom")
            cyl.GetRadiusAttr().Set(0.045)
            cyl.GetHeightAttr().Set(L)
            cyl.GetAxisAttr().Set("X")
            _bind(cyl, _material(stage, "pipe"))

            # agua (radio 0.85×)
            pid = f"/HidroPlan/canales/w_{j}_{i}"
            parent = _anchor(stage, pid)
            parent.AddTranslateOp().Set((0, y + 0.01, z))
            cyl = UsdGeom.Cylinder.Define(stage, pid + "/geom")
            cyl.GetRadiusAttr().Set(0.045 * 0.85)
            cyl.GetHeightAttr().Set(L)
            cyl.GetAxisAttr().Set("X")
            _bind(cyl, _material(stage, "water"))

            # plantines (elipsoide mesh, radius 0.04 con estiramiento propio)
            plant_count = max(3, int(L / 0.25))
            for k in range(plant_count):
                px = -L / 2 + 0.15 + (k * (L - 0.3)) / max(1, plant_count - 1)
                pid = f"/HidroPlan/plantas/p_{plant_idx}"
                plant_idx += 1
                parent = _anchor(stage, pid)
                parent.AddTranslateOp().Set((px, y + 0.05, z))
                sph = _mesh_ellipsoid(stage, pid + "/geom")
                _bind(sph, _material(stage, "plant"))

    # 4. Tanque reservorio (caja mesh exacta, sin scale por xform)
    tank_w, tank_h, tank_d = 0.5, 0.55, (nL - 1) * SEP_Y + 0.35
    pid = "/HidroPlan/tanque"
    parent = _anchor(stage, pid)
    parent.AddTranslateOp().Set((offsetX - tank_w / 2 - 0.15, tank_h / 2, 0))
    cube = _mesh_box(stage, pid + "/geom",
                     (tank_w / 2, tank_h / 2, tank_d / 2))
    _bind(cube, _material(stage, "tank"))

    # 5. Bomba
    pid = "/HidroPlan/bomba"
    parent = _anchor(stage, pid)
    parent.AddTranslateOp().Set((offsetX - tank_w / 2 - 0.15, 0.12, 0))
    cube = _mesh_box(stage, pid + "/geom", (0.1, 0.1, 0.1))
    _bind(cube, _material(stage, "pump"))

    return root


def make_usdz(tag, L, nN, nL):
    tmp = tempfile.mkdtemp(prefix="hidroplan_usdz_")
    usdc_path = os.path.join(tmp, "model.usdc")
    usdz_path = os.path.join(OUT, f"{tag}.usdz")

    stage = Usd.Stage.CreateNew(usdc_path)
    UsdGeom.SetStageUpAxis(stage, UsdGeom.Tokens.y)
    UsdGeom.SetStageMetersPerUnit(stage, 1.0)
    stage.SetDefaultPrim(build(stage, tag, L, nN, nL).GetPrim())
    stage.Save()

    # Empaquetar a .usdz. CreateNewUsdzPackage puede fallar en crates "in-place";
    # fallback: zip estándar con la usdc en la raíz.
    try:
        UsdUtils.CreateNewUsdzPackage(usdc_path, usdz_path)
    except Exception:
        with zipfile.ZipFile(usdz_path, "w", zipfile.ZIP_DEFLATED) as zf:
            zf.write(usdc_path, "model.usdc")
    shutil.rmtree(tmp, ignore_errors=True)
    size_kb = os.path.getsize(usdz_path) / 1024
    print(f"USDZ OK  →  modelos/{tag}.usdz  ({size_kb:.1f} KB, config {L}m × {nL} × {nN})")


if __name__ == "__main__":
    for tag, L, nN, nL in PRESETS:
        make_usdz(tag, L, nN, nL)
    print("Listo.")