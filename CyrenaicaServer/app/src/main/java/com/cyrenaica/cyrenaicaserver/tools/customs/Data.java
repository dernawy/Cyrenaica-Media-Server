package com.cyrenaica.cyrenaicaserver.tools.customs;

import static com.arthenica.ffmpegkit.Packages.getPackageName;

import android.util.Log;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Data extends AppCompatActivity {

    String NAME;

    public List<FfmpegColors> getColorList() {

        List<FfmpegColors> colorList = new ArrayList<>();

        Field[] fields = null;

        try {
            fields = Class.forName(getPackageName()+".R$color").getDeclaredFields();
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        for(Field field : fields) {

            String colorName = field.getName();

            if(colorName.startsWith("ffmpeg")){
                int colorId = 0;
                try {
                    colorId = field.getInt(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                int color = getResources().getColor(colorId);
                Log.i("test", colorName + " => " + colorId + " => " + color);
            }


        }

        return colorList;
    }

}
