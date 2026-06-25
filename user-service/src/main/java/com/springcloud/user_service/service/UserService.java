package com.springcloud.user_service.service;

import com.springcloud.user_service.dto.UserDto;

public interface UserService {
    UserDto getUserByEmail(String email);
    UserDto getUserById(Long id);
    void createUser(UserDto userDto);
}
