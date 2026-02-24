package com.autoparts.catalog;

import android.os.Bundle;
import android.widget.RadioGroup;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends BaseActivity {

    private RadioGroup radioGroupTheme;
    private RadioGroup radioGroupLang;
    private ThemeHelper themeHelper;
    private LocaleHelper localeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        themeHelper = new ThemeHelper(this);
        localeHelper = new LocaleHelper(this);
        radioGroupTheme = findViewById(R.id.radio_group_theme);
        radioGroupLang = findViewById(R.id.radio_group_lang);

        // Set listeners first, then initial selection (listener will fire but value unchanged = no recreate)
        radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int theme = themeFromId(checkedId);
            if (theme != themeHelper.getTheme()) {
                themeHelper.setTheme(theme);
                recreate();
            }
        });

        radioGroupLang.setOnCheckedChangeListener((group, checkedId) -> {
            String lang = langFromId(checkedId);
            if (!lang.equals(localeHelper.getLanguage())) {
                localeHelper.setLanguage(lang);
                recreate();
            }
        });

        // Set initial selection
        radioGroupTheme.check(idFromTheme(themeHelper.getTheme()));
        radioGroupLang.check(idFromLang(localeHelper.getLanguage()));
    }

    private int themeFromId(int id) {
        if (id == R.id.radio_light) return ThemeHelper.THEME_LIGHT;
        if (id == R.id.radio_dark) return ThemeHelper.THEME_DARK;
        return ThemeHelper.THEME_SYSTEM;
    }

    private int idFromTheme(int theme) {
        if (theme == ThemeHelper.THEME_LIGHT) return R.id.radio_light;
        if (theme == ThemeHelper.THEME_DARK) return R.id.radio_dark;
        return R.id.radio_system;
    }

    private String langFromId(int id) {
        if (id == R.id.radio_lang_en) return LocaleHelper.LANG_EN;
        if (id == R.id.radio_lang_ru) return LocaleHelper.LANG_RU;
        return LocaleHelper.LANG_SYSTEM;
    }

    private int idFromLang(String lang) {
        if (LocaleHelper.LANG_EN.equals(lang)) return R.id.radio_lang_en;
        if (LocaleHelper.LANG_RU.equals(lang)) return R.id.radio_lang_ru;
        return R.id.radio_lang_system;
    }
}
