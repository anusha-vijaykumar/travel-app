package com.springcloud.user_service.service;

import com.springcloud.user_service.dto.UserDto;
import com.springcloud.user_service.entity.User;
import com.springcloud.user_service.repository.UserRepository;
import com.springcloud.user_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserServiceImpl userService = new UserServiceImpl(userRepository);

    @Test
    void getUserByIdMapsEntityToDto() {
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        when(userRepository.getUserById(1L)).thenReturn(new User(1L, "Ada", "Lovelace", "ada@example.com", createdAt));

        UserDto user = userService.getUserById(1L);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getFirstname()).isEqualTo("Ada");
        assertThat(user.getLastname()).isEqualTo("Lovelace");
        assertThat(user.getEmail()).isEqualTo("ada@example.com");
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void getUserByEmailMapsEntityToDto() {
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        when(userRepository.getUserByEmail("grace@example.com"))
                .thenReturn(new User(2L, "Grace", "Hopper", "grace@example.com", createdAt));

        UserDto user = userService.getUserByEmail("grace@example.com");

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getEmail()).isEqualTo("grace@example.com");
    }

    @Test
    void createUserSavesMappedEntity() {
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        UserDto userDto = new UserDto(3L, "Katherine", "Johnson", "katherine@example.com", createdAt);

        userService.createUser(userDto);

        verify(userRepository).save(any(User.class));
    }
}
