package com.fitnessdb.util;

import java.awt.*;

public class Theme {
    // ── Backgrounds
    public static final Color BG       = new Color(0x080A0F);
    public static final Color SURFACE  = new Color(0x0D1017);
    public static final Color SURFACE2 = new Color(0x131720);
    public static final Color SURFACE3 = new Color(0x1A2030);
    public static final Color SURFACE4 = new Color(0x202840);

    // ── Borders
    public static final Color BORDER   = new Color(0x1E2535);
    public static final Color BORDER2  = new Color(0x283045);
    public static final Color BORDER3  = new Color(0x344060);

    // ── Accents
    public static final Color ACCENT    = new Color(0xC8FF00);
    public static final Color ACCENT_DIM= new Color(0x9FCC00);
    public static final Color ACCENT2   = new Color(0x00F5C8);
    public static final Color ACCENT3   = new Color(0xFF4F6B);
    public static final Color ACCENT4   = new Color(0x6C7FFF);

    // ── Text  — TEXT must always be readable on SURFACE/SURFACE2/BG
    public static final Color TEXT     = new Color(0xF4F7FF);
    public static final Color TEXT2    = new Color(0xA0AABF);   // brightened from 0x7A85A8
    public static final Color TEXT3    = new Color(0x5A6580);   // brightened from 0x3F4A68

    // ── Semantic
    public static final Color SUCCESS  = new Color(0x39D98A);
    public static final Color WARNING  = new Color(0xFFB020);
    public static final Color DANGER   = new Color(0xFF4F6B);
    public static final Color INFO     = new Color(0x40A9FF);

    // ── Fonts — everything larger
    public static final Font FONT_LOGO    = new Font("Segoe UI", Font.BOLD,  17);
    public static final Font FONT_LOGO_S  = new Font("Consolas", Font.PLAIN, 11);
    public static final Font FONT_NAV     = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font FONT_NAV_LBL = new Font("Consolas", Font.BOLD,  12);
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  24);
    public static final Font FONT_SUBTITLE= new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL   = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 13);
    public static final Font FONT_KPI     = new Font("Segoe UI", Font.BOLD,  38);
    public static final Font FONT_BADGE   = new Font("Consolas", Font.BOLD,  11);
    public static final Font FONT_TH      = new Font("Consolas", Font.BOLD,  11);
    public static final Font FONT_BREAD   = new Font("Segoe UI", Font.BOLD,  15);

    public static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    public static Color lighten(Color c, float factor) {
        int r = (int)(c.getRed()   + (255 - c.getRed())   * factor);
        int g = (int)(c.getGreen() + (255 - c.getGreen()) * factor);
        int b = (int)(c.getBlue()  + (255 - c.getBlue())  * factor);
        return new Color(Math.min(r,255), Math.min(g,255), Math.min(b,255));
    }
}
