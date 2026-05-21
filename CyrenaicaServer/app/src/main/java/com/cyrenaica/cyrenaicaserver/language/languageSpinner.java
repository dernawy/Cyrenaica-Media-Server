package com.cyrenaica.cyrenaicaserver.language;



import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.cyrenaica.cyrenaicaserver.R;

public class languageSpinner extends androidx.appcompat.widget.AppCompatTextView {

    public languageSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public languageSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public languageSpinner(Context context) {
        super(context);
        init(null);
    }

    private void init(AttributeSet attrs){
        if(attrs != null){
            try {
                setPadding(5, 5, 5, 5);
                setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_down_24, 0 );
                setGravity((Gravity.CENTER_VERTICAL));

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
