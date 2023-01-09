package android.kakoytochathz.user;

import android.kakoytochathz.R;
import android.kakoytochathz.main.ChatActivity;
import android.kakoytochathz.message.MessageAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private ArrayList<User> users;
    private OnUserClickListener listener;

    public interface OnUserClickListener{
        void onUserClick(int pos);
    }

    public void setOnUserClickListener(OnUserClickListener listener){
        this.listener = listener;
    }

    public UserAdapter(ArrayList<User> users){
        this.users = users;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        private ImageView avatarImageView;
        private TextView userNameTextView, lastSmsTextView;

        public UserViewHolder(@NonNull View itemView, OnUserClickListener listener) {
            super(itemView);

            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            lastSmsTextView = itemView.findViewById(R.id.lastSmsTextView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION){
                            listener.onUserClick(pos);
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_item, viewGroup, false);
        UserViewHolder viewHolder = new UserViewHolder(view, listener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int position) {
        User nowUser = users.get(position);
        userViewHolder.avatarImageView.setImageResource(nowUser.getAvatarResource());
        userViewHolder.userNameTextView.setText(nowUser.getName());
        userViewHolder.lastSmsTextView.setText(ChatActivity.lastSms); //Последнее сообщение из чата с пользователем
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
