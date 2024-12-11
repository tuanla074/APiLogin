package com.example.apilogin.Service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Service
public class LoginService {

    private Properties users;

    public LoginService() {
        loadUsers();
    }

    private void loadUsers() {
        users = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("user.properties")) {
            if (inputStream == null) {
                throw new IOException("Unable to find user.properties");
            }
            users.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean authenticate(String username, String password) {
        String storedPassword = users.getProperty(username);
        return storedPassword != null && storedPassword.equals(password);
    }
}
