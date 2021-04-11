package com.p2p.wowlet.common.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class CheckableButton extends AppCompatButton {

    private ArrayList<CheckableButtonGroup.OnClickEvent> clickEvents;
    private boolean isChecked;
    private int backgroundDrawableResStateChecked = -1;
    private int backgroundDrawableResStateUnChecked = -1;
    private int textColorResStateChecked = -1;
    private int textColorResStateUnChecked = -1;

    public int position;

    public CheckableButton(@NonNull Context context) {
        super(context);
        init();
    }

    public CheckableButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CheckableButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        isChecked = false;
        clickEvents = new ArrayList<>();
        this.setOnClickListener(v -> {
            if (isChecked()) return;
            for (CheckableButtonGroup.OnClickEvent clickEvent : clickEvents) {
                clickEvent.onClick(this);
            }
        });
    }

    public void addClickEvent(CheckableButtonGroup.OnClickEvent onClickListener) {
        clickEvents.add(onClickListener);
    }

    ArrayList<CheckableButtonGroup.OnClickEvent> getClickEvents() {
        return clickEvents;
    }

    public void removeClickListener(CheckableButtonGroup.OnClickEvent onClickListener) {
        clickEvents.remove(onClickListener);
    }


    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        if (isChecked) {
            setStateChecked();
        }
        else {
            setStateUnChecked();
        }
    }

    public boolean isChecked() {
        return isChecked;
    }

    private void setStateChecked() {
        if (backgroundDrawableResStateChecked == -1
                || textColorResStateChecked == -1) {
            return;
        }
        this.setBackgroundResource(backgroundDrawableResStateChecked);
        this.setTextColor(ContextCompat.getColor(getContext(), textColorResStateChecked));
    }

    private void setStateUnChecked() {
        if (backgroundDrawableResStateUnChecked == -1
                || textColorResStateUnChecked == -1) {
            return;
        }
        this.setBackgroundResource(backgroundDrawableResStateUnChecked);
        this.setTextColor(ContextCompat.getColor(getContext(), textColorResStateUnChecked));
    }

    public void setBackgroundDrawableResStateChecked(@DrawableRes int drawableRes) {
        this.backgroundDrawableResStateChecked = drawableRes;
    }

    public void setBackgroundDrawableResStateUnChecked(@DrawableRes int drawableRes) {
        this.backgroundDrawableResStateUnChecked = drawableRes;
    }

    public void setTextColorResStateChecked(@ColorRes int colorRes) {
        this.textColorResStateChecked = colorRes;
    }

    public void setTextColorResStateUnChecked(@ColorRes int colorRes) {
        this.textColorResStateUnChecked = colorRes;
    }




}
