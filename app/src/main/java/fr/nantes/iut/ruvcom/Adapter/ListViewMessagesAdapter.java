package fr.nantes.iut.ruvcom.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import fr.nantes.iut.ruvcom.Activities.FullScreenImageActivity;
import fr.nantes.iut.ruvcom.Bean.Message;
import fr.nantes.iut.ruvcom.Bean.User;
import fr.nantes.iut.ruvcom.R;
import fr.nantes.iut.ruvcom.Utils.RUVComUtils;

/**
 * Created by ughostephan on 20/01/2016.
 */
public class ListViewMessagesAdapter extends BaseAdapter {

    private List<Message> list;
    private final Context _c;
    private User user;
    private User distantUser;
    private String url_image = "";

    private final ImageLoader imageLoader = ImageLoader.getInstance();

    public ListViewMessagesAdapter(Context context, List<Message> list, User user, User distantUser) {
        this._c = context;
        this.list = list;
        this.user = user;
        this.distantUser = distantUser;
    }

    public void setDistantUser(User distantUser) {
        this.distantUser = distantUser;
    }

    public void setList(List<Message> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Message getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) getItem(position).getId();
    }

    private static class ViewHolder {
        public CircularImageView avatar;
        public TextView message;
        public RoundedImageView image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        View v = null;
        final Message message = getItem(position);

        LayoutInflater vi = (LayoutInflater) _c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        holder = new ViewHolder();

        if (message.getPhoto() != null) {
            // MESSAGES PHOTO
            if (message.getIdUserSender() == user.getId()) {
                // MY MESSAGE PHOTO
                v = vi.inflate(R.layout.item_message_photo_right, null);
                holder.avatar = (CircularImageView) v.findViewById(R.id.item_message_photo_right_avatar);
                holder.image = (RoundedImageView) v.findViewById(R.id.item_message_photo_right_image);
            } else {
                // MESSAGE PHOTO FROM DISTANT USER
                v = vi.inflate(R.layout.item_message_photo_left, null);
                holder.avatar = (CircularImageView) v.findViewById(R.id.item_message_photo_left_avatar);
                holder.image = (RoundedImageView) v.findViewById(R.id.item_message_photo_left_image);
            }
        } else {
            // MESSAGES TEXT
            if (message.getIdUserSender() == user.getId()) {
                // MY MESSAGE TEXT
                v = vi.inflate(R.layout.item_message_right, null);
                holder.avatar = (CircularImageView) v.findViewById(R.id.item_message_right_avatar);
                holder.message = (TextView) v.findViewById(R.id.item_message_right_text);
            } else {
                // MESSAGE TEXT FROM DISTANT USER
                v = vi.inflate(R.layout.item_message_left, null);
                holder.avatar = (CircularImageView) v.findViewById(R.id.item_message_left_avatar);
                holder.message = (TextView) v.findViewById(R.id.item_message_left_text);
            }
        }

        v.setTag(holder);


        url_image = "";

        if (message.getIdUserSender() == user.getId()) {
            // USER
            url_image = user.getImageUrl();
        } else {
            // USER DISTANT
            url_image = distantUser.getImageUrl();
        }

        if(!url_image.equals("")) {
            imageLoader.displayImage(url_image, holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.ic_project);
        }

        if (message.getPhoto() != null) {
            // MESSAGE PHOTO
            imageLoader.displayImage(message.getPhoto().getUrl(), holder.image);


            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent fullScreenImageIntent = new Intent(_c, FullScreenImageActivity.class);
                    fullScreenImageIntent.putExtra("imageUrl", message.getPhoto().getUrl());
                    fullScreenImageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    _c.startActivity(fullScreenImageIntent);
                }
            });

            holder.image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // TODO : téléchargement de l'image
                    Toast.makeText(_c, "A venir téléchargement de l'image", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        } else {
            // MESSAGE TEXT
            String m = message.getMessage();
            m = RUVComUtils.removeUTFCharacters(m);
            holder.message.setText(m);
        }

        return v;
    }
}
