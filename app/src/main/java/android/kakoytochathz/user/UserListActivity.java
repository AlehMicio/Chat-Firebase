package android.kakoytochathz.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.kakoytochathz.R;
import android.kakoytochathz.main.ChatActivity;
import android.kakoytochathz.main.SignIn;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    private DatabaseReference userDatabaseReference;
    private ChildEventListener userChildEventListener;
    private ArrayList<User> userArrayList;
    private RecyclerView userListRecyclerView;
    private UserAdapter userAdapter;
    private RecyclerView.LayoutManager userListLayoutManager;
    private FirebaseAuth auth;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        userArrayList = new ArrayList<>();
        UsersArrayListInit();
        Initialization();
    }

    private void Initialization(){
        auth = FirebaseAuth.getInstance();
        userListRecyclerView = findViewById(R.id.userListRecyclerView);
        userListRecyclerView.setHasFixedSize(true);
        userListLayoutManager = new LinearLayoutManager(this);
        userAdapter = new UserAdapter(userArrayList);
        userListRecyclerView.setLayoutManager(userListLayoutManager);
        userListRecyclerView.setAdapter(userAdapter);

        userAdapter.setOnUserClickListener(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(int pos) {
                goToChat(pos);
            }
        });

        Intent intent = getIntent();
        if (intent!=null){
            userName = intent.getStringExtra("userName");
        } else userName = "Default name";

        //Устанавливает разделительную горизонтальную линию между пользователями
        userListRecyclerView.addItemDecoration(new DividerItemDecoration(userListRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

    }

    private void UsersArrayListInit(){
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        if (userChildEventListener == null){
            userChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                     User user = snapshot.getValue(User.class);
                     if (!user.getId().equals(auth.getCurrentUser().getUid())){ //if - проверка на то, чтобы убрать самого пользователя из списка пользователей
                         user.setAvatarResource(R.drawable.ic_person);
                         userArrayList.add(user);
                         userAdapter.notifyDataSetChanged();
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
            userDatabaseReference.addChildEventListener(userChildEventListener);
        }
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
                startActivity(new Intent(UserListActivity.this, SignIn.class));
                return  true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void goToChat(int pos){
        Intent intent = new Intent(UserListActivity.this, ChatActivity.class);
        //При кликаньи по юзера в Recycler View на какой-то позиции,
        // мы извлекаем на той же позиции объект из Array List и получаем его id
        intent.putExtra("recipientId", userArrayList.get(pos).getId());
        intent.putExtra("recipientName", userArrayList.get(pos).getName());
        intent.putExtra("userName", userName);
        startActivity(intent);
    }
}