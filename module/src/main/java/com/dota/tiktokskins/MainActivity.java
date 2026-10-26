package com.dota.tiktokskins;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {

    private ImageView previewImg;
    private TextView heroNameTxt;
    private TextView stageTxt;
    private TextView daysTxt;
    private String currentHeroId = "cm";
    private int currentDays = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentHeroId = DotaSkinManager.getSelectedHeroId(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#09090c"));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(36, 48, 36, 48);
        scroll.addView(layout);

        // App Title
        TextView title = new TextView(this);
        title.setText("🔥 TikTok Dota 2 Streak Skins");
        title.setTextSize(22f);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 8);
        layout.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Менеджер и переключатель скинов огонька для TikTok");
        sub.setTextSize(13f);
        sub.setTextColor(Color.parseColor("#888899"));
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 32);
        layout.addView(sub);

        // Preview Card
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(32, 32, 32, 32);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#15161e"));
        cardBg.setCornerRadius(28f);
        cardBg.setStroke(2, Color.parseColor("#272935"));
        card.setBackground(cardBg);

        previewImg = new ImageView(this);
        previewImg.setLayoutParams(new LinearLayout.LayoutParams(280, 280));
        card.addView(previewImg);

        heroNameTxt = new TextView(this);
        heroNameTxt.setTextSize(18f);
        heroNameTxt.setTextColor(Color.WHITE);
        heroNameTxt.setPadding(0, 16, 0, 4);
        card.addView(heroNameTxt);

        stageTxt = new TextView(this);
        stageTxt.setTextSize(13f);
        stageTxt.setTextColor(Color.parseColor("#ffd700"));
        stageTxt.setPadding(0, 0, 0, 16);
        card.addView(stageTxt);

        daysTxt = new TextView(this);
        daysTxt.setTextSize(14f);
        daysTxt.setTextColor(Color.parseColor("#fe2c55"));
        card.addView(daysTxt);

        SeekBar slider = new SeekBar(this);
        slider.setMax(200);
        slider.setProgress(currentDays);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentDays = Math.max(1, progress);
                updatePreview();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        card.addView(slider);

        layout.addView(card);

        // Section Title: Heroes
        TextView heroesLabel = new TextView(this);
        heroesLabel.setText("\nВыбери активного персонажа (12 героев):");
        heroesLabel.setTextSize(15f);
        heroesLabel.setTextColor(Color.WHITE);
        heroesLabel.setPadding(0, 24, 0, 16);
        layout.addView(heroesLabel);

        final List<DotaSkinManager.Hero> heroes = DotaSkinManager.getAllHeroes();
        GridView grid = new GridView(this);
        grid.setNumColumns(3);
        grid.setVerticalSpacing(20);
        grid.setHorizontalSpacing(20);

        final BaseAdapter adapter = new BaseAdapter() {
            @Override public int getCount() { return heroes.size(); }
            @Override public Object getItem(int pos) { return heroes.get(pos); }
            @Override public long getItemId(int pos) { return pos; }

            @Override
            public View getView(int pos, View cv, ViewGroup parent) {
                final DotaSkinManager.Hero h = heroes.get(pos);
                LinearLayout item = new LinearLayout(MainActivity.this);
                item.setOrientation(LinearLayout.VERTICAL);
                item.setGravity(Gravity.CENTER);
                item.setPadding(12, 12, 12, 12);

                GradientDrawable itemBg = new GradientDrawable();
                boolean isSel = h.id.equalsIgnoreCase(currentHeroId);
                itemBg.setColor(Color.parseColor(isSel ? "#321623" : "#1a1b24"));
                itemBg.setCornerRadius(16f);
                if (isSel) {
                    itemBg.setStroke(3, Color.parseColor("#fe2c55"));
                }
                item.setBackground(itemBg);

                ImageView iv = new ImageView(MainActivity.this);
                iv.setLayoutParams(new LinearLayout.LayoutParams(110, 110));
                Bitmap b = DotaSkinManager.loadHeroBitmap(MainActivity.this, h.id);
                if (b != null) iv.setImageBitmap(b);
                item.addView(iv);

                TextView tv = new TextView(MainActivity.this);
                tv.setText(h.name);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(11f);
                tv.setMaxLines(1);
                tv.setPadding(0, 6, 0, 0);
                item.addView(tv);

                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentHeroId = h.id;
                        DotaSkinManager.setSelectedHeroId(MainActivity.this, h.id);
                        Toast.makeText(MainActivity.this, "Активный скин: " + h.name, Toast.LENGTH_SHORT).show();
                        updatePreview();
                        notifyDataSetChanged();
                    }
                });

                return item;
            }
        };

        grid.setAdapter(adapter);
        layout.addView(grid, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 850));

        // Quick In-App Picker Button
        TextView openPickerBtn = new TextView(this);
        openPickerBtn.setText("Открыть всплывающее меню (Bottom Sheet Test)");
        openPickerBtn.setTextSize(14f);
        openPickerBtn.setTextColor(Color.WHITE);
        openPickerBtn.setGravity(Gravity.CENTER);
        openPickerBtn.setPadding(0, 28, 0, 28);
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.parseColor("#fe2c55"));
        btnBg.setCornerRadius(20f);
        openPickerBtn.setBackground(btnBg);
        openPickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SkinPickerDialog.show(MainActivity.this, new SkinPickerDialog.OnSkinSelectedListener() {
                    @Override
                    public void onSkinSelected(DotaSkinManager.Hero hero) {
                        currentHeroId = hero.id;
                        updatePreview();
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        layout.addView(openPickerBtn);

        setContentView(scroll);
        updatePreview();
    }

    private void updatePreview() {
        DotaSkinManager.Hero h = DotaSkinManager.getHeroById(currentHeroId);
        heroNameTxt.setText(h.name + " (" + h.element + ")");
        int stage = DotaSkinManager.getStageByDays(currentDays);
        stageTxt.setText(DotaSkinManager.getStageDescription(stage));
        daysTxt.setText("Стрик: " + currentDays + " дн.");

        Bitmap bmp = DotaSkinManager.loadHeroBitmap(this, currentHeroId);
        if (bmp != null) {
            previewImg.setImageBitmap(bmp);
        }
    }
}
