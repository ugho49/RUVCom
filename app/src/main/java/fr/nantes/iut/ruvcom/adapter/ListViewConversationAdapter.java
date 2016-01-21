package fr.nantes.iut.ruvcom.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.List;

import fr.nantes.iut.ruvcom.bean.Conversation;
import fr.nantes.iut.ruvcom.R;

/**
 * Created by ughostephan on 20/01/2016.
 */
public class ListViewConversationAdapter extends BaseAdapter {

    private List<Conversation> list;
    private final Context _c;

    private final ImageLoader imageLoader = ImageLoader.getInstance();

    public ListViewConversationAdapter(Context context, List<Conversation> list) {
        this._c = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Conversation getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) getItem(position).getUser().getId();
    }

    private static class ViewHolder {
        public CircularImageView avatar;
        public TextView displayName;
        public TextView email;
        public TextView lastDateMessage;
        public ImageView notification;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        View v = convertView;
        LayoutInflater vi = (LayoutInflater) _c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Conversation conversation = getItem(position);

        if (v == null) {
            v = vi.inflate(R.layout.item_conversation, null);
            holder = new ViewHolder();
            holder.avatar = (CircularImageView) v.findViewById(R.id.item_conv_avatar);
            holder.displayName = (TextView) v.findViewById(R.id.item_conv_displayname);
            holder.email = (TextView) v.findViewById(R.id.item_conv_email);
            holder.lastDateMessage = (TextView) v.findViewById(R.id.item_conv_lastmessage);
            holder.notification = (ImageView) v.findViewById(R.id.item_conv_notification);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        final String url_image = conversation.getUser().getImageUrl();
        final String lastDate = new SimpleDateFormat("dd/MM/yyyy - HH:mm").format(conversation.getLastDateMessage());

        if(!url_image.equals("")) {
            imageLoader.displayImage(url_image, holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.ic_project);
        }

        holder.displayName.setText(conversation.getUser().getDisplayName());
        holder.email.setText(conversation.getUser().getEmail());
        holder.lastDateMessage.setText(lastDate);

        if (conversation.getNotification()) {
            holder.notification.setVisibility(View.VISIBLE);
        } else {
            holder.notification.setVisibility(View.INVISIBLE);
        }

        return v;
    }
}
