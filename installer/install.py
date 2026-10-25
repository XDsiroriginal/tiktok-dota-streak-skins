#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
TikTok Dota 2 Streak Flame Skins Installer & Manager
Created for automated deployment, asset generation, and phone management.
"""

import os
import sys
import subprocess
import shutil
import webbrowser
from PIL import Image

# Ensure UTF-8 output on Windows
if sys.platform == "win32":
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SKINS_DIR = os.path.join(BASE_DIR, "skins")
STAGES_DIR = os.path.join(BASE_DIR, "stages")
VIEWER_PATH = os.path.join(BASE_DIR, "viewer", "index.html")
BUILD_DIR = os.path.join(BASE_DIR, "build")

# Common ADB locations on Windows
ADB_CANDIDATES = [
    "adb",
    r"C:\Users\XDsir\AppData\Local\Android\Sdk\platform-tools\adb.exe",
    os.path.expandvars(r"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"),
    r"C:\platform-tools\adb.exe",
    r"C:\adb\adb.exe"
]

def find_adb():
    for p in ADB_CANDIDATES:
        try:
            r = subprocess.run([p, "version"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            if r.returncode == 0:
                return p
        except Exception:
            continue
    return None

ADB_BIN = find_adb() or "adb"

HEROES = [
    ("cm", "Crystal Maiden (Ледяной огонёк)"),
    ("lina", "Lina (Пылающий огонёк)"),
    ("qop", "Queen of Pain (Суккуб огонёк)"),
    ("luna", "Luna (Лунный огонёк)"),
    ("mirana", "Mirana (Звёздный огонёк)"),
    ("vengeful", "Vengeful Spirit (Призрачный огонёк)"),
    ("sf", "Shadow Fiend (Огонёк душ)"),
    ("necro", "Necrophos (Чумной огонёк)"),
    ("weaver", "Weaver (Временной огонёк)"),
    ("ogre", "Ogre Magi (Двухголовый огонёк)"),
    ("huskar", "Huskar (Огонёк берсерка)"),
    ("invoker", "Invoker (Гранд-маг огонёк)")
]

STAGES = [
    ("stage_1_3days.png", "Стадия 1: Искра (1–7 дней)"),
    ("stage_2_10days.png", "Стадия 2: Рост (8–29 дней)"),
    ("stage_3_50days.png", "Стадия 3: Зрелое пламя (30–99 дней)"),
    ("stage_4_100days.png", "Стадия 4: Эпическое сияние (100+ дней)")
]

def clear():
    os.system('cls' if os.name == 'nt' else 'clear')

def print_header():
    print("=" * 65)
    print("      TikTok Dota 2 Streak Flame Skins - Installer & Manager     ")
    print("=" * 65)
    print(f"Путь к проекту: {BASE_DIR}")
    print(f"ADB статус:     {'Найден (' + ADB_BIN + ')' if find_adb() else 'Не найден в PATH'}")
    print("-" * 65)

def check_device():
    print("\n[+] Сканирование подключенных Android устройств через ADB...")
    try:
        res = subprocess.run([ADB_BIN, "devices", "-l"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        lines = [l.strip() for l in res.stdout.splitlines() if l.strip() and not l.startswith("*")]
        print(res.stdout)
        if len(lines) <= 1:
            print("[-] Устройства не найдены. Убедитесь, что телефон подключен по USB и включена отладка!")
        else:
            print("[+] Устройство обнаружено успешно!")
    except Exception as e:
        print(f"[-] Ошибка выполнения ADB: {e}")
    input("\nНажмите Enter для возврата в меню...")

def check_tiktok():
    print("\n[+] Поиск установленного TikTok на телефоне...")
    try:
        packages = ["com.zhiliaoapp.musically", "com.ss.android.ugc.trill"]
        found = False
        for pkg in packages:
            r = subprocess.run([ADB_BIN, "shell", f"pm path {pkg}"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
            if "package:" in r.stdout:
                found = True
                print(f"[+] Найден пакет: {pkg}")
                print(f"    Путь в системе: {r.stdout.strip()}")
                ver = subprocess.run([ADB_BIN, "shell", f"dumpsys package {pkg} | grep versionName"], stdout=subprocess.PIPE, text=True)
                print(f"    Версия: {ver.stdout.strip()}")
        if not found:
            print("[-] Пакет TikTok не найден на подключенном устройстве.")
    except Exception as e:
        print(f"[-] Ошибка проверки пакета: {e}")
    input("\nНажмите Enter для возврата в меню...")

def export_assets():
    print("\n[+] Выбор персонажа для экспорта:")
    for idx, (hid, hname) in enumerate(HEROES, 1):
        print(f"  [{idx:2d}] {hname}")
    
    choice = input("\nВыберите номер героя (1-12) [1]: ").strip() or "1"
    try:
        hidx = int(choice) - 1
        hero_id, hero_name = HEROES[hidx]
    except Exception:
        print("[-] Неверный выбор.")
        input("\nНажмите Enter...")
        return

    print(f"\n[+] Выбран герой: {hero_name}")
    print("Выберите стадию:")
    for idx, (sfile, sname) in enumerate(STAGES, 1):
        print(f"  [{idx}] {sname}")
    schoice = input("\nВыберите стадию (1-4) [3]: ").strip() or "3"
    try:
        sidx = int(schoice) - 1
        stage_file, stage_name = STAGES[sidx]
    except Exception:
        print("[-] Неверный выбор.")
        input("\nНажмите Enter...")
        return

    src_stage_path = os.path.join(STAGES_DIR, hero_id, stage_file)
    if not os.path.exists(src_stage_path):
        print(f"[-] Исходный файл не найден: {src_stage_path}")
        input("\nНажмите Enter...")
        return

    # Generate Android density folders in build/
    export_dir = os.path.join(BUILD_DIR, f"{hero_id}_export")
    densities = {
        "drawable-mdpi": (48, 48),
        "drawable-hdpi": (72, 72),
        "drawable-xhdpi": (96, 96),
        "drawable-xxhdpi": (144, 144),
        "drawable-xxxhdpi": (192, 192),
        "raw_master_1024": (1024, 1024)
    }

    print(f"\n[+] Экспорт ассетов в {export_dir}...")
    base_img = Image.open(src_stage_path).convert("RGBA")
    for folder, dim in densities.items():
        target_dir = os.path.join(export_dir, folder)
        os.makedirs(target_dir, exist_ok=True)
        resized = base_img.resize(dim, Image.Resampling.LANCZOS)
        out_file = os.path.join(target_dir, "tiktok_streak_fire.png")
        resized.save(out_file, "PNG")
        print(f"  -> {folder}/tiktok_streak_fire.png ({dim[0]}x{dim[1]})")

    # Create zip archive
    zip_path = shutil.make_archive(export_dir, 'zip', export_dir)
    print(f"\n[+] Готовый архив ассетов: {zip_path}")
    print("[+] Готово! Эти ассеты можно использовать для LSPatch, Resource Redirection или ReVanced.")
    input("\nНажмите Enter для возврата в меню...")

def open_simulator():
    if os.path.exists(VIEWER_PATH):
        print("\n[+] Запуск интерактивного симулятора в браузере...")
        webbrowser.open(f"file:///{VIEWER_PATH.replace(os.sep, '/')}")
    else:
        print(f"[-] Файл симулятора не найден: {VIEWER_PATH}")
    input("\nНажмите Enter...")

def main():
    while True:
        clear()
        print_header()
        print("  [1] Проверить подключение Android / Samsung по ADB")
        print("  [2] Проверить установленный TikTok и его версию")
        print("  [3] Выбрать героя и экспортировать набор ассетов для телефона")
        print("  [4] Запустить интерактивный веб-симулятор в браузере")
        print("  [5] Перегенерировать все стадии для всех героев")
        print("  [0] Выход")
        print("-" * 65)
        ch = input("Выберите действие (0-5): ").strip()
        if ch == "1":
            check_device()
        elif ch == "2":
            check_tiktok()
        elif ch == "3":
            export_assets()
        elif ch == "4":
            open_simulator()
        elif ch == "5":
            print("\n[+] Запуск build_stages.py...")
            subprocess.run([sys.executable, os.path.join(BASE_DIR, "build_stages.py")])
            input("\nНажмите Enter...")
        elif ch == "0":
            print("\nДо свидания!")
            break

if __name__ == "__main__":
    main()
