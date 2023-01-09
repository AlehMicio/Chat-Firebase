package android.kakoytochathz.message;

import android.app.Activity;
import android.kakoytochathz.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {

    private List<Message> messages;
    private Activity activity;
    public static String lastSmsText;

    public MessageAdapter(Activity context, int resource, List<Message> messages) {
        super(context, resource, messages);
        this.messages = messages;
        this.activity = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        //Переключение разметок в зависимости от того праметра isMine, т.е. в зависимости от кого исходит сообщение
        Message message = getItem(position);
        int layoutResource = 0;
        int viewType = getItemViewType(position);

        if (viewType == 0){
            layoutResource = R.layout.my_message_item;
        }
        else layoutResource = R.layout.your_message_item;

        if (convertView != null){ //ConvertView - наш объект
            viewHolder = (ViewHolder) convertView.getTag();
        }
        else {
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        //Проверка: является ли сообщение текстом или фото
        boolean isText = message.getImageUrl() == null; //Если нет imageUrl, то это текст. Если есть imageUrl, то это картинка
        if (isText){
            viewHolder.messageTextView.setVisibility(View.VISIBLE); //Если это текст, то значит выводим в чат текст
            viewHolder.photoImageView.setVisibility(View.GONE);//А View картинки невидимой GONE - чтобы убирался и сам вид (орнамент)
            viewHolder.messageTextView.setText(message.getText());
            lastSmsText = message.getText();
        }
        else { //Иначе наоборот: картинка видна, текст нет
            viewHolder.messageTextView.setVisibility(View.GONE);
            viewHolder.photoImageView.setVisibility(View.VISIBLE);
            //Получаем Url и указываем - куда хоти загрузить
            Glide.with(viewHolder.photoImageView.getContext()).load(message.getImageUrl()).into(viewHolder.photoImageView);
            lastSmsText = "Фото";
        }

       //Старая версия кода, без ViewHolder (проверка: является сообщение текстом или фото)
       /* if (convertView == null){  //ConverView - наш объект
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.message_item, parent, false);
        }

        ImageView fotoImage = convertView.findViewById(R.id.fotoImage);
        TextView messageTextView = convertView.findViewById(R.id.messageTextView);
        TextView nameTextView = convertView.findViewById(R.id.nameTextView);

        boolean isText = message.getImageUrl() == null; //Если нет imageUrl, то это текст. Если есть imageUrl, то это картинка
        if (isText){
            messageTextView.setVisibility(View.VISIBLE); //Если это текст, то значит выводим в чат текст
            fotoImage.setVisibility(View.GONE);//А View картинки невидимой GONE - чтобы убирался и сам вид (орнамент)
            messageTextView.setText(message.getText());
        }
        else { //Иначе наоборот: кратинка видна, текст нет
         messageTextView.setVisibility(View.GONE);
         fotoImage.setVisibility(View.VISIBLE);
         //Получаем Url и указываем - куда хоти загрузить
         Glide.with(fotoImage.getContext()).load(message.getImageUrl()).into(fotoImage);
        }

        nameTextView.setText(message.getName()); //Устанавливаем текст для Имени
            */
        return convertView;
    }

    @Override //Переопределяем метод коренного адаптера
    public int getItemViewType(int position) {
        int flag;
        Message message = messages.get(position);
        if (message.isMine()) flag = 0;
        else flag = 1;

        return flag;
    }

    @Override //Переопределяем метод коренного адаптера. Возвращаем кол-во разметок, которые имеем
    public int getViewTypeCount() {
        return 2;
    }

    private class ViewHolder{
        private TextView messageTextView;
        private ImageView photoImageView;

        public ViewHolder(View view){
            photoImageView = view.findViewById(R.id.hotoImageView);
            messageTextView = view.findViewById(R.id.messageTextView);
        }

    }
}
