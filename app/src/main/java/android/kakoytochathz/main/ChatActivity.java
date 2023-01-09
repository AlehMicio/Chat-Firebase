package android.kakoytochathz.main;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.kakoytochathz.R;
import android.kakoytochathz.message.Message;
import android.kakoytochathz.message.MessageAdapter;
import android.kakoytochathz.user.User;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String userName, recipientId, recipientName;
    public static String lastSms;

    private ListView messageListView;
    private MessageAdapter adapter;
    private ProgressBar progressBar;
    private ImageButton sendImageButton;
    private Button sendMessageButton;
    private EditText messageEditText;

    private FirebaseDatabase database;
    private DatabaseReference messageDatabaseReference, usersDatabaseReference;
    private ChildEventListener messageChildEventListener, usersChildEventListener; //Нужен для взаимодействия с child - объектами
    private FirebaseStorage storage;
    private StorageReference chatImageStogageReferance;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        SharedPreferences sharedPreferences = this.getSharedPreferences("sms", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastSms", MessageAdapter.lastSmsText);
        editor.apply();
        lastSms = sharedPreferences.getString("lastSms", "");

        Initialization();
        DatabaseSettings();
        Settings();
    }

    private void Initialization(){

        database = FirebaseDatabase.getInstance(); //Получаем доступ ко всей БД приложения
        auth = FirebaseAuth.getInstance();
        messageDatabaseReference = database.getReference().child("message"); //Нужна для чтения в БД
        usersDatabaseReference = database.getReference().child("users");
        storage = FirebaseStorage.getInstance();
        chatImageStogageReferance = storage.getReference().child("chat_images");

        messageListView = findViewById(R.id.messageListView);
        sendImageButton = findViewById(R.id.sendImageButton);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messageEditText = findViewById(R.id.messageEditText);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.INVISIBLE); //Делаем изначально невидимым

        Intent intent = getIntent();
        if (intent != null) {
            userName = intent.getStringExtra("userName");
            recipientId = intent.getStringExtra("recipientId");
            recipientName = intent.getStringExtra("recipientName");
        }
        else userName = "Default name";

        setTitle(recipientName + ""); //Устанавливаем на Toolbar имя собеседника

        List<Message> messagesArrayList = new ArrayList<>();
        adapter = new MessageAdapter(this, R.layout.message_item, messagesArrayList);
        messageListView.setAdapter(adapter);

    }

    private void Settings(){
        //Когда в EditText появляет какой-то текст, то кнопка отправки должна быть активна
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override //Проверка на длину текста в сообщении
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.toString().trim().length() > 0){
                    sendMessageButton.setEnabled(true);
                }
                else sendMessageButton.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //Ограничение на кол-во символов, вводимое за раз в EditText
        messageEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(50)});

        //Клик по кнопке отправки
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Message message = new Message();
                message.setText(messageEditText.getText().toString());
                message.setName(userName);
                message.setSender(auth.getCurrentUser().getUid());
                message.setRecipient(recipientId);
                message.setImageUrl(null);

                messageDatabaseReference.push().setValue(message);

                messageEditText.setText("");
            }
        });

        //Клик по добавлению фото
        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Создаём интент для получение контента (пр: выбор фото)
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*"); //Указываем тип интента, который он будет получать (можно указать конретное расширение)
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true); //Берём файлы с внтуренней памяти телефона
                //123 - это код, по которому будет обращение к этому интенту
                startActivityForResult(Intent.createChooser(intent, "Выберите фото"), 123);
            }
        });
    }

    private void DatabaseSettings(){

        //Нужно для считывания из БД и добавления на экран вклакди massage
        messageChildEventListener = new ChildEventListener() {
            @Override //Когда добавляется child
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class); //Получаем объект с полями как в БД
                if (message.getSender().equals(auth.getCurrentUser().getUid())
                        && message.getRecipient().equals(recipientId)){
                    message.setMine(true); //Т.е. это я отправляю сообщение
                    adapter.add(message);
                }
                else if (message.getRecipient().equals(auth.getCurrentUser().getUid())
                            && message.getSender().equals(recipientId)){
                    message.setMine(false); //Т.е. это собеседник отправляет сообщение
                    adapter.add(message);
                }
            }

            @Override //Когда меняется child
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override //Когда удаляется child
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override //Когда перемещается child
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override //Когда произошла какая-то ошибка
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        messageDatabaseReference.addChildEventListener(messageChildEventListener); //Добавляем в БД

        usersChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User user = snapshot.getValue(User.class);
                //Проверка на совпадение id текущего пользоватля с id в БД
                //Нужно, чтобы взять данные этого пользователя из БД
                if (user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    userName = user.getName();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        usersDatabaseReference.addChildEventListener(usersChildEventListener); //Добавляем в БД
    }

    @Override //Меню три точки
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override //Кнопки меню
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ChatActivity.this, SignIn.class));
                return  true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override //Необходимо переопределить данный метод, т.к. используем выше startActivityForResult()
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //123 - то, что присвоили в startActivityForResult()
        //RESULT_OK - системная const, означающая, что выбор прошёл успешно
        if (requestCode == 123 && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();
            //Получаем сегмент Uri (пр: content://folder/3 - получим 3)
            StorageReference imageReferance = chatImageStogageReferance.child(selectedImageUri.getLastPathSegment());
            UploadTask uploadTask = imageReferance.putFile(selectedImageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageReferance.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Message message = new Message();
                        message.setImageUrl(downloadUri.toString());
                        message.setName(userName);
                        message.setSender(auth.getCurrentUser().getUid());
                        message.setRecipient(recipientId);
                        messageDatabaseReference.push().setValue(message);
                    } else {

                    }
                }
            });
        }
    }
}