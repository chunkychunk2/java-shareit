package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient userClient;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(UserController.class)
    static class TestConfig {
    }

    @Test
    void createUser() throws Exception {
        UserDto userDto = new UserDto(1L, "Alex", "alex@example.com");
        UserCreateDto createDto = new UserCreateDto("Alex", "alex@example.com");

        when(userClient.createUser(any(UserCreateDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(userDto));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(createDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userDto.getId().intValue())))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userClient).createUser(any(UserCreateDto.class));
    }

    @Test
    void updateUser() throws Exception {
        long userId = 1L;
        UserUpdateDto updateReq = new UserUpdateDto("Alex", "alex@example.com");
        UserDto updatedDto = new UserDto(userId, "Alex", "alex@example.com");

        when(userClient.updateUser(eq(userId), any(UserUpdateDto.class)))
                .thenReturn(ResponseEntity.ok(updatedDto));

        mvc.perform(patch("/users/{id}", userId)
                        .content(mapper.writeValueAsString(updateReq))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) userId)))
                .andExpect(jsonPath("$.name", is("Alex")))
                .andExpect(jsonPath("$.email", is("alex@example.com")));

        verify(userClient).updateUser(eq(userId), any(UserUpdateDto.class));
    }

    @Test
    void getUserById() throws Exception {
        UserDto userDto = new UserDto(1L, "Alex", "alex@example.com");

        when(userClient.getUser(eq(1L)))
                .thenReturn(ResponseEntity.ok(userDto));

        mvc.perform(get("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId().intValue())))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userClient).getUser(1L);
    }

    @Test
    void getAllUsers() throws Exception {
        UserDto u1 = new UserDto(1L, "Alex", "alex@example.com");
        UserDto u2 = new UserDto(2L, "Max", "max@example.com");

        when(userClient.getAllUsers())
                .thenReturn(ResponseEntity.ok(List.of(u1, u2)));

        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].name", is("Alex")))
                .andExpect(jsonPath("$[1].email", is("max@example.com")));

        verify(userClient).getAllUsers();
    }

    @Test
    void deleteUser() throws Exception {
        when(userClient.deleteUser(1L))
                .thenReturn(ResponseEntity.noContent().build());

        mvc.perform(delete("/users/1")).andExpect(status().isNoContent());

        verify(userClient).deleteUser(1L);
    }
}