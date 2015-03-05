/*
 * Copyright 2014 Phil Bayfield
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.philio.ghost.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * A {@link ScrollView} that will apply a parallax effect to a {@link ParallaxImageView} child, also
 * makes scroll observable
 */
public class ParallaxScrollView extends ScrollView {

    /**
     * The image view to apply parallax effect to
     */
    private ParallaxImageView mParallaxImageView;

    /**
     * Scroll listener
     */
    private OnScrollListener mScrollListener;

    public ParallaxScrollView(Context context) {
        super(context);
    }

    public ParallaxScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ParallaxScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ParallaxScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Find parallax image within the child view group
        if (getChildCount() > 0 && getChildAt(0) instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) getChildAt(0);
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                if (viewGroup.getChildAt(i) instanceof ParallaxImageView) {
                    mParallaxImageView = (ParallaxImageView) viewGroup.getChildAt(i);
                    break;
                }
            }
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        // If parallax image is visible, adjust y position
        if (mParallaxImageView != null && mParallaxImageView.getVisibility() == VISIBLE) {
            mParallaxImageView.setTranslationY((int) (t / 1.8));
        }

        // Send event to listener
        if (mScrollListener != null) {
            mScrollListener.onScrollChanged(l, t, oldl, oldt);
        }
    }

    /**
     * Set scroll listener
     *
     * @param listener
     */
    public void setOnScrollListener(OnScrollListener listener) {
        mScrollListener = listener;
    }

    /**
     * Listener interface to receive scroll events
     */
    public interface OnScrollListener {

        public void onScrollChanged(int l, int t, int oldl, int oldt);

    }

}
