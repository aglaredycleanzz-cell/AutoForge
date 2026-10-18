package com.autoforge.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(15, 15, 17);
    private static final int PANEL = Color.rgb(29, 29, 33);
    private static final int RED = Color.rgb(229, 57, 53);
    private static final int WHITE = Color.rgb(248, 248, 250);
    private static final int MUTED = Color.rgb(170, 170, 180);
    private LinearLayout content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        showHome();
    }

    private void showHome() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(18), dp(20), dp(28));
        scroll.addView(content);
        setContentView(scroll);

        LinearLayout brandRow = new LinearLayout(this);
        brandRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView mark = text("AF", 18, RED, true);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(round(RED, 14));
        brandRow.addView(mark, new LinearLayout.LayoutParams(dp(52), dp(52)));
        LinearLayout brandText = new LinearLayout(this);
        brandText.setOrientation(LinearLayout.VERTICAL);
        brandText.setPadding(dp(12), 0, 0, 0);
        brandText.addView(text("AUTOFORGE", 22, WHITE, true));
        brandText.addView(text("مرآبك الذكي", 14, MUTED, false));
        brandRow.addView(brandText, new LinearLayout.LayoutParams(0, -2, 1));
        content.addView(brandRow);
        gap(content, 24);

        TextView hero = text("افهم سيارتك.\nطوّرها بثقة.", 30, WHITE, true);
        hero.setLineSpacing(dp(4), 1f);
        content.addView(hero);
        gap(content, 8);
        content.addView(text("منظور تفكيكي، رموز القطع، وأفكار التخصيص — في مكان واحد.", 15, MUTED, false));
        gap(content, 22);

        LinearLayout heroCard = panel();
        heroCard.setBackground(round(Color.rgb(39, 20, 22), 20, RED, 1));
        heroCard.addView(text("01  /  ورشة العمل", 13, RED, true));
        gap(heroCard, 10);
        heroCard.addView(text("ابدأ من مخطط السيارة", 21, WHITE, true));
        gap(heroCard, 6);
        heroCard.addView(text("نظّم القطع وحدّد ما تريد فحصه أو تغييره.", 14, MUTED, false));
        gap(heroCard, 16);
        Button explore = button("فتح مساحة العمل");
        explore.setOnClickListener(v -> openFeature("مساحة العمل", "ستُبنى هنا مساحة عرض القطع التفكيكية والبحث داخل المخططات."));
        heroCard.addView(explore);
        content.addView(heroCard);
        gap(content, 26);

        content.addView(text("الأدوات", 21, WHITE, true));
        gap(content, 12);
        addFeatureCard("⌘", "المنظور التفكيكي", "استعرض أجزاء السيارة وعلاقات تركيبها.", "المنظور التفكيكي");
        addFeatureCard("№", "OEM ورموز القطع", "سجّل أرقام القطع وملاحظاتك المرجعية.", "OEM ورموز القطع");
        addFeatureCard("◈", "الطلاء والتخصيص", "خطّط لمظهر خارجي بالأسود والأحمر.", "الطلاء والتخصيص");
        addFeatureCard("✓", "دليل الصيانة", "اجمع أعمال الفحص والإصلاح في قائمة واحدة.", "دليل الصيانة");

        gap(content, 22);
        TextView footer = text("AUTOFORGE  •  الإصدار 0.1.0", 11, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        content.addView(footer);
    }

    private void addFeatureCard(String icon, String title, String description, String feature) {
        LinearLayout card = panel();
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        TextView badge = text(icon, 22, RED, true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(round(Color.rgb(47, 28, 30), 12));
        card.addView(badge, new LinearLayout.LayoutParams(dp(52), dp(52)));
        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        words.setPadding(dp(12), 0, dp(8), 0);
        words.addView(text(title, 17, WHITE, true));
        gap(words, 4);
        words.addView(text(description, 13, MUTED, false));
        card.addView(words, new LinearLayout.LayoutParams(0, -2, 1));
        TextView arrow = text("‹", 26, RED, true);
        card.addView(arrow);
        card.setOnClickListener(v -> openFeature(feature,
                "هذه مساحة مبدئية. ستُضاف وظائف " + feature + " تدريجيًا مع حفظ بياناتك داخل التطبيق."));
        content.addView(card);
        gap(content, 10);
    }

    private void openFeature(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("حسنًا", (d, w) -> {})
                .show();
    }

    private LinearLayout panel() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(16), dp(16), dp(16));
        box.setBackground(round(PANEL, 18));
        return box;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(round(RED, 12));
        b.setPadding(dp(12), dp(7), dp(12), dp(7));
        return b;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.RIGHT);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setIncludeFontPadding(true);
        return t;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));
        return shape;
    }

    private GradientDrawable round(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable shape = round(color, radius);
        shape.setStroke(dp(strokeWidth), strokeColor);
        return shape;
    }

    private void gap(LinearLayout parent, int height) {
        View space = new View(this);
        parent.addView(space, new LinearLayout.LayoutParams(1, dp(height)));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
