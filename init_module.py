import os
import shutil

base = r"C:\Users\XDsir\.gemini\antigravity\scratch\tiktok_dota_skins\module"
dirs = [
    os.path.join(base, "src", "main", "java", "com", "dota", "tiktokskins"),
    os.path.join(base, "src", "main", "java", "de", "robv", "android", "xposed", "callbacks"),
    os.path.join(base, "src", "main", "res", "values"),
    os.path.join(base, "src", "main", "res", "layout"),
    os.path.join(base, "src", "main", "res", "drawable"),
    os.path.join(base, "src", "main", "assets", "skins"),
    os.path.join(base, "bin")
]

for d in dirs:
    os.makedirs(d, exist_ok=True)

# Copy the 12 master skins into module/src/main/assets/skins/
skins_src = r"C:\Users\XDsir\.gemini\antigravity\scratch\tiktok_dota_skins\skins"
assets_dest = os.path.join(base, "src", "main", "assets", "skins")

for f in os.listdir(skins_src):
    if f.endswith((".jpg", ".png")):
        shutil.copy2(os.path.join(skins_src, f), os.path.join(assets_dest, f))

print("Module directories initialized and skins copied!")
