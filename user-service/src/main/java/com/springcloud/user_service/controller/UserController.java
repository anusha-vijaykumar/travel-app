package com.springcloud.user_service.controller;


import com.springcloud.user_service.dto.UserDto;
import com.springcloud.user_service.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") Long id){
        UserDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable("email") String email){
        UserDto userDto = userService.getUserByEmail(email);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto){
        userService.createUser(userDto);
        return ResponseEntity.ok(userDto);
    }
}
