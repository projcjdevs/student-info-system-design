package com.cabatian.studentinfosystemdesign;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

public class StudentInfoActivity extends AppCompatActivity {

    /** Username passed from the Log In screen. */
    public static final String EXTRA_USERNAME = "extra_username";

    private View root;
    private TextInputLayout tilStudentNumber;
    private TextInputLayout tilLastName;
    private TextInputLayout tilFirstName;
    private TextInputLayout tilMiddleName;
    private TextInputLayout tilProgram;
    private TextInputLayout tilSpecialization;
    private ChipGroup cgYearLevel;

    /** True right after Save, false again as soon as something is changed. */
    private boolean saved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_student_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        root = findViewById(R.id.main);
        tilStudentNumber = findViewById(R.id.til_student_number);
        tilLastName = findViewById(R.id.til_last_name);
        tilFirstName = findViewById(R.id.til_first_name);
        tilMiddleName = findViewById(R.id.til_middle_name);
        tilProgram = findViewById(R.id.til_program);
        tilSpecialization = findViewById(R.id.til_specialization);
        cgYearLevel = findViewById(R.id.cg_year_level);
        MaterialButton btnClear = findViewById(R.id.btn_clear);
        MaterialButton btnSave = findViewById(R.id.btn_save);

        setUpToolbar();

        // Clear and Save buttons
        btnClear.setOnClickListener(v -> confirmClear());
        btnSave.setOnClickListener(v -> attemptSave());

        // Any change removes the field's error and marks the form as "not saved"
        Runnable markChanged = () -> saved = false;
        TextInputLayout[] fields = {tilStudentNumber, tilLastName, tilFirstName,
                tilMiddleName, tilProgram, tilSpecialization};
        for (TextInputLayout field : fields) {
            FormUtils.clearErrorWhenTyping(field, markChanged);
        }
        cgYearLevel.setOnCheckedStateChangeListener((group, checkedIds) -> saved = false);

