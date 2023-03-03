package com.example.registerandlogin_nain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RegisterUser extends AppCompatActivity {

    boolean checker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        EditText empIdInput = findViewById(R.id.empIdInput);
        EditText userInput = findViewById(R.id.registerUserInput);
        TextView passwordInput = findViewById(R.id.registerPassInput);
        TextView confirmPassInput = findViewById(R.id.confirmPassword);
        Switch checkSwitch = findViewById(R.id.checkSwitch);
        TextView checkUserMsg = findViewById(R.id.checkUserMsg);
        TextView checkPassMsg = findViewById(R.id.checkPassMsg);
        TextView checkConfirmPassMsg = findViewById(R.id.checkConfirmPassMsg);
        TextView checkIdMsg = findViewById(R.id.checkIdMsg);
        Button registerBtn = findViewById(R.id.registerBtn);
        Handler handler = new Handler();

        String status = "active";
        String confirmPassword = confirmPassInput.getText().toString();
        checker = false;

        userInput.setEnabled(false);
        passwordInput.setEnabled(false);
        confirmPassInput.setEnabled(false);
        registerBtn.setEnabled(false);

        checkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkSwitch.getThumbDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                    checkSwitch.getTrackDrawable().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
                    userInput.setEnabled(false);
                    passwordInput.setEnabled(false);
                    confirmPassInput.setEnabled(false);
                    registerBtn.setEnabled(false);

                } else {
                    checkSwitch.getThumbDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                    checkSwitch.getTrackDrawable().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
                    userInput.setEnabled(true);
                    passwordInput.setEnabled(true);
                    confirmPassInput.setEnabled(true);
                    registerBtn.setEnabled(true);
                }
            }
        });

            registerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (empIdInput != null && !TextUtils.isEmpty(empIdInput.getText().toString()) &&
                            userInput != null && !TextUtils.isEmpty(userInput.getText().toString())&&
                            passwordInput != null && !TextUtils.isEmpty(passwordInput.getText().toString())&&
                            confirmPassInput != null && !TextUtils.isEmpty( confirmPassInput.getText().toString())) {
                        String status = "active";
                        String password = passwordInput.getText().toString();
                        String empId = empIdInput.getText().toString();
                        String user = userInput.getText().toString();
                        storeUserData(empId, user, password, status);
                    }
                    else{
                        Toast.makeText(RegisterUser.this, "Failed", Toast.LENGTH_SHORT).show();
                    }

                }
            });

            empIdInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //do nothing
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //do nothing
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String empId = s.toString().trim();
                    EmpIdValidationThread idValidator = new EmpIdValidationThread(empId, status, checkSwitch, checkIdMsg);
                    idValidator.start();

                }
            });

            userInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String userName = s.toString().trim();
                    if (TextUtils.isEmpty(userName)) {
                        checkUserMsg.setVisibility(View.VISIBLE);
                        checkUserMsg.setText("Username cannot be empty!");
                        checkUserMsg.setTextColor(Color.RED);
                    } else {
                        UserNameValidationThread userValidator = new UserNameValidationThread(userName, checkUserMsg, handler);
                        userValidator.start();
                    }
                }

            });

            passwordInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String password = s.toString().trim();

                    if (password.isEmpty()) {
                        checkPassMsg.setVisibility(View.VISIBLE);
                        checkPassMsg.setText("Password cannot be empty");
                        checkPassMsg.setTextColor(Color.RED);
                        return;
                    }
                    passwordValidationThread passwordValidator = new passwordValidationThread(password);
                    passwordValidator.start();
                    try {
                        // Wait for the thread to complete
                        passwordValidator.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    boolean isValid = passwordValidator.isValid();
                            if (isValid) {
                                checkPassMsg.setVisibility(View.VISIBLE);
                                checkPassMsg.setText("Password Strong");
                                checkPassMsg.setTextColor(Color.GREEN);
                            } else {
                                checkPassMsg.setVisibility(View.VISIBLE);
                                checkPassMsg.setText("Password too weak!");
                                checkPassMsg.setTextColor(Color.RED);
                            }

                }
            });


            confirmPassInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String confirmPass = s.toString().trim();
                    String password = passwordInput.getText().toString();

                    if (confirmPass.isEmpty()) {
                        checkConfirmPassMsg.setVisibility(View.VISIBLE);
                        checkConfirmPassMsg.setText("Cannot be empty");
                        checkConfirmPassMsg.setTextColor(Color.RED);
                        return;
                    }

                    confirmPasswordValidationThread confirmPassValidator = new confirmPasswordValidationThread(password, confirmPass);
                    confirmPassValidator.start();


                    try {
                        // Wait for the thread to complete
                        confirmPassValidator.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    boolean isValid = confirmPassValidator.isValid();
                    if (isValid == false) {
                        checkConfirmPassMsg.setVisibility(View.VISIBLE);
                        checkConfirmPassMsg.setText("Passwords do not match");
                        checkConfirmPassMsg.setTextColor(Color.RED);
                    }
                    else{
                        checkConfirmPassMsg.setVisibility(View.INVISIBLE);
                    }

                }
            });


        }
    public void storeUserData(String employeeId, String userName, String password, String status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userData = new HashMap<>();
        userData.put("employeeId", employeeId);
        userData.put("username", userName);
        userData.put("password", password);
        userData.put("status", status);

        db.collection("Users").document()
                .set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RegisterUser.this, "Stored Successfully", Toast.LENGTH_SHORT).show();
                        // data stored successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // error occurred while storing data
                        Toast.makeText(RegisterUser.this, "Error storing data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
class EmpIdValidationThread extends Thread{
    String empId;
    String status;
    Switch checkSwitch;
    TextView checkMsg;

    EmpIdValidationThread(String empId, String status, Switch checkSwitch, TextView checkMsg){
        this.empId = empId;
        this.status = status;
        this.checkSwitch = checkSwitch;
        this.checkMsg = checkMsg;
    }

    @Override
    public void run() {
        checkEmpIdExists(empId, status, checkSwitch, checkMsg);
    }

    public void checkEmpIdExists(String empId, String status, Switch checkSwitch, TextView checkMsg) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Employees")
                .whereEqualTo("employeeId", empId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        boolean empIdExists = !querySnapshot.isEmpty();

                        // Check if employeeId exists in the Users collection
                        db.collection("Users")
                                .whereEqualTo("employeeId", empId)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot querySnapshot) {
                                        boolean userIdExists = !querySnapshot.isEmpty();

                                        // Do something with the results
                                        // Update the switch state regardless of whether the second query is successful or not
//                                        if (!userIdExists) {
//                                            checkSwitch.setChecked(false);
//                                        }

                                        if (empIdExists && userIdExists) {
                                            checkSwitch.setChecked(true);
                                            checkMsg.setVisibility(View.VISIBLE);
                                            checkMsg.setText("Account Already Exists!");
                                            checkMsg.setTextColor(Color.RED);
                                        } else if (empIdExists && !userIdExists) {
                                            checkSwitch.setChecked(false);
                                            checkMsg.setVisibility(View.INVISIBLE);
                                        } else if (userIdExists) {
                                            checkSwitch.setChecked(true);
                                        } else if(!empIdExists){
                                            checkSwitch.setChecked(false);
                                            checkSwitch.setChecked(true);
                                            checkMsg.setVisibility(View.VISIBLE);
                                            checkMsg.setText("Employee does not exist!");
                                            checkMsg.setTextColor(Color.RED);
                                        }

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Update the switch state regardless of whether the query failed or not
                                        checkSwitch.setChecked(false);
                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Update the switch state regardless of whether the query failed or not
                        checkSwitch.setChecked(false);
                    }
                });
    }

}

