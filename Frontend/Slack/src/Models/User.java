package Models;

import Controllers.DBSupport;
/**
 * Model for the user, holds the same data that the server would and contains the static calls to the dbsupport
 */
public class User {

    private String name;
    private String password;
    private Integer userId;

    public User(String uName, String uPassword){
        name = uName;
        password = uPassword;
        userId = -1;
    }

    public User(String uName, String uPassword, Integer uId){
        name = uName;
        password = uPassword;
        userId = uId;
    }

    /**
     * Calls DBSupport and returns the response
     * @param username
     * @param password
     * @return
     */
    public static DBSupport.HTTPResponse signIn(String username, String password) {
        DBSupport.HTTPResponse res = DBSupport.signin(username, password);
        return res;
    }

    /**
     * Calls DBSupport and returns the response
     * @param senderId
     * @return
     */
    public static DBSupport.HTTPResponse getUserNameByID(Integer senderId) {
        DBSupport.HTTPResponse res = DBSupport.getUserNameByID(senderId);
        return res;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String username) {
        password = username;
    }


    public static DBSupport.HTTPResponse searchUser(String name){
        DBSupport.HTTPResponse res = DBSupport.searchUser(name);
        return res;
    }

    public static DBSupport.HTTPResponse createUser(String name, String password){
        DBSupport.HTTPResponse res = DBSupport.createUser(name, password);
        return res;
    }

}