        // Phone back button / back gesture: same as the toolbar back arrow
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmLeave();
            }
        });
    }

    // ---------------------------------------------------------------- toolbar

    private void setUpToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        // Back arrow: leave the screen (asks first if there are unsaved details)
        toolbar.setNavigationOnClickListener(v -> confirmLeave());

        // "Logged in as ..." under the title
        String username = getIntent().getStringExtra(EXTRA_USERNAME);
        if (username != null && !username.isEmpty()) {
            toolbar.setSubtitle(getString(R.string.msg_logged_in_as, username));
            toolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.sis_white_80));
        }

        // Three-dot menu: About and Log Out
        toolbar.inflateMenu(R.menu.menu_student_info);
        Drawable overflow = toolbar.getOverflowIcon();
        if (overflow != null) {
            Drawable white = DrawableCompat.wrap(overflow.mutate());
            DrawableCompat.setTint(white, ContextCompat.getColor(this, R.color.sis_white));
            toolbar.setOverflowIcon(white);
        }
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_about) {
                showAbout();
                return true;
            } else if (item.getItemId() == R.id.action_logout) {
                confirmLogout();
                return true;
            }
            return false;
        });
    }

    private void showAbout() {
        new MaterialAlertDialogBuilder(root.getContext())
                .setTitle(R.string.dlg_about_title)
                .setMessage(R.string.dlg_about_message)
                .setPositiveButton(R.string.action_ok, null)
                .show();
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(root.getContext())
                .setTitle(R.string.dlg_logout_title)
                .setMessage(R.string.dlg_logout_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_logout, (dialog, which) -> {
                    Toast.makeText(this, R.string.msg_logged_out, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    private void confirmLeave() {
        if (!hasInput() || saved) {
            finish();
            return;
        }
        new MaterialAlertDialogBuilder(root.getContext())
                .setTitle(R.string.dlg_discard_title)
                .setMessage(R.string.dlg_discard_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_discard, (dialog, which) -> finish())
                .show();
    }

    // ---------------------------------------------------------------- save

    private void attemptSave() {
        String studentNumber = FormUtils.textOf(tilStudentNumber);
        String lastName = FormUtils.textOf(tilLastName);
        String firstName = FormUtils.textOf(tilFirstName);
        String middleName = FormUtils.textOf(tilMiddleName);
        String program = FormUtils.textOf(tilProgram);
        String specialization = FormUtils.textOf(tilSpecialization);

        // Check the required fields, top to bottom
        TextInputLayout firstProblem = null;

        if (studentNumber.isEmpty()) {
            tilStudentNumber.setError(getString(R.string.msg_field_required));
            firstProblem = tilStudentNumber;
        } else if (!studentNumber.matches("\\d{4}-\\d{5}")) {
            tilStudentNumber.setError(getString(R.string.msg_student_number_format));
            firstProblem = tilStudentNumber;
        }
        if (lastName.isEmpty()) {
            tilLastName.setError(getString(R.string.msg_field_required));
            if (firstProblem == null) firstProblem = tilLastName;
        }
        if (firstName.isEmpty()) {
            tilFirstName.setError(getString(R.string.msg_field_required));
            if (firstProblem == null) firstProblem = tilFirstName;
        }
        if (program.isEmpty()) {
            tilProgram.setError(getString(R.string.msg_program_required));
            if (firstProblem == null) firstProblem = tilProgram;
        }

        if (firstProblem != null) {
            firstProblem.requestFocus();
            Snackbar.make(root, R.string.msg_check_fields, Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Everything is filled in: show a summary and ask to confirm
        String na = getString(R.string.not_applicable);
        String fullName = middleName.isEmpty()
                ? lastName + ", " + firstName
                : lastName + ", " + firstName + " " + middleName;
        String summary = getString(R.string.dlg_save_message,
                studentNumber,
                fullName,
                program,
                specialization.isEmpty() ? na : specialization,
                selectedYearLevel());

        new MaterialAlertDialogBuilder(root.getContext())
                .setTitle(R.string.dlg_save_title)
                .setMessage(summary)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    saved = true;
                    Snackbar.make(root, getString(R.string.msg_saved, firstName + " " + lastName),
                                    Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_new_record, v -> clearForm())
                            .setActionTextColor(ContextCompat.getColor(this, R.color.sis_accent))
                            .show();
                })
                .show();
    }

    private String selectedYearLevel() {
        Chip chip = findViewById(cgYearLevel.getCheckedChipId());
        return chip != null ? chip.getText().toString() : getString(R.string.not_applicable);
    }

    // ---------------------------------------------------------------- clear

    private void confirmClear() {
        if (!hasInput()) {
            Toast.makeText(this, R.string.msg_nothing_to_clear, Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(root.getContext())
                .setTitle(R.string.dlg_clear_title)
                .setMessage(R.string.dlg_clear_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_clear, (dialog, which) -> {
                    clearForm();
                    Toast.makeText(this, R.string.msg_cleared, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void clearForm() {
        TextInputLayout[] textFields = {tilStudentNumber, tilLastName, tilFirstName, tilMiddleName};
        for (TextInputLayout field : textFields) {
            EditText editText = field.getEditText();
            if (editText != null) {
                editText.setText("");
            }
        }
        ((MaterialAutoCompleteTextView) findViewById(R.id.act_program)).setText("", false);
        ((MaterialAutoCompleteTextView) findViewById(R.id.act_specialization)).setText("", false);
        cgYearLevel.check(R.id.chip_year_1);

        TextInputLayout[] allFields = {tilStudentNumber, tilLastName, tilFirstName,
                tilMiddleName, tilProgram, tilSpecialization};
        for (TextInputLayout field : allFields) {
            FormUtils.setError(field, null);
        }
        root.clearFocus();
        saved = false;
    }

    /** True if anything was typed/chosen (Year Level counts only if not the default 1st Year). */
    private boolean hasInput() {
        TextInputLayout[] fields = {tilStudentNumber, tilLastName, tilFirstName,
                tilMiddleName, tilProgram, tilSpecialization};
        for (TextInputLayout field : fields) {
            if (!FormUtils.textOf(field).isEmpty()) {
                return true;
            }
        }
        return cgYearLevel.getCheckedChipId() != R.id.chip_year_1;
    }
}
