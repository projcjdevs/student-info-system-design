package com.cabatian.studentinfosystemdesign;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

/** Small helpers shared by both screens for reading text boxes and showing errors. */
final class FormUtils {

    private FormUtils() {
    }

    /** Returns the trimmed text typed inside a TextInputLayout ("" if empty). */
    static String textOf(TextInputLayout layout) {
        EditText editText = layout.getEditText();
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    /**
     * Shows a red error under the box, or removes it when message is null.
     * Removing also gives back the space the error used, so the spacing matches the design.
     */
    static void setError(TextInputLayout layout, String message) {
        if (message == null) {
            layout.setError(null);
            layout.setErrorEnabled(false);
        } else {
            layout.setError(message);
        }
    }

    /** Hides the red error message as soon as the user starts typing again. */
    static void clearErrorWhenTyping(TextInputLayout layout, Runnable onChanged) {
        EditText editText = layout.getEditText();
        if (editText == null) {
            return;
        }
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout.getError() != null) {
                    setError(layout, null);
                }
                if (onChanged != null) {
                    onChanged.run();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}
