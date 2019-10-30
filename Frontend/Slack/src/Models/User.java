package Models;

import Controllers.DBSupport;

public class User {

    private String name;
    private String password;

    public User(String uName, String uPassword){
        name = uName;
        password = uPassword;
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

    DBSupport.HTTPResponse createUser(String name, String password){
        DBSupport.HTTPResponse res = DBSupport.createUser(name, password);
        return res;
    }

    /** to go into message not user
    DBSupport.HTTPResponse pinMessage(Integer id){
        DBSupport.HTTPResponse res = DBSupport.pinMessage(id);
        return res;
    }*/

}
