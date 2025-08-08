package ru.practicum.shareit.request;

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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(classes = ShareItServer.class)
@AutoConfigureMockMvc
@Transactional
public class ItemRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ItemRequestRepository requestRepo;

    @Autowired
    private ItemRepository itemRepo;

    private User requester;
    private User other;

    @BeforeEach
    void setup() {
        itemRepo.deleteAll();
        requestRepo.deleteAll();
        userRepo.deleteAll();

        requester = new User();
        requester.setName("Alex");
        requester.setEmail("alex@example.com");
        requester = userRepo.save(requester);

        other = new User();
        other.setName("Bob");
        other.setEmail("bob@example.com");
        other = userRepo.save(other);
    }

    @Test
    void createRequest() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Need a book");

        String json = mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", requester.getId())
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.description").value("Need a book"))
                .andExpect(jsonPath("$.items").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ItemRequestDto resp = mapper.readValue(json, ItemRequestDto.class);
        assertThat(requestRepo.findById(resp.getId())).isPresent();
    }

    @Test
    void getOwnRequests() throws Exception {
        ItemRequest r1 = new ItemRequest();
        r1.setDescription("First");
        r1.setRequester(requester);
        r1.setCreated(LocalDateTime.now().minusMinutes(5));
        r1 = requestRepo.save(r1);

        ItemRequest r2 = new ItemRequest();
        r2.setDescription("Second");
        r2.setRequester(requester);
        r2.setCreated(LocalDateTime.now());
        r2 = requestRepo.save(r2);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", requester.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(r2.getId().intValue()))
                .andExpect(jsonPath("$[1].id").value(r1.getId().intValue()));
    }

    @Test
    void getOtherRequests() throws Exception {
        ItemRequest r = new ItemRequest();
        r.setDescription("Need a cup");
        r.setRequester(requester);
        r.setCreated(LocalDateTime.now());
        r = requestRepo.save(r);

        Item item = new Item();
        item.setName("Cup");
        item.setDescription("For a tea");
        item.setAvailable(true);
        item.setOwner(other);
        item.setRequestId(r.getId());
        itemRepo.save(item);

        // другой пользователь запрашивает
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", other.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(r.getId().intValue()))
                .andExpect(jsonPath("$[0].items.length()").value(1))
                .andExpect(jsonPath("$[0].items[0].id").value(item.getId().intValue()));
    }

    @Test
    void getRequestById() throws Exception {
        ItemRequest r = new ItemRequest();
        r.setDescription("Example");
        r.setRequester(requester);
        r.setCreated(LocalDateTime.now());
        r = requestRepo.save(r);

        // пользователь может смотреть свой запрос
        mvc.perform(get("/requests/{id}", r.getId())
                        .header("X-Sharer-User-Id", requester.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(r.getId().intValue()))
                .andExpect(jsonPath("$.description").value("Example"));

        // любой другой тоже может смотреть
        mvc.perform(get("/requests/{id}", r.getId())
                        .header("X-Sharer-User-Id", other.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(r.getId().intValue()))
                .andExpect(jsonPath("$.description").value("Example"));
    }
}
