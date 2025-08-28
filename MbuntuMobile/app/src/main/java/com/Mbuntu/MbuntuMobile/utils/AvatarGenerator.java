package com.Mbuntu.MbuntuMobile.utils;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.content.res.Resources;

public class AvatarGenerator {

    private static final int[] MATERIAL_COLORS = {
            Color.parseColor("#e57373"), Color.parseColor("#f06292"), Color.parseColor("#ba68c8"),
            Color.parseColor("#9575cd"), Color.parseColor("#7986cb"), Color.parseColor("#64b5f6"),
            Color.parseColor("#4fc3f7"), Color.parseColor("#4dd0e1"), Color.parseColor("#4db6ac"),
            Color.parseColor("#81c784"), Color.parseColor("#aed581"), Color.parseColor("#ff8a65"),
            Color.parseColor("#d4e157"), Color.parseColor("#ffd54f"), Color.parseColor("#ffb74d")
    };

    public static BitmapDrawable getDrawable(String text) {
        String initial = "?";
        if (text != null && !text.trim().isEmpty()) {
            initial = String.valueOf(text.trim().charAt(0)).toUpperCase();
        }

        int color = MATERIAL_COLORS[Math.abs(text.hashCode()) % MATERIAL_COLORS.length];
        int size = 100; // Taille de l'avatar en pixels

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Dessiner le cercle de fond
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        background.setColor(color);
        background.setBounds(0, 0, size, size);
        background.draw(canvas);

        // Configurer le texte
        TextPaint paint = new TextPaint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(size * 0.5f); // Taille de la lettre = 50% de la taille de l'avatar
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);

        // Calculer la position pour centrer le texte verticalement
        float yPos = (canvas.getHeight() / 2f) - ((paint.descent() + paint.ascent()) / 2f);

        // Dessiner l'initiale
        canvas.drawText(initial, size / 2f, yPos, paint);

        return new BitmapDrawable(Resources.getSystem(), bitmap);
    }
}