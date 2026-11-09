package com.example.gym;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gym.db.FitnessRepository;
import com.example.gym.model.User;
import com.example.gym.util.AppDialog;
import com.example.gym.util.Prefs;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private EditText etAccount;
    private EditText etPassword;
    private MaterialButton btnLogin;
    private FitnessRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Prefs.getUserId(this) > 0) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        repository = FitnessRepository.get(this);

        etAccount = findViewById(R.id.etAccount);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvForgot = findViewById(R.id.tvForgot);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> showRegisterDialog());
        tvForgot.setOnClickListener(v -> showForgotAccountDialog());

        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
    }

    private void attemptLogin() {
        String account = etAccount.getText().toString().trim();
        String password = etPassword.getText().toString();
        if (account.isEmpty() || password.isEmpty()) {
            toast(R.string.toast_account_password_empty);
            return;
        }

        btnLogin.setEnabled(false);
        repository.login(account, password, user -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            btnLogin.setEnabled(true);
            if (user == null) {
                toast(R.string.toast_login_failed);
                return;
            }
            Prefs.saveUserId(this, user.id);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void showRegisterDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_register, null);
        EditText regAccount = view.findViewById(R.id.etRegAccount);
        EditText regPassword = view.findViewById(R.id.etRegPassword);
        EditText regQuestion = view.findViewById(R.id.etRegQuestion);
        EditText regAnswer = view.findViewById(R.id.etRegAnswer);
        TextView confirm = view.findViewById(R.id.btnDialogConfirm);

        Dialog dialog = AppDialog.show(this, view);
        view.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            String account = regAccount.getText().toString().trim();
            String password = regPassword.getText().toString();
            String question = regQuestion.getText().toString().trim();
            String answer = regAnswer.getText().toString().trim();

            if (account.isEmpty() || password.isEmpty()
                    || question.isEmpty() || answer.isEmpty()) {
                toast(R.string.toast_fill_all);
                return;
            }
            if (password.length() < 6) {
                toast(R.string.toast_password_length);
                return;
            }

            setConfirmEnabled(confirm, false);
            repository.register(account, password, question, answer, userId -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setConfirmEnabled(confirm, true);
                if (userId != null && userId > 0) {
                    dialog.dismiss();
                    etAccount.setText(account);
                    etPassword.setText("");
                    etPassword.requestFocus();
                    toast(R.string.toast_register_success);
                } else if (userId != null && userId == -1L) {
                    toast(R.string.toast_account_exists);
                } else {
                    toast(R.string.toast_operation_failed);
                }
            });
        });
    }

    private void showForgotAccountDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_account, null);
        EditText input = view.findViewById(R.id.etForgotAccount);
        TextView confirm = view.findViewById(R.id.btnDialogConfirm);

        Dialog dialog = AppDialog.show(this, view);
        view.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            String account = input.getText().toString().trim();
            if (account.isEmpty()) {
                toast(R.string.toast_fill_all);
                return;
            }
            setConfirmEnabled(confirm, false);
            repository.findUserByAccount(account, user -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setConfirmEnabled(confirm, true);
                if (user == null) {
                    toast(R.string.toast_account_not_found);
                    return;
                }
                dialog.dismiss();
                showForgotAnswerDialog(user);
            });
        });
    }

    private void showForgotAnswerDialog(User user) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_answer, null);
        TextView questionView = view.findViewById(R.id.tvSecurityQuestion);
        EditText answerInput = view.findViewById(R.id.etForgotAnswer);
        questionView.setText(user.securityQuestion);
        TextView confirm = view.findViewById(R.id.btnDialogConfirm);

        Dialog dialog = AppDialog.show(this, view);
        view.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            String answer = answerInput.getText().toString().trim();
            if (answer.isEmpty()
                    || !answer.equalsIgnoreCase(user.securityAnswer.trim())) {
                toast(R.string.toast_answer_wrong);
                return;
            }
            dialog.dismiss();
            showForgotNewPasswordDialog(user);
        });
    }

    private void showForgotNewPasswordDialog(User user) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_forgot_new_password, null);
        EditText newPassword = view.findViewById(R.id.etNewPassword);
        EditText confirmPassword = view.findViewById(R.id.etConfirmPassword);
        TextView confirm = view.findViewById(R.id.btnDialogConfirm);

        Dialog dialog = AppDialog.show(this, view);
        view.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            String password = newPassword.getText().toString();
            String confirmation = confirmPassword.getText().toString();
            if (password.length() < 6) {
                toast(R.string.toast_password_length);
                return;
            }
            if (!password.equals(confirmation)) {
                toast(R.string.toast_password_mismatch);
                return;
            }

            setConfirmEnabled(confirm, false);
            repository.updatePassword(user.id, password, success -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setConfirmEnabled(confirm, true);
                if (Boolean.TRUE.equals(success)) {
                    dialog.dismiss();
                    etAccount.setText(user.account);
                    etPassword.setText("");
                    toast(R.string.toast_password_updated);
                } else {
                    toast(R.string.toast_operation_failed);
                }
            });
        });
    }

    private void setConfirmEnabled(TextView confirm, boolean enabled) {
        confirm.setEnabled(enabled);
        confirm.setAlpha(enabled ? 1f : 0.5f);
    }

    private void toast(int messageRes) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
    }
}
