package uk.co.jmrtra.tripchat.typeface;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextRobotoMedium extends TextView {

    public TextRobotoMedium(Context context) {
        super(context);
    }

    public TextRobotoMedium(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextRobotoMedium(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                    "fonts/Roboto-Medium.ttf");
            setTypeface(tf);
        }
    }
}