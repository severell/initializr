package com.severell.initializr.auth;

import com.severell.core.crypto.PasswordUtils;
import com.severell.core.http.NeedsRequest;
import com.severell.initializr.models.User;
import com.severell.initializr.models.query.QUser;


import java.util.List;

public class Auth extends NeedsRequest {

    public boolean login(String username, String password) {
        User user = new QUser().email.equalTo(username).findOne();
        boolean auth = false;
        if(user != null) {
            auth = PasswordUtils.checkPassword(password, user.getPassword());
            if(auth) {
                request.getSession().setAttribute("userid", user.getId());
            }
        }

        return auth;
    }

}