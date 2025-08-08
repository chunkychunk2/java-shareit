package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ShareItServer.class)
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepo;

    private User user;

    @BeforeEach
    void setup() {
        userRepo.deleteAll();

        User u = new User();
        u.setName("Alex");
        u.setEmail("alex@example.com");
        user = userRepo.save(u);
    }

    @Test
    void createUser() throws Exception {
        UserDto toCreate = new UserDto(null, "John", "john@example.com");

        mvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(toCreate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        List<User> all = userRepo.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void getById() throws Exception {
        mvc.perform(get("/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value("Alex"))
                .andExpect(jsonPath("$.email").value("alex@example.com"));
    }

    @Test
    void getAll() throws Exception {
        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Alex"));
    }

    @Test
    void update() throws Exception {
        UserDto update = new UserDto(user.getId(), "Alex Updated", "alex.upd@example.com");

        mvc.perform(put("/users")
                        .content(objectMapper.writeValueAsString(update))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alex Updated"));
    }

    @Test
    void updatePatch() throws Exception {
        UserDto patch = new UserDto(null, "Alexey", "");

        mvc.perform(patch("/users/{id}", user.getId())
                        .content(objectMapper.writeValueAsString(patch))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alexey"))
                .andExpect(jsonPath("$.email").value("alex@example.com"));
    }

    @Test
    void deleteAndNotFoundAfter() throws Exception {
        mvc.perform(delete("/users/{id}", user.getId()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/users/{id}", user.getId()))
                .andExpect(status().isNotFound());
    }
}
