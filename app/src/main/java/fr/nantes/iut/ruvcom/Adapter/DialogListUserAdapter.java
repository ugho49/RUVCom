package fr.nantes.iut.ruvcom.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.BubbleImageView;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.List;

import fr.nantes.iut.ruvcom.Bean.Conversation;
import fr.nantes.iut.ruvcom.Bean.User;
import fr.nantes.iut.ruvcom.R;

/**
 * Created by ughostephan on 21/01/2016.
 */
public class DialogListUserAdapter extends BaseAdapter {

    private Context _c;
    List<User> list;

    private final ImageLoader imageLoader = ImageLoader.getInstance();

    public DialogListUserAdapter(Context _c, List<User> list) {
        this._c = _c;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public User getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) getItem(position).getId();
    }

    private static class ViewHolder {
        public CircularImageView avatar;
        public TextView displayName;
        public TextView email;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        View v = convertView;
        LayoutInflater vi = (LayoutInflater) _c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        User user = getItem(position);

        if (v == null) {
            v = vi.inflate(R.layout.item_user, null);
            holder = new ViewHolder();
            holder.avatar = (CircularImageView) v.findViewById(R.id.item_user_avatar);
            holder.displayName = (TextView) v.findViewById(R.id.item_user_displayname);
            holder.email = (TextView) v.findViewById(R.id.item_user_email);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        final String url_image = user.getImageUrl();

        if(!url_image.equals("")) {
            imageLoader.displayImage(url_image, holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.ic_project);
        }

        holder.displayName.setText(user.getDisplayName());
        holder.email.setText(user.getEmail());

        return v;
    }
}
