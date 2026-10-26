import os
import sys
import subprocess
import shutil
import zipfile

MODULE_DIR = r"C:\Users\XDsir\.gemini\antigravity\scratch\tiktok_dota_skins\module"
BIN_DIR = os.path.join(MODULE_DIR, "bin")
SRC_DIR = os.path.join(MODULE_DIR, "src", "main")
RES_DIR = os.path.join(SRC_DIR, "res")
JAVA_DIR = os.path.join(SRC_DIR, "java")
ASSETS_DIR = os.path.join(SRC_DIR, "assets")
MANIFEST = os.path.join(SRC_DIR, "AndroidManifest.xml")

SDK_ROOT = r"C:\Users\XDsir\AppData\Local\Android\Sdk"
BUILD_TOOLS = os.path.join(SDK_ROOT, "build-tools", "35.0.0")
AAPT2 = os.path.join(BUILD_TOOLS, "aapt2.exe")
D8 = os.path.join(BUILD_TOOLS, "d8.bat")
APKSIGNER = os.path.join(BUILD_TOOLS, "apksigner.bat")
ANDROID_JAR = os.path.join(SDK_ROOT, "platforms", "android-35", "android.jar")
ADB = os.path.join(SDK_ROOT, "platform-tools", "adb.exe")

JAVA_HOME = r"C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
JAVAC = os.path.join(JAVA_HOME, "bin", "javac.exe")
KEYTOOL = os.path.join(JAVA_HOME, "bin", "keytool.exe")

os.makedirs(BIN_DIR, exist_ok=True)
OBJ_DIR = os.path.join(BIN_DIR, "obj")
CLASSES_DIR = os.path.join(BIN_DIR, "classes")
os.makedirs(OBJ_DIR, exist_ok=True)
os.makedirs(CLASSES_DIR, exist_ok=True)

print("=" * 60)
print("   BUILDING TIKTOK DOTA 2 SKINS ANDROID APK / MODULE   ")
print("=" * 60)

# Step 1: AAPT2 compile resources
print("[1] Compiling Android resources with AAPT2...")
compiled_res_zip = os.path.join(OBJ_DIR, "compiled_res.zip")
cmd = [AAPT2, "compile", "--dir", RES_DIR, "-o", compiled_res_zip]
r = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
if r.returncode != 0:
    print("AAPT2 compile failed:\n", r.stderr)
    sys.exit(1)

# Step 2: AAPT2 link to generate R.java and base APK (without -A to avoid Windows backslash bug)
print("[2] Linking resources and generating R.java...")
base_apk = os.path.join(BIN_DIR, "base.apk")
r_java_dir = os.path.join(BIN_DIR, "gen")
os.makedirs(r_java_dir, exist_ok=True)

cmd = [
    AAPT2, "link",
    "-I", ANDROID_JAR,
    "--manifest", MANIFEST,
    "--java", r_java_dir,
    "-o", base_apk,
    compiled_res_zip
]
r = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
if r.returncode != 0:
    print("AAPT2 link failed:\n", r.stderr)
    sys.exit(1)

# Step 3: Find all Java files
print("[3] Compiling Java source files with javac...")
java_files = []
for root, _, files in os.walk(JAVA_DIR):
    for f in files:
        if f.endswith(".java"):
            java_files.append(os.path.join(root, f))
for root, _, files in os.walk(r_java_dir):
    for f in files:
        if f.endswith(".java"):
            java_files.append(os.path.join(root, f))

# Write sources list to file
sources_list_file = os.path.join(BIN_DIR, "sources.txt")
with open(sources_list_file, "w", encoding="utf-8") as f:
    for jf in java_files:
        f.write(jf.replace("\\", "/") + "\n")

cmd = [
    JAVAC,
    "-encoding", "utf-8",
    "-source", "1.8",
    "-target", "1.8",
    "-cp", ANDROID_JAR,
    "-d", CLASSES_DIR,
    f"@{sources_list_file}"
]
r = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
if r.returncode != 0:
    print("javac compilation failed:\n", r.stderr)
    sys.exit(1)
print(f"    Compiled {len(java_files)} Java source files successfully!")

# Step 4: D8 Dexing
print("[4] Compiling bytecode to classes.dex with D8...")
class_files = []
for root, _, files in os.walk(CLASSES_DIR):
    for f in files:
        if f.endswith(".class"):
            class_files.append(os.path.join(root, f))

cmd = [
    "cmd.exe", "/c", D8,
    "--output", BIN_DIR,
    "--lib", ANDROID_JAR,
    "--min-api", "24"
] + class_files

r = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
if r.returncode != 0:
    print("D8 dexing failed:\n", r.stderr)
    sys.exit(1)

classes_dex = os.path.join(BIN_DIR, "classes.dex")
if not os.path.exists(classes_dex):
    print("classes.dex was not generated!")
    sys.exit(1)
print(f"    classes.dex created ({os.path.getsize(classes_dex)} bytes)")

# Step 5: Add classes.dex and assets into APK
print("[5] Injecting classes.dex and assets into base.apk...")
unaligned_apk = os.path.join(BIN_DIR, "unaligned.apk")
shutil.copyfile(base_apk, unaligned_apk)

with zipfile.ZipFile(unaligned_apk, 'a') as z:
    z.write(classes_dex, "classes.dex")
    for root, _, files in os.walk(ASSETS_DIR):
        for f in files:
            full_path = os.path.join(root, f)
            rel_path = os.path.relpath(full_path, SRC_DIR).replace("\\", "/")
            z.write(full_path, rel_path)
print("    Injected classes.dex and assets with normalized Unix paths.")

# Step 6: Create debug keystore if not exists
keystore_path = os.path.join(BIN_DIR, "debug.keystore")
if not os.path.exists(keystore_path):
    print("[6] Creating debug signing keystore...")
    cmd = [
        KEYTOOL,
        "-genkeypair",
        "-keystore", keystore_path,
        "-storepass", "android",
        "-alias", "androiddebugkey",
        "-keypass", "android",
        "-dname", "CN=Android Debug,O=Android,C=US",
        "-validity", "10000",
        "-keyalg", "RSA",
        "-keysize", "2048"
    ]
    subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True)

# Step 7: Sign APK
print("[7] Signing APK with apksigner...")
final_apk = os.path.join(BIN_DIR, "TikTokDotaSkins.apk")
cmd = [
    "cmd.exe", "/c", APKSIGNER, "sign",
    "--ks", keystore_path,
    "--ks-pass", "pass:android",
    "--ks-key-alias", "androiddebugkey",
    "--key-pass", "pass:android",
    "--out", final_apk,
    unaligned_apk
]
r = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
if r.returncode != 0:
    print("apksigner failed:\n", r.stderr)
    sys.exit(1)

print("\n" + "=" * 60)
print(f"[SUCCESS] APK Built Successfully: {final_apk}")
print(f"APK Size: {os.path.getsize(final_apk) / 1024:.1f} KB")
print("=" * 60)

# Step 8: Check if phone is connected via ADB and install!
print("\n[8] Checking connected ADB device for automatic installation...")
try:
    devs = subprocess.run([ADB, "devices"], stdout=subprocess.PIPE, text=True)
    if "device" in devs.stdout and "unauthorized" not in devs.stdout:
        print("[+] Device ready, installing APK onto phone...")
        inst = subprocess.run([ADB, "install", "-r", final_apk], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        print("ADB Install Output:", inst.stdout.strip())
        if inst.returncode == 0:
            print("[+] APK installed on Samsung phone successfully!")
    else:
        print("[-] Device not ready for installation. Output:\n", devs.stdout)
except Exception as e:
    print("ADB install check error:", e)
