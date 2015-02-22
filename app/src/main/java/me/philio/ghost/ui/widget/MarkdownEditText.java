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
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import me.philio.ghost.PreferenceConstants;

/**
 * A markdown aware {@link EditText} that formats the text based on markdown syntax
 */
public class MarkdownEditText extends EditText {

    /**
     * Logging tag
     */
    private static final String TAG = MarkdownEditText.class.getName();

    /**
     * Headings
     */
    private static final String H1 = "#";
    private static final String H2 = "##";
    private static final String H3 = "###";
    private static final String H4 = "####";
    private static final String H5 = "#####";
    private static final String H6 = "######";

    /**
     * Heading text size proportions
     */
    private static final float H1_SIZE = 2f;
    private static final float H2_SIZE = 1.6f;
    private static final float H3_SIZE = 1.4f;
    private static final float H4_SIZE = 1.2f;
    private static final float H5_SIZE = 1f;
    private static final float H6_SIZE = 0.8f;

    /**
     * Flag to allow rich text formatting
     */
    private boolean mRichTextEnabled = true;

    public MarkdownEditText(Context context) {
        super(context);
        checkPreferences();
    }

    public MarkdownEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        checkPreferences();
    }

    public MarkdownEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        checkPreferences();
    }

    @TargetApi(21)
    public MarkdownEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        checkPreferences();
    }

    /**
     * Check preferences related to editing
     */
    private void checkPreferences() {
        if (isInEditMode()) {
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int mode = Integer.parseInt(preferences.getString(PreferenceConstants.KEY_EDITING_MODE,
                Integer.toString(PreferenceConstants.EDITING_MODE_DEFAULT)));
        switch (mode) {
            case PreferenceConstants.EDITING_MODE_RICH:
                mRichTextEnabled = true;
                break;
            case PreferenceConstants.EDITING_MODE_PLAIN:
                mRichTextEnabled = false;
                break;
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (mRichTextEnabled) {
            // TODO no need to reformat the whole document, optimise this later
            Log.d(TAG, "Formatting markdown");
            removeSpans(0, getEditableText().length());
            formatAll();
        }
    }

    /**
     * Format all content line by line
     */
    private void formatAll() {
        // Split text into lines and format
        String content = getEditableText().toString();
        int start = 0;
        int end = 0;

        while (end < content.length()) {
            end = content.indexOf('\n', start);
            if (end == -1) {
                end = content.length();
            }
            formatLine(content.substring(start, end), start, end);
            start = end + 1;
        }
    }

    /**
     * Apply formatting to a line of text
     *
     * @param line  Line of text to format
     * @param start Start point
     * @param end   End point
     */
    private void formatLine(String line, int start, int end) {
        formatHeadings(line, start, end);
        formatItalic(line, start, end);
        formatBold(line, start, end);
    }

    /**
     * Format headings
     *
     * @param line  Line of text to format
     * @param start Start point
     * @param end   End point
     */
    private void formatHeadings(String line, int start, int end) {
        if (line.startsWith(H6)) {
            addRelativeSizeSpan(H6_SIZE, start, end);
            addStyleSpan(Typeface.BOLD, start, end);
        } else if (line.startsWith(H5)) {
            addRelativeSizeSpan(H5_SIZE, start, end);
            addStyleSpan(Typeface.BOLD, start, end);
        } else if (line.startsWith(H4)) {
            addRelativeSizeSpan(H4_SIZE, start, end);
            addStyleSpan(Typeface.BOLD, start, end);
        } else if (line.startsWith(H3)) {
            addRelativeSizeSpan(H3_SIZE, start, end);
            addStyleSpan(Typeface.BOLD, start, end);
        } else if (line.startsWith(H2)) {
            addRelativeSizeSpan(H2_SIZE, start, end);
            addStyleSpan(Typeface.BOLD, start, end);
        } else if (line.startsWith(H1)) {
            addRelativeSizeSpan(H1_SIZE, start, end);
            addStyleSpan(Typeface.BOLD, start, end);
        }
    }

    /**
     * Format italic text
     *
     * @param line  Line of text to format
     * @param start Start point
     * @param end   End point
     */
    private void formatItalic(String line, int start, int end) {
        addStyle(Typeface.ITALIC, "*", line, start, end);
        addStyle(Typeface.ITALIC, "_", line, start, end);
    }

    /**
     * Format bold text
     *
     * @param line  Line of text to format
     * @param start Start point
     * @param end   End point
     */
    private void formatBold(String line, int start, int end) {
        addStyle(Typeface.BOLD, "**", line, start, end);
        addStyle(Typeface.BOLD, "__", line, start, end);
    }

    /**
     * Add a style between markers found in line
     *
     * @param style  Text style to apply
     * @param marker
     * @param line   Line of text to format
     * @param start  Start point
     * @param end    End point
     */
    private void addStyle(int style, String marker, String line, int start, int end) {
        if (line.contains(marker)) {
            int position = 0;
            while (position < line.length()) {
                int openPosition = line.indexOf(marker, position);
                if (openPosition == -1) {
                    break;
                }
                int closePosition = line.indexOf(marker, openPosition + 1);
                if (closePosition == -1) {
                    closePosition = line.length();
                }
                if (closePosition > openPosition + 1) {
                    addStyleSpan(style, start + openPosition + marker.length(), start + closePosition);
                }
                position = closePosition + marker.length();
            }
        }
    }

    /**
     * Find and remove all spans between the start and end points
     */
    private void removeSpans(int start, int end) {
        RelativeSizeSpan[] relativeSizeSpans = getEditableText().getSpans(start, end, RelativeSizeSpan.class);
        for (RelativeSizeSpan relativeSizeSpan : relativeSizeSpans) {
            getEditableText().removeSpan(relativeSizeSpan);
        }
        StyleSpan[] styleSpans = getEditableText().getSpans(start, end, StyleSpan.class);
        for (StyleSpan styleSpan : styleSpans) {
            getEditableText().removeSpan(styleSpan);
        }
    }

    /**
     * Add a {@link RelativeSizeSpan} between the start and end points
     *
     * @param proportion Proportion to change the size
     * @param start      Start point
     * @param end        End point
     */
    private void addRelativeSizeSpan(float proportion, int start, int end) {
        getEditableText().setSpan(new RelativeSizeSpan(proportion), start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * Add a {@link StyleSpan} between the start and end points
     *
     * @param style Text style to apply (bold, italic, bold/italic)
     * @param start Start point
     * @param end   End point
     */
    private void addStyleSpan(int style, int start, int end) {
        getEditableText().setSpan(new StyleSpan(style), start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

}
