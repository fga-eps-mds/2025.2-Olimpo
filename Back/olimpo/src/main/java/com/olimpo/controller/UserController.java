package com.olimpo.controller;

import com.olimpo.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.olimpo.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.create(user.getUsername(), user.getPassword());
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/confirm")
    public String getString(){
        return "confirm";
    }
}
