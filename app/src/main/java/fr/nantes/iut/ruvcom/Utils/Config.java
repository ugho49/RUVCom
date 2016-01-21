package fr.nantes.iut.ruvcom.Utils;

/**
 * Created by ughostephan on 18/01/2016.
 */
public class Config {
    // Secret token pour l'api
    public static final String SECRET_TOKEN = "Ok2CCaEaHngyNPMqPRcE5MkvzIAwpnrJc5zECIO9fAW9dnxI1zppPvRKu7pnU8tbeFrjjke5m8wDacadWWWOFgiLcr1xvdxhUVMA1WWTJkLiFFmFAAGwS";
    // Directory name to store captured images and videos
    public static final String LOCAL_IMAGE_DIRECTORY_NAME = "RUVComFileUpload";
    // API KEY
    public static final String API_KEY = "AIzaSyCoFVlSOijLQC1HlQswRQ3-YI8EkeODTZQ";
    // Url de base de L'API
    public static final String BASE_API_URL = "http://dev.ugho-stephan.fr/api/";
    // PARAM : googleID
    public static final String API_USER_EXIST = BASE_API_URL + "userExists/%s";
    // PARAM : NULL
    public static final String API_USER_GET_ALL = BASE_API_URL + "getUsers";
    // PARAM : idUser
    public static final String API_USER_GET_ALL_EXCEPT_ME = BASE_API_URL + "getUsers/%s";
    // PARAM : googleID, displayName, email
    public static final String API_USER_CREATE = BASE_API_URL + "createUser/%s/%s/%s";
    // PARAM : id, googleID, displayName, email
    public static final String API_USER_UPDATE = BASE_API_URL + "updateUser/%s/%s/%s/%s";
    // PARAM : id
    public static final String API_CONVERSATIONS_GET = BASE_API_URL + "getConversations/%s";
    // PARAM : idSender, idReceiver
    public static final String API_MESSAGES_GET = BASE_API_URL + "getMessages/%s/%s";
    // PARAM : idSender, idReceiver
    public static final String API_MESSAGE_POST = BASE_API_URL + "createMessage/%s/%s";
    // PARAM : idSender, idReceiver
    public static final String API_UPLOAD_PICTURE = BASE_API_URL + "uploadPicture/%s/%s";
    // PARAM : idUser, idDistantUser
    public static final String API_UPDATE_MESSAGE_READ = BASE_API_URL + "setMessageRead/%s/%s";
}
