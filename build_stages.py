import os
from PIL import Image, ImageEnhance, ImageFilter

input_dir = r"C:\Users\XDsir\.gemini\antigravity\scratch\tiktok_dota_skins\skins"
output_dir = r"C:\Users\XDsir\.gemini\antigravity\scratch\tiktok_dota_skins\stages"
os.makedirs(output_dir, exist_ok=True)

heroes = [
    ("cm", "Crystal Maiden"),
    ("lina", "Lina"),
    ("qop", "Queen of Pain"),
    ("luna", "Luna"),
    ("mirana", "Mirana"),
    ("vengeful", "Vengeful Spirit"),
    ("sf", "Shadow Fiend"),
    ("necro", "Necrophos"),
    ("weaver", "Weaver"),
    ("ogre", "Ogre Magi"),
    ("huskar", "Huskar"),
    ("invoker", "Invoker")
]

for hero_prefix, hero_name in heroes:
    fname = f"{hero_prefix}_flame_concept.jpg"
    src_path = os.path.join(input_dir, fname)
    if not os.path.exists(src_path):
        continue
    
    base_img = Image.open(src_path).convert("RGBA")
    hero_stage_dir = os.path.join(output_dir, hero_prefix)
    os.makedirs(hero_stage_dir, exist_ok=True)
    
    # Stage 1: Spark (3-7 days) - scaled down, soft glow
    s1 = base_img.copy().resize((600, 600), Image.Resampling.LANCZOS)
    canvas1 = Image.new("RGBA", (1024, 1024), (0, 0, 0, 0))
    canvas1.paste(s1, (212, 350))
    # Soften
    enhancer1 = ImageEnhance.Color(canvas1)
    canvas1 = enhancer1.enhance(0.85)
    canvas1.save(os.path.join(hero_stage_dir, "stage_1_3days.png"))

    # Stage 2: Growing Flame (8-29 days) - 80% scale
    s2 = base_img.copy().resize((820, 820), Image.Resampling.LANCZOS)
    canvas2 = Image.new("RGBA", (1024, 1024), (0, 0, 0, 0))
    canvas2.paste(s2, (102, 160))
    canvas2.save(os.path.join(hero_stage_dir, "stage_2_10days.png"))

    # Stage 3: Full Flame (30-99 days) - 100% full detail
    base_img.save(os.path.join(hero_stage_dir, "stage_3_50days.png"))

    # Stage 4: Epic Blazing Flame (100+ days) - enhanced brilliance & contrast
    enhancer_color = ImageEnhance.Color(base_img)
    epic = enhancer_color.enhance(1.25)
    enhancer_bright = ImageEnhance.Brightness(epic)
    epic = enhancer_bright.enhance(1.15)
    epic.save(os.path.join(hero_stage_dir, "stage_4_100days.png"))

    print(f"Generated 4 stages for {hero_name} ({hero_prefix})")

print("All hero stages created successfully!")
