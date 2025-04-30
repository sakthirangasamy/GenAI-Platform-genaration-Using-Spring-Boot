package com.ai.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.demo.entity.User;
import com.ai.demo.repo.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(User user) {
        // You can add validations or password encoding here
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User authenticate(String email, String password) {
        // Retrieve user by email
        Optional<User> userOpt = userRepository.findByEmail(email);

        // Check if user exists and password matches
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                return user;  // Return the user if authentication is successful
            }
        }

        return null;  // Return null if the user doesn't exist or the password doesn't match
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
