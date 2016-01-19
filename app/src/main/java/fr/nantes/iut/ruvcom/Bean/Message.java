package fr.nantes.iut.ruvcom.Bean;

import java.util.Date;

/**
 * Created by ughostephan on 19/01/2016.
 */
public class Message extends BaseBean {

    private int id;
    private int idUserSender;
    private int idUserReceiver;
    private String message;
    private Photo photo;
    private boolean isRead;
    private Date dateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUserSender() {
        return idUserSender;
    }

    public void setIdUserSender(int idUserSender) {
        this.idUserSender = idUserSender;
    }

    public int getIdUserReceiver() {
        return idUserReceiver;
    }

    public void setIdUserReceiver(int idUserReceiver) {
        this.idUserReceiver = idUserReceiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }
}
