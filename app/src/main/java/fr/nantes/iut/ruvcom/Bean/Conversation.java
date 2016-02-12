package fr.nantes.iut.ruvcom.Bean;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ughostephan on 20/01/2016.
 */
public class Conversation extends BaseBean implements Comparable<Conversation> {

    private User user;
    private Boolean notification;
    private Date lastDateMessage;

    public Conversation() {
        // Default Constructor
    }

    public Conversation(JSONObject data) {
        if (data != null) {
            try {
                this.user = new User(data.getJSONObject("user"));
                this.notification = data.getBoolean("notification");
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(data.getString("lastMessage"));
                this.lastDateMessage = date;
            } catch (JSONException e) {
                Log.e("CONVERSATION", e.getMessage());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getNotification() {
        return notification;
    }

    public void setNotification(Boolean notification) {
        this.notification = notification;
    }

    public Date getLastDateMessage() {
        return lastDateMessage;
    }

    public void setLastDateMessage(Date lastDateMessage) {
        this.lastDateMessage = lastDateMessage;
    }

    @Override
    public String toString() {
        return "{"+
                    "user:" + user.toString() + "," +
                    "notification:" + notification + "," +
                    "lastDateMessage" + lastDateMessage +
                "}";
    }

    @Override
    public int compareTo(Conversation o) {
        return getLastDateMessage().compareTo(o.getLastDateMessage());
    }
}
