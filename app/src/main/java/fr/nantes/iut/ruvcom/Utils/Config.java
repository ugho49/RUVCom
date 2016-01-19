package fr.nantes.iut.ruvcom.Utils;

/**
 * Created by ughostephan on 18/01/2016.
 */
public class Config {

    // HELPER
    /*String fs = String.format("The value of the float " +
            "variable is %f, while " +
            "the value of the " +
            "integer variable is %d, " +
            " and the string is %s",
            6.89, 8768, "String var");*/

    // Secret token pour l'api
    public static final String SECRET_TOKEN = "Ok2CCaEaHngyNPMqPRcE5MkvzIAwpnrJc5zECIO9fAW9dnxI1zppPvRKu7pnU8tbeFrjjke5m8wDacadWWWOFgiLcr1xvdxhUVMA1WWTJkLiFFmFAAGwS";

    // Url de base de L'API
    public static final String BASE_API_URL = "http://dev.ugho-stephan.fr/api/";
    // PARAM : googleID
    public static final String API_USER_EXIST = BASE_API_URL + "userExists/%s";
    // PARAM : NULL
    public static final String API_USER_GET_ALL = BASE_API_URL + "getUsers";
    // PARAM : googleID, displayName, email
    public static final String API_USER_CREATE = BASE_API_URL + "createUser/%s/%s/%s";
    // PARAM : id, googleID, displayName, email
    public static final String API_USER_UPDATE = BASE_API_URL + "updateUser/%s/%s/%s/%s";
}
