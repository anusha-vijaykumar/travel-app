package com.springcloud.user_service.controller;

import com.springcloud.user_service.dto.UserDto;
import com.springcloud.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private final UserController userController = new UserController(userService);

    @Test
    void getUserByIdReturnsUser() {
        UserDto userDto = new UserDto(1L, "Ada", "Lovelace", "ada@example.com", new Timestamp(1L));
        when(userService.getUserById(1L)).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.getUserById(1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(userDto);
    }

    @Test
    void createUserDelegatesToServiceAndReturnsRequestBody() {
        UserDto userDto = new UserDto(1L, "Ada", "Lovelace", "ada@example.com", new Timestamp(1L));

        ResponseEntity<UserDto> response = userController.createUser(userDto);

        verify(userService).createUser(userDto);
        assertThat(response.getBody()).isEqualTo(userDto);
    }
}
