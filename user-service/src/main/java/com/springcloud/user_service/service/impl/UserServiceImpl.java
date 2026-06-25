package com.springcloud.user_service.service.impl;

import com.springcloud.user_service.dto.UserDto;
import com.springcloud.user_service.entity.User;
import com.springcloud.user_service.repository.UserRepository;
import com.springcloud.user_service.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    @Override
    public UserDto getUserByEmail(String email) {
       User user = userRepository.getUserByEmail(email);
       UserDto userDto = new UserDto(user.getId(), user.getFirstname(), user.getLastname(), user.getEmail(), user.getCreatedAt());
       return userDto;
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.getUserById(id);
        UserDto userDto = new UserDto(user.getId(), user.getFirstname(), user.getLastname(), user.getEmail(), user.getCreatedAt());
        return userDto;
    }

    @Override
    public void createUser(UserDto userDto) {
        User user = new User(userDto.getId(), userDto.getFirstname(), userDto.getLastname(), userDto.getEmail(), userDto.getCreatedAt());
        userRepository.save(user);
    }
}