class UserNameValidationThread extends Thread{
    String userName;
    TextView checkUserMsg;
    Handler handler;

    UserNameValidationThread(String userName, TextView checkUserMsg, Handler handler){
        this.userName = userName;
        this.checkUserMsg = checkUserMsg;
        this.handler = handler;
    }

    @Override
    public void run() {
        checkUserExists(userName, checkUserMsg);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                checkUserMsg.setVisibility(View.VISIBLE);
                checkUserMsg.setText("Checking username...");
            }
        });
    }

    public void checkUserExists(String userName, TextView checkUserMsg){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .whereEqualTo("username", userName) // search for the document that has the specified username
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) { // username is available
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    checkUserMsg.setVisibility(View.VISIBLE);
                                    checkUserMsg.setText("Username available");
                                    checkUserMsg.setTextColor(Color.GREEN);
                                }
                            });
                        } else { // username is taken
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    checkUserMsg.setVisibility(View.VISIBLE);
                                    checkUserMsg.setText("Username already taken!");
                                    checkUserMsg.setTextColor(Color.RED);
                                }
                            });
                        }
                    }
                });
    }



}

class passwordValidationThread extends Thread {
    String password;
    boolean isValid;

    passwordValidationThread(String password){
        this.password = password;
        this.isValid = false;
    }

    @Override
    public void run() {
        if (password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
            isValid = true;
        }
    }
    public boolean isValid() {
        return isValid;
    }
}


class confirmPasswordValidationThread extends Thread{
    String password, confirmedPass;
    boolean isValid;

    confirmPasswordValidationThread(String password, String confirmedPass) {
        this.password = password;
        this.confirmedPass = confirmedPass;
        isValid = false;
    }
        @Override
        public void run() {
            // Check if password and confirm password match
            if (password.equals(confirmedPass)) {
                isValid = true;
            }
        }

        public boolean isValid() {
            return isValid;
        }

}