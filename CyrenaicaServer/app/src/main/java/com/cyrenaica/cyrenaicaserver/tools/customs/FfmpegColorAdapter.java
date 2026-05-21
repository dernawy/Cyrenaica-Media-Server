package com.cyrenaica.cyrenaicaserver.tools.customs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cyrenaica.cyrenaicaserver.R;

import java.util.List;

public class FfmpegColorAdapter extends BaseAdapter {
    private Context context;
    private List<FfmpegColors> colors_List;

    public FfmpegColorAdapter(Context context, List<FfmpegColors> List) {
        this.context = context;
        this.colors_List = List;
    }

    @Override
    public int getCount() {
        return colors_List != null ? colors_List.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        @SuppressLint("ViewHolder") View rootView = LayoutInflater.from(context).inflate(R.layout.ffmpeg_color_spinner_item, viewGroup, false);


        TextView name = rootView.findViewById(R.id.name);
        TextView color = rootView.findViewById(R.id.color);
        TextView code = rootView.findViewById(R.id.code);

        name.setText(colors_List.get(i).getName());
        color.setBackgroundColor(colors_List.get(i).getColor());
        code.setText(colors_List.get(i).getCode());

        return rootView;
    }
}
