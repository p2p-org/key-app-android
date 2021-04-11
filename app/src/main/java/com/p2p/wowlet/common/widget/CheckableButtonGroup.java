package com.p2p.wowlet.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.p2p.wowlet.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


// fixme: attrs
public class CheckableButtonGroup extends LinearLayoutCompat {

    private CheckableButton[] children;
    private boolean isInitialized;
    private OnInitialize onInitialize;
    private final ArrayList<OnInitialize> onInitializes;
    private Map<Integer, CheckableButtonAttrs> specialPositionAttrs;
    private int checkedPosition = 1;

    public CheckableButtonGroup(@NonNull Context context) {
        super(context);
        isInitialized = false;
        specialPositionAttrs = new HashMap<>();
        onInitializes = new ArrayList<>();
    }

    public CheckableButtonGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        isInitialized = false;
        specialPositionAttrs = new HashMap<>();
        onInitializes = new ArrayList<>();
    }

    public CheckableButtonGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        isInitialized = false;
        specialPositionAttrs = new HashMap<>();
        onInitializes = new ArrayList<>();
    }

    public static HashMap<Integer, CheckableButtonAttrs> slippageAttrs() {
        HashMap<Integer, CheckableButtonAttrs> attrs = new HashMap<>();
        int checkedBg = R.drawable.bg_slippage_custom_checkable_button_state_checked;
        int textColorChecked = R.color.cornflowerblue;
        int uncheckedBg = R.drawable.bg_slippage_custom_checkable_button_state_un_checked;
        int textColorUnchecked = R.color.gray_blue_400;
        attrs.put(5, new CheckableButtonAttrs(checkedBg, textColorChecked, uncheckedBg, textColorUnchecked));
        return attrs;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        init();
    }

    private void init() {
        children = new CheckableButton[getChildCount()];
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof CheckableButton) {
                CheckableButton childButton = (CheckableButton) child;
                childButton.position = i + 1;
                children[i] = childButton;
            }else {
                throw new IllegalArgumentException("CheckableButtonGroup can contain only CheckableButtons");
            }
        }
        for (CheckableButton child : children) {
            if (!child.getClickEvents().isEmpty()) continue;
            child.addClickEvent(checkClickEvent(child));
        }
        setCheckedItemPosition(checkedPosition);
        if (onInitialize != null) {
            onInitialize.init();
        }
        for (OnInitialize init : onInitializes) {
            if (init != null && !isInitialized) {
                init.init();
            }
        }
        isInitialized = true;
    }

    private OnClickEvent checkClickEvent(CheckableButton child) {
        return v -> {
            for (CheckableButton childButton : children) {
                boolean setChecked = childButton == child;
                childButton.setChecked(setChecked);
                if (setChecked) {
                    checkedPosition = child.position;
                }
            }
        };
    }


    public void setCheckedItemPosition(int _position) {
        checkedPosition = _position;
        int position = _position - 1;
        checkPositionValidity(position);
        for (int i = 0; i < children.length; i++) {
            children[i].setChecked(i == position);
        }
    }

    private void checkPositionValidity(int position) {
        if (position < 0) throw new IllegalStateException("Position must be bigger than 0");
        if (position > children.length) {
            throw new IllegalArgumentException("Invalid CheckableButton position, please make sure that selected position is not bigger than the actual amount of CheckableButtons in CheckableButtonGroup");
        }
    }


    public void setButtonStates(
            @DrawableRes int drawableChecked,
            @ColorRes int textColorChecked,
            @DrawableRes int drawableUnchecked,
            @ColorRes int textColorUnchecked
    ) {
        if (!isInitialized) {
            onInitialize = () -> setButtonStatesReal(drawableChecked, textColorChecked, drawableUnchecked, textColorUnchecked);
        }
    }

    private void setButtonStatesReal (
            @DrawableRes int drawableChecked,
            @ColorRes int textColorChecked,
            @DrawableRes int drawableUnchecked,
            @ColorRes int textColorUnchecked)
    {
        for (CheckableButton child : children) {
            child.setBackgroundDrawableResStateChecked(drawableChecked);
            child.setTextColorResStateChecked(textColorChecked);
            child.setBackgroundDrawableResStateUnChecked(drawableUnchecked);
            child.setTextColorResStateUnChecked(textColorUnchecked);
        }
        for (int _position : specialPositionAttrs.keySet()) {
            int position = _position - 1;
            checkPositionValidity(position);
            CheckableButtonAttrs attrs = specialPositionAttrs.get(_position);
            if (attrs == null) return;
            children[position].setTextColorResStateChecked(attrs.getTextColorSelected());
            children[position].setBackgroundDrawableResStateChecked(attrs.getBackgroundSelected());
            children[position].setTextColorResStateUnChecked(attrs.getTextColorUnselected());
            children[position].setBackgroundDrawableResStateUnChecked(attrs.getBackgroundUnselected());
        }
    }

    public void setAttributesForSpecialPositions(Map<Integer, CheckableButtonAttrs> attrs) {
        specialPositionAttrs = attrs;
    }

    public void addClickEvents(OnClickEvent clickEvent) {
        for (CheckableButton child : children) {
            child.addClickEvent(clickEvent);
        }
    }

    public void addClickEvents(OnClickEvent clickEvent, Map<Integer, OnClickEvent> specialClickEvents) {
        OnInitialize onAddItemClicks = () -> addClickEventsReal(clickEvent, specialClickEvents);
        onInitializes.add(onAddItemClicks);
    }

    private void addClickEventsReal(OnClickEvent clickEvent, Map<Integer, OnClickEvent> specialClickEvents) {
        for (CheckableButton child: children) {
            child.addClickEvent(clickEvent);
        }
        for (int _position: specialClickEvents.keySet()) {
            int position = _position - 1;
            checkPositionValidity(position);
            OnClickEvent listener = specialClickEvents.get(_position);
            children[position].removeClickListener(clickEvent);
            children[position].addClickEvent(listener);
        }
    }

    public void addFocusListenerForPosition(int position, OnFocusChangeListener onFocusChangeListener) {
        OnInitialize onInitialize = () -> addFocusListenerForPositionReal(position, onFocusChangeListener);
        onInitializes.add(onInitialize);
    }

    private void addFocusListenerForPositionReal(int _position, OnFocusChangeListener onFocusChangeListener) {
        int position = _position - 1;
        checkPositionValidity(position);
        children[position].setOnFocusChangeListener(onFocusChangeListener);
    }

    public void addClickEventForPosition(int position, OnClickEvent clickEvent) {
        OnInitialize onInitialize = () -> addClickEventForPositionReal(position, clickEvent);
        onInitializes.add(onInitialize);
    }

    private void addClickEventForPositionReal(int _position, OnClickEvent clickEvent) {
        int position = _position - 1;
        checkPositionValidity(position);
        children[position].addClickEvent(clickEvent);
    }

    public CheckableButton[] getChildren() {
        return children;
    }

    private interface OnInitialize {
        void init();
    }

    public interface OnClickEvent {
        void onClick(CheckableButton view);
    }

}

