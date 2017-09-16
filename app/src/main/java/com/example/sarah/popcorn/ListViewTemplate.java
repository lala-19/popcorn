package com.example.sarah.popcorn;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by Sarah on 9/21/2016.
 */
public class ListViewTemplate extends ListView {
    public ListViewTemplate(Context context){super(context);}
    public ListViewTemplate(Context context,AttributeSet attrs){ super(context,attrs);}
    public ListViewTemplate(Context context,AttributeSet attrs,int defStyle){super(context,attrs,defStyle);}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE>>2,MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}
