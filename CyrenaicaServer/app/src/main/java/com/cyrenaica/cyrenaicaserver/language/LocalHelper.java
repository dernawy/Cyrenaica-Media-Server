package com.cyrenaica.cyrenaicaserver.language;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.cyrenaica.cyrenaicaserver.MainActivity;
import com.cyrenaica.cyrenaicaserver.R;

import java.util.Locale;

public class LocalHelper {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";
    public static final String[] language       = { "العربية", "English", "Française" };

    public static String GENERAL_LANGUAGE_CODE = "";

    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        return setLocale(context, lang);
    }

    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static ArrayAdapter<String> langSpinnerAdapter(Context context){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.language_spinner_custom_field, LocalHelper.language);
        adapter.setDropDownViewResource(R.layout.language_spinner_custom_dropdown_item);
        return adapter;
    }

    public static Context setLocale(Context context, String language) {
        persist(context, language);

        return updateResources(context, language);

    }

    public static void init_language(Context context, Activity current_activity, Spinner langSpinner, TextView label, boolean view_label){

        String act_lang = getLanguage(context);

        ArrayAdapter<String> language_adapter = langSpinnerAdapter(context);
        langSpinner.setAdapter(language_adapter);

        if (LocalHelper.getLanguage(context).equalsIgnoreCase("ar")) {

            langSpinner.setSelection(language_adapter.getPosition("العربية"));
        }
        else if (LocalHelper.getLanguage(context).equalsIgnoreCase("en")) {
            langSpinner.setSelection(language_adapter.getPosition("English"));
        }
        else
        {
            langSpinner.setSelection(language_adapter.getPosition("Française"));
        }

        langSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                switch (i) {

                    case 0:
                        GENERAL_LANGUAGE_CODE = "ar";

                        LocalHelper.setLocale(context, GENERAL_LANGUAGE_CODE);

                        if(view_label){

                            label.setVisibility(View.VISIBLE);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                label.setTypeface(context.getResources().getFont(R.font.changa));
                            }

                            label.setText(R.string.word_select_Language);
                        }
                        else
                        {
                            label.setVisibility(View.GONE);
                        }

                        break;

                    case 1:
                        GENERAL_LANGUAGE_CODE = "en";

                        LocalHelper.setLocale(context, GENERAL_LANGUAGE_CODE);

                        if(view_label){
                            label.setVisibility(View.VISIBLE);
                            label.setText(R.string.word_select_Language);
                        }
                        else
                        {
                            label.setVisibility(View.GONE);
                        }

                        break;

                    case 2:
                        GENERAL_LANGUAGE_CODE = "fr";

                        LocalHelper.setLocale(context, GENERAL_LANGUAGE_CODE);

                        if(view_label){
                            label.setVisibility(View.VISIBLE);
                            label.setText(R.string.word_select_Language);
                        }
                        else
                        {
                            label.setVisibility(View.GONE);
                        }

                        break;
                }

                if (!LocalHelper.GENERAL_LANGUAGE_CODE.equalsIgnoreCase(act_lang)) {
                    Intent intent = new Intent(context, MainActivity.class);
                    current_activity.startActivity(intent);
                    current_activity.overridePendingTransition(0, 0);
                    current_activity.finish();
                    current_activity.overridePendingTransition(0, 0);
                }

                System.out.println("LANGUAGE: " + act_lang);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        configuration.setLayoutDirection(locale);

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }
}
