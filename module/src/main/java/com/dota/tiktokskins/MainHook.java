package com.dota.tiktokskins;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    private static final String TAG = "TikTokDotaHook";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.zhiliaoapp.musically") &&
            !lpparam.packageName.equals("com.ss.android.ugc.trill")) {
            return;
        }

        XposedBridge.log("[+] TikTok Dota Skins Module successfully loaded into: " + lpparam.packageName);

        // Hook Activity onResume to monitor chat / streak views
        try {
            Class<?> activityClass = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(activityClass, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    final Activity act = (Activity) param.thisObject;
                    final String actName = act.getClass().getName();

                    // Check if current screen is chat or message view
                    if (actName.toLowerCase().contains("chat") || 
                        actName.toLowerCase().contains("im") || 
                        actName.toLowerCase().contains("streak") ||
                        actName.toLowerCase().contains("message")) {
                        
                        // Attach long-click / click to streak icons
                        act.getWindow().getDecorView().post(new Runnable() {
                            @Override
                            public void run() {
                                attachStreakPicker(act);
                            }
                        });
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    private void attachStreakPicker(final Activity activity) {
        try {
            View decor = activity.getWindow().getDecorView();
            scanAndHookStreakViews(activity, decor);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    private void scanAndHookStreakViews(final Activity act, View view) {
        if (view == null) return;

        // Check if view matches streak flame icon identifiers
        String desc = view.getContentDescription() != null ? view.getContentDescription().toString().toLowerCase() : "";
        String tag = view.getTag() != null ? view.getTag().toString().toLowerCase() : "";
        String resName = "";
        try {
            if (view.getId() != View.NO_ID && view.getResources() != null) {
                resName = view.getResources().getResourceEntryName(view.getId()).toLowerCase();
            }
        } catch (Exception ignored) {}

        boolean isStreak = desc.contains("streak") || desc.contains("огон") ||
                           tag.contains("streak") || resName.contains("streak") ||
                           resName.contains("fire") || resName.contains("flame");

        if (isStreak && view instanceof ImageView) {
            final ImageView iv = (ImageView) view;
            
            // Apply current skin immediately
            applyCurrentSkin(act, iv);

            // Hook click to open skin picker
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SkinPickerDialog.show(act, new SkinPickerDialog.OnSkinSelectedListener() {
                        @Override
                        public void onSkinSelected(DotaSkinManager.Hero hero) {
                            applyCurrentSkin(act, iv);
                        }
                    });
                }
            });
            return;
        }

        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup vg = (android.view.ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                scanAndHookStreakViews(act, vg.getChildAt(i));
            }
        }
    }

    private void applyCurrentSkin(Activity act, ImageView iv) {
        try {
            String heroId = DotaSkinManager.getSelectedHeroId(act);
            Bitmap bmp = DotaSkinManager.loadHeroBitmap(act, heroId);
            if (bmp != null) {
                iv.setImageBitmap(bmp);
            }
        } catch (Exception e) {
            XposedBridge.log(e);
        }
    }
}
