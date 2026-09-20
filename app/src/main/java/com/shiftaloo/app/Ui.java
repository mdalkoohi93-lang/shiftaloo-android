package com.shiftaloo.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

/** Offline design tokens shared by every native screen. Icons: Phosphor (MIT). */
final class Ui {
    static final int INK=0xff123968, MUTED=0xff657c9c, BG=0xfffffaf5, PINK=0xffdf2456,
        LINE=0xffdfe5ee, MINT=0xffdff9f1, PEACH=0xfffff0df, LILAC=0xffefe7ff, SKY=0xffe1f3ff;
    static final String HOME="\ue2c2", CALENDAR="\ue714", REPORT="\ue150", HOSPITAL="\ue844",
        SUN="\ue472", SUNSET="\ue5b6", MOON="\ue330", CLOCK="\ue19a", PIN="\ue316",
        BOLT="\ue2de", BELL="\ue0ce", PLUS="\ue3d4", CHECK="\ue182", CLOSE="\ue4f6",
        LEFT="\ue138", RIGHT="\ue13a", DOWN="\ue136", COINS="\ue78e", FILTER="\ue266",
        TRASH="\ue4a6", GEAR="\ue270", HEART="\ue2a8";
    final Context c; final Typeface normal,bold,icons,outline;
    Ui(Context context) { c=context; normal=Typeface.createFromAsset(c.getAssets(),"fonts/Vazirmatn-Regular.ttf");
        bold=Typeface.createFromAsset(c.getAssets(),"fonts/Vazirmatn-Bold.ttf");
        icons=Typeface.createFromAsset(c.getAssets(),"fonts/Phosphor-Fill.ttf");
        outline=Typeface.createFromAsset(c.getAssets(),"fonts/Phosphor.ttf"); }
    int dp(float n) { return Math.round(n*c.getResources().getDisplayMetrics().density); }
    LinearLayout col() { LinearLayout v=new LinearLayout(c); v.setOrientation(1); v.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return v; }
    LinearLayout row() { LinearLayout v=new LinearLayout(c); v.setOrientation(0); v.setGravity(Gravity.CENTER_VERTICAL); v.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return v; }
    TextView text(String s,float size,int color,boolean strong) { TextView v=new TextView(c); v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setTypeface(strong?bold:normal);v.setIncludeFontPadding(false);v.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);v.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_RTL);return v; }
    TextView icon(String glyph,int size,int color,boolean fill) { TextView v=text(glyph,size,color,false);v.setTypeface(fill?icons:outline);v.setGravity(Gravity.CENTER);v.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);return v; }
    GradientDrawable bg(int color,int radius,int stroke) { GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d; }
    void touch(View v,int color,int radius,int stroke) { v.setBackground(new RippleDrawable(ColorStateList.valueOf(0x180d3968),bg(color,radius,stroke),null));v.setFocusable(true);v.setClickable(true); }
    LinearLayout card(int color) { LinearLayout v=col();v.setPadding(dp(14),dp(12),dp(14),dp(12));v.setBackground(bg(color,20,color==Color.WHITE?LINE:0));return v; }
    LinearLayout.LayoutParams full(int height) { return new LinearLayout.LayoutParams(-1,height<0?height:dp(height)); }
    LinearLayout.LayoutParams weight(int height) { return new LinearLayout.LayoutParams(0,height<0?height:dp(height),1); }
    LinearLayout.LayoutParams gap(int height,int top) { LinearLayout.LayoutParams p=full(height);p.topMargin=dp(top);return p; }
    void gap(LinearLayout parent,int height) { parent.addView(new View(c),full(height)); }
    View line() { View v=new View(c);v.setBackgroundColor(LINE);return v; }
    TextView iconButton(String glyph,String description,Runnable click) { TextView v=icon(glyph,22,INK,false);v.setContentDescription(description);v.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);touch(v,Color.TRANSPARENT,12,0);v.setOnClickListener(w->click.run());v.setMinimumWidth(dp(48));v.setMinimumHeight(dp(48));return v; }
    LinearLayout button(String label,String glyph,boolean primary,Runnable click) { LinearLayout b=row();b.setGravity(Gravity.CENTER);b.setMinimumHeight(dp(50));b.setPadding(dp(10),dp(8),dp(10),dp(8));touch(b,primary?PINK:Color.WHITE,14,primary?0:LINE);if(glyph!=null){b.addView(icon(glyph,23,primary?Color.WHITE:INK,false),new LinearLayout.LayoutParams(dp(28),dp(28)));} TextView t=text(label,14,primary?Color.WHITE:INK,true);t.setPadding(dp(7),0,dp(7),0);b.addView(t);b.setContentDescription(label);b.setOnClickListener(v->click.run());return b; }
    EditText input(String value,String hint,boolean number) { EditText v=new EditText(c);v.setText(value);v.setHint(hint);v.setTextColor(INK);v.setHintTextColor(MUTED);v.setTextSize(14);v.setTypeface(normal);v.setPadding(dp(12),dp(10),dp(12),dp(10));v.setBackground(bg(Color.WHITE,12,LINE));v.setSingleLine(true);v.setMinimumHeight(dp(48));v.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);v.setInputType(number?InputType.TYPE_CLASS_NUMBER:InputType.TYPE_CLASS_TEXT);return v; }
    void field(LinearLayout parent,String label,View v) { TextView t=text(label,12,INK,false);t.setPadding(dp(2),dp(8),dp(2),dp(5));parent.addView(t,full(-2));parent.addView(v,full(-2));t.setLabelFor(v.getId()); }
    ImageView art(int resource) { ImageView v=new ImageView(c);v.setImageResource(resource);v.setScaleType(ImageView.ScaleType.FIT_CENTER);v.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);return v; }
    void fieldIcon(TextView v,String glyph) { int size=dp(21); android.graphics.Bitmap bitmap=android.graphics.Bitmap.createBitmap(size,size,android.graphics.Bitmap.Config.ARGB_8888);android.graphics.Canvas canvas=new android.graphics.Canvas(bitmap);android.graphics.Paint paint=new android.graphics.Paint(3);paint.setTypeface(outline);paint.setTextSize(size);paint.setColor(MUTED);paint.setTextAlign(android.graphics.Paint.Align.CENTER);android.graphics.Paint.FontMetrics fm=paint.getFontMetrics();canvas.drawText(glyph,size/2f,size/2f-(fm.ascent+fm.descent)/2,paint);android.graphics.drawable.BitmapDrawable d=new android.graphics.drawable.BitmapDrawable(c.getResources(),bitmap);d.setBounds(0,0,size,size);v.setCompoundDrawables(null,null,d,null);v.setCompoundDrawablePadding(dp(8)); }
    static int typeColor(String type) { return Shift.TYPE_NIGHT.equals(type)?LILAC:Shift.TYPE_EVENING.equals(type)?PEACH:MINT; }
    static int typeInk(String type) { return Shift.TYPE_NIGHT.equals(type)?0xff8c63d5:Shift.TYPE_EVENING.equals(type)?0xffcd762d:0xff009e8c; }
    static String typeIcon(String type) { return Shift.TYPE_NIGHT.equals(type)?MOON:Shift.TYPE_EVENING.equals(type)?SUNSET:SUN; }
}
