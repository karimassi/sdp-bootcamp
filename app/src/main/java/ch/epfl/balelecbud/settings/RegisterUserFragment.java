package ch.epfl.balelecbud.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import ch.epfl.balelecbud.R;
import ch.epfl.balelecbud.models.User;
import ch.epfl.balelecbud.util.database.DatabaseWrapper;

import static ch.epfl.balelecbud.BalelecbudApplication.getAppAuthenticator;
import static ch.epfl.balelecbud.BalelecbudApplication.getAppDatabaseWrapper;
import static ch.epfl.balelecbud.util.StringUtils.isEmailValid;

public class RegisterUserFragment extends DialogFragment {
    public static final String TAG = RegisterUserFragment.class.getSimpleName();
    private final SettingsFragment settingsFragment;
    private EditText nameField;
    private EditText emailField;
    private EditText passwordField;
    private EditText repeatPasswordField;
    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(validateEntry());
        }
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_register_user, null);

        nameField = view.findViewById(R.id.editTextNameRegister);
        emailField = view.findViewById(R.id.editTextEmailRegister);
        passwordField = view.findViewById(R.id.editTextPasswordRegister);
        repeatPasswordField = view.findViewById(R.id.editTextRepeatPasswordRegister);

        nameField.addTextChangedListener(watcher);
        emailField.addTextChangedListener(watcher);
        passwordField.addTextChangedListener(watcher);
        repeatPasswordField.addTextChangedListener(watcher);

        builder.setView(view).setTitle(R.string.register)
                .setPositiveButton(R.string.action_register, (dialog, id) ->
                        register(
                                nameField.getText().toString(),
                                emailField.getText().toString(),
                                passwordField.getText().toString()))
                .setNeutralButton(R.string.action_no_account, (dialog, which) -> {
                    dismiss();
                    DialogFragment registerDialog = LoginUserFragment.newInstance(settingsFragment);
                    registerDialog.show(getActivity().getSupportFragmentManager(), LoginUserFragment.TAG);
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel());
        view.post(() -> {
            AlertDialog dialog = ((AlertDialog) getDialog());
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        });
        return builder.create();
    }

    RegisterUserFragment(SettingsFragment settingsFragment) {
        this.settingsFragment = settingsFragment;
    }

    private void register(String name, String email, String password) {
        if (!validateEntry())
            return;

        getAppAuthenticator().createAccount(name, email, password).whenComplete((aVoid, throwable) -> {
            if (throwable != null) {
                Toast.makeText(
                        getContext(),
                        throwable.getCause().getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            } else {
                onAuthComplete();
            }
        });

    }

    public static RegisterUserFragment newInstance(SettingsFragment settingsFragment) {
        return new RegisterUserFragment(settingsFragment);
    }

    private boolean validateEntry() {
        boolean valid = true;
        if (!isNameValid())
            valid = false;

        if (!isEmailValid(getContext(), emailField))
            valid = false;

        if (!isPasswordsValid())
            valid = false;

        return valid;
    }

    private boolean isPasswordsValid() {
        boolean valid = true;
        String password = passwordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordField.setError(getString(R.string.require_password));
            valid = false;
        } else if (password.length() < 6) {
            passwordField.setError(getString(R.string.invalid_password));
            valid = false;
        }

        String passwordRepeated = repeatPasswordField.getText().toString();
        if (TextUtils.isEmpty(passwordRepeated)) {
            repeatPasswordField.setError(getString(R.string.require_password_repeated));
            valid = false;
        }

        if (!passwordRepeated.equals(password)) {
            passwordField.setError(getString(R.string.mismatch_password));
            repeatPasswordField.setError(getString(R.string.mismatch_password));
            valid = false;
        }
        return valid;
    }

    private boolean isNameValid() {
        String name = nameField.getText().toString();
        if (TextUtils.isEmpty(name)) {
            nameField.setError(getString(R.string.require_name));
           return false;
        }
        return true;
    }

    private void onAuthComplete() {
        getAppDatabaseWrapper()
                .getCustomDocument(DatabaseWrapper.USERS_PATH, getAppAuthenticator().getCurrentUid(), User.class)
                .whenComplete((user, throwable) -> {
                    if (throwable != null) {
                        Toast.makeText(getContext(),
                                throwable.getCause().getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        getAppAuthenticator().setCurrentUser(user);
                        settingsFragment.updateLoginStatus(true);
                    }
                });

    }
}
