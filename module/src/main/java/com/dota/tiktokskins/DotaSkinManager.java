package com.dota.tiktokskins;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DotaSkinManager {
    public static final String PREFS_NAME = "tiktok_dota_skins";
    public static final String KEY_HERO = "selected_hero";
    public static final String KEY_DAYS = "override_days";
    private static final String TAG = "DotaSkinManager";

    public static class Hero {
        public final String id;
        public final String name;
        public final String element;
        public final String assetName;

        public Hero(String id, String name, String element, String assetName) {
            this.id = id;
            this.name = name;
            this.element = element;
            this.assetName = assetName;
        }
    }

    private static final List<Hero> HEROES = new ArrayList<>();

    static {
        HEROES.add(new Hero("cm", "Crystal Maiden", "Ледяной огонёк Rylai", "cm_flame_concept.jpg"));
        HEROES.add(new Hero("lina", "Lina", "Пылающий огонёк Slayer", "lina_flame_concept.jpg"));
        HEROES.add(new Hero("qop", "Queen of Pain", "Демонический огонёк суккуба", "qop_flame_concept.jpg"));
        HEROES.add(new Hero("luna", "Luna", "Лунный огонёк Moon Rider", "luna_flame_concept.jpg"));
        HEROES.add(new Hero("mirana", "Mirana", "Звёздный огонёк жрицы", "mirana_flame_concept.jpg"));
        HEROES.add(new Hero("vengeful", "Vengeful Spirit", "Призрачный огонёк скайрас", "vengeful_flame_concept.jpg"));
        HEROES.add(new Hero("sf", "Shadow Fiend", "Огонёк душ Невермора", "sf_flame_concept.jpg"));
        HEROES.add(new Hero("necro", "Necrophos", "Чумной изумрудный огонёк", "necro_flame_concept.jpg"));
        HEROES.add(new Hero("weaver", "Weaver", "Временной огонёк Шукучи", "weaver_flame_concept.jpg"));
        HEROES.add(new Hero("ogre", "Ogre Magi", "Двухголовый огонёк удачи", "ogre_flame_concept.jpg"));
        HEROES.add(new Hero("huskar", "Huskar", "Огонёк берсерка Life Break", "huskar_flame_concept.jpg"));
        HEROES.add(new Hero("invoker", "Invoker", "Гранд-маг Quas-Wex-Exort", "invoker_flame_concept.jpg"));
    }

    public static List<Hero> getAllHeroes() {
        return HEROES;
    }

    public static Hero getHeroById(String id) {
        for (Hero h : HEROES) {
            if (h.id.equalsIgnoreCase(id)) return h;
        }
        return HEROES.get(0);
    }

    public static String getSelectedHeroId(Context ctx) {
        try {
            SharedPreferences sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return sp.getString(KEY_HERO, "cm");
        } catch (Exception e) {
            return "cm";
        }
    }

    public static void setSelectedHeroId(Context ctx, String heroId) {
        try {
            SharedPreferences sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            sp.edit().putString(KEY_HERO, heroId).apply();
            Log.i(TAG, "Selected hero saved: " + heroId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to save selected hero", e);
        }
    }

    public static int getStageByDays(int days) {
        if (days < 8) return 1;     // Искра
        if (days < 30) return 2;    // Рост
        if (days < 100) return 3;   // Зрелое пламя
        return 4;                   // Эпическое сияние
    }

    public static String getStageDescription(int stage) {
        switch (stage) {
            case 1: return "Стадия 1: Искра (1–7 дней)";
            case 2: return "Стадия 2: Рост (8–29 дней)";
            case 3: return "Стадия 3: Зрелое пламя (30–99 дней)";
            case 4: return "Стадия 4: Эпическое сияние (100+ дней)";
            default: return "Стадия 3";
        }
    }

    public static Bitmap loadHeroBitmap(Context ctx, String heroId) {
        Hero h = getHeroById(heroId);
        try {
            InputStream is = ctx.getAssets().open("skins/" + h.assetName);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();
            return bmp;
        } catch (Exception e) {
            Log.e(TAG, "Error loading asset for hero: " + heroId, e);
            return null;
        }
    }
}
