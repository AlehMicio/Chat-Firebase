package android.kakoytochathz.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.kakoytochathz.R;
import android.kakoytochathz.user.User;
import android.kakoytochathz.user.UserListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignIn extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextView loginTextView, textView1, textView2;
    private EditText emailEditText, passwordEditText, repeatPasswordEditText, nameEditText;
    private Button signInButton;
    private boolean vhodActiv;

    FirebaseDatabase database;
    DatabaseReference usersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(android.kakoytochathz.R.layout.activity_sign_in);

        Initialization();

        //Проверка: если зарегистрированный пользователь не вышел из чата, то при следующем запуске приложения сразу попадает в чат
        if(auth.getCurrentUser()!=null) startActivity(new Intent(SignIn.this, UserListActivity.class));

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vhodUser(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim());
            }
        });

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(vhodActiv){
                    vhodActiv = false;
                    signInButton.setText("Войти");
                    loginTextView.setText("Зарегистрироваться");
                    textView1.setText("Вход");
                    textView2.setVisibility(View.GONE);
                    repeatPasswordEditText.setVisibility(View.GONE);
                    nameEditText.setVisibility(View.GONE);
                }
                else {
                    vhodActiv = true;
                    signInButton.setText("Зарегистрироваться");
                    loginTextView.setText("Войти");
                    textView1.setText("Регистрация");
                    textView2.setVisibility(View.VISIBLE);
                    repeatPasswordEditText.setVisibility(View.VISIBLE);
                    nameEditText.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void Initialization(){
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersDatabaseReference = database.getReference().child("users");

        emailEditText = findViewById(android.kakoytochathz.R.id.emailEditText);
        passwordEditText = findViewById(android.kakoytochathz.R.id.passwordEditText);
        repeatPasswordEditText = findViewById(android.kakoytochathz.R.id.repeatPasswordEditText);
        nameEditText = findViewById(android.kakoytochathz.R.id.nameEditText);
        loginTextView = findViewById(android.kakoytochathz.R.id.loginTextView);
        textView1 = findViewById(android.kakoytochathz.R.id.textView1);
        textView2 = findViewById(android.kakoytochathz.R.id.textView2);
        signInButton = findViewById(R.id.signInButton);
        vhodActiv = false;
    }

    private void vhodUser(String email, String password){

        //Проверка входа или регистрации
        if(!vhodActiv){
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                try {
                                    throw task.getException();
                                }
                                catch (FirebaseAuthInvalidUserException e) {
                                    Toast.makeText(SignIn.this, "Неверный email", Toast.LENGTH_SHORT).show();
                                }
                                catch (FirebaseAuthInvalidCredentialsException e) {
                                    Toast.makeText(SignIn.this, "Неверный пароль", Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e) {
                                    Toast.makeText(SignIn.this, "Ошибка входа", Toast.LENGTH_SHORT).show();
                                    Log.d("vhod", "" + e.getMessage());
                                }
                            }
                            else {
                                //Вход удачно
                                Toast.makeText(SignIn.this, "Успешный вход", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = auth.getCurrentUser();
                                //Передача имени для чата и переход из активити регистрации в активити выбора пользователя
                                Intent intent = new Intent(SignIn.this, UserListActivity.class);
                                intent.putExtra("userName", nameEditText.getText().toString().trim());
                                startActivity(intent);
                            }
                        }
                    });
        }
        else {
            if (!passwordEditText.getText().toString().trim().
             equals(repeatPasswordEditText.getText().toString().trim()))Toast.makeText(SignIn.this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            else{
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    try {
                                        throw task.getException();
                                    }
                                    catch (FirebaseAuthUserCollisionException e) {
                                        Toast.makeText(SignIn.this, "Данный email уже существует", Toast.LENGTH_SHORT).show();
                                    }
                                    catch (FirebaseAuthWeakPasswordException e) {
                                        Toast.makeText(SignIn.this, "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show();
                                    }
                                    catch (Exception e) {
                                        Toast.makeText(SignIn.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                                        Log.d("reg", "" + e.getMessage());
                                    }
                                }
                                else {
                                    // Регистрация прошла успешна
                                    Toast.makeText(SignIn.this, "Регистрация прошла успешна", Toast.LENGTH_SHORT).show();
                                    FirebaseUser user = auth.getCurrentUser();
                                    createUser(user);
                                    // updateUI(user);
                                    //Передача имени для чата и переход из активити регистрации в активити чата
                                    Intent intent = new Intent(SignIn.this, UserListActivity.class);
                                    intent.putExtra("userName", nameEditText.getText().toString().trim());
                                    startActivity(intent);
                                }
                            }
                        });
            }
        }
    }

    //При создании нового пользователя данный метод отображает его данные в RealTime Firebase (можно будет видеть его на сайте)
    private void createUser(FirebaseUser firebaseUser){
        User user = new User();
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setName(nameEditText.getText().toString().trim());

        usersDatabaseReference.push().setValue(user);
    }
}