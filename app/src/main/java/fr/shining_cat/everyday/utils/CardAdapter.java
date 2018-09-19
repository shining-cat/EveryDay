package fr.shining_cat.everyday.utils;


import android.support.v7.widget.CardView;

public interface CardAdapter {

    int MAX_ELEVATION_FACTOR = 9;

    float getBaseElevation();

    CardView getCardViewAt(int position);

    int getCount();
}
