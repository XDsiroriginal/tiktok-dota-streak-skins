package com.dota.tiktokskins;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class SkinPickerDialog {

    public interface OnSkinSelectedListener {
        void onSkinSelected(DotaSkinManager.Hero hero);
    }

    public static void show(Context context, final OnSkinSelectedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Root container (TikTok Dark theme: #121217)
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#121217"));
        root.setPadding(32, 32, 32, 32);

        // Header Title
        TextView title = new TextView(context);
        title.setText("🔥 Выбор скина огонька (Dota 2)");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18f);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 8);
        root.addView(title);

        // Subtitle
        TextView subtitle = new TextView(context);
        subtitle.setText("Нажми на героя, чтобы применить скин на серию");
        subtitle.setTextColor(Color.parseColor("#888899"));
        subtitle.setTextSize(12f);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 24);
        root.addView(subtitle);

        // Grid of Heroes (3 columns)
        GridView grid = new GridView(context);
        grid.setNumColumns(3);
        grid.setVerticalSpacing(20);
        grid.setHorizontalSpacing(20);
        grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);

        final List<DotaSkinManager.Hero> heroes = DotaSkinManager.getAllHeroes();
        final String currentHeroId = DotaSkinManager.getSelectedHeroId(context);

        builder.setView(root);
        final AlertDialog dialog = builder.create();

        grid.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() { return heroes.size(); }
            @Override
            public Object getItem(int position) { return heroes.get(position); }
            @Override
            public long getItemId(int position) { return position; }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final DotaSkinManager.Hero h = heroes.get(position);
                LinearLayout item = new LinearLayout(context);
                item.setOrientation(LinearLayout.VERTICAL);
                item.setGravity(Gravity.CENTER);
                item.setPadding(12, 12, 12, 12);

                GradientDrawable bg = new GradientDrawable();
                boolean isSelected = h.id.equalsIgnoreCase(currentHeroId);
                bg.setColor(Color.parseColor(isSelected ? "#2b1a28" : "#1e1f29"));
                bg.setCornerRadius(18f);
                if (isSelected) {
                    bg.setStroke(3, Color.parseColor("#fe2c55")); // TikTok Red
                }
                item.setBackground(bg);

                ImageView icon = new ImageView(context);
                icon.setLayoutParams(new LinearLayout.LayoutParams(110, 110));
                android.graphics.Bitmap bmp = DotaSkinManager.loadHeroBitmap(context, h.id);
                if (bmp != null) {
                    icon.setImageBitmap(bmp);
                }
                item.addView(icon);

                TextView name = new TextView(context);
                name.setText(h.name);
                name.setTextColor(Color.WHITE);
                name.setTextSize(11f);
                name.setGravity(Gravity.CENTER);
                name.setPadding(0, 6, 0, 0);
                name.setMaxLines(1);
                item.addView(name);

                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DotaSkinManager.setSelectedHeroId(context, h.id);
                        Toast.makeText(context, "Скин применён: " + h.name + "!", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onSkinSelected(h);
                        }
                        dialog.dismiss();
                    }
                });

                return item;
            }
        });

        root.addView(grid, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 800));

        dialog.show();
    }
}
