package fr.nantes.iut.ruvcom.Bean;

/**
 * Created by ughostephan on 19/01/2016.
 */
public class Photo extends BaseBean {
    private int id;
    private String url;
    private String filesize;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilesize() {
        return filesize;
    }

    public void setFilesize(String filesize) {
        this.filesize = filesize;
    }
}
