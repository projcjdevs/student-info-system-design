package com.cabatian.studentinfosystemdesign;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private View root;
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private MaterialCheckBox cbRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        root = findViewById(R.id.main);
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        cbRemember = findViewById(R.id.cb_remember);
        MaterialButton btnLogin = findViewById(R.id.btn_login);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);

        // Log In button: check the fields, then open the Student Information screen
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Pressing "Done" on the keyboard while in the password box also logs in
        if (tilPassword.getEditText() != null) {
            tilPassword.getEditText().setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            });
        }

        // Forgot password?: show a simple message dialog
        tvForgotPassword.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(root.getContext())
                        .setTitle(R.string.dlg_forgot_title)
                        .setMessage(R.string.dlg_forgot_message)
                        .setPositiveButton(R.string.action_ok, null)
                        .show());

        // Remember me: a short confirmation when it is ticked
        cbRemember.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, R.string.msg_remember_on, Toast.LENGTH_SHORT).show();
            }
        });

        // Typing again removes the red error under a box
        FormUtils.clearErrorWhenTyping(tilUsername, null);
        FormUtils.clearErrorWhenTyping(tilPassword, null);
    }

    private void attemptLogin() {
        String username = FormUtils.textOf(tilUsername);
        String password = FormUtils.textOf(tilPassword);

        FormUtils.setError(tilUsername, username.isEmpty() ? getString(R.string.msg_username_required) : null);
        FormUtils.setError(tilPassword, password.isEmpty() ? getString(R.string.msg_password_required) : null);
        if (username.isEmpty()) {
            tilUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            tilPassword.requestFocus();
            return;
        }

        Toast.makeText(this, getString(R.string.msg_welcome_user, username), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, StudentInfoActivity.class);
        intent.putExtra(StudentInfoActivity.EXTRA_USERNAME, username);
        startActivity(intent);

        // The password is never kept. The username is kept only if "Remember me" is ticked.
        if (tilPassword.getEditText() != null) {
            tilPassword.getEditText().setText("");
        }
        if (!cbRemember.isChecked() && tilUsername.getEditText() != null) {
            tilUsername.getEditText().setText("");
        }
        FormUtils.setError(tilUsername, null);
        FormUtils.setError(tilPassword, null);
        root.clearFocus();
    }
}
