package ru.practicum.shareit.item;

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
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ShareItServer.class)
@AutoConfigureMockMvc
@Transactional
public class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ItemRepository itemRepo;

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private CommentRepository commentRepo;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setup() {
        bookingRepo.deleteAll();
        itemRepo.deleteAll();
        userRepo.deleteAll();

        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@mail.com");
        owner = userRepo.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@mail.com");
        booker = userRepo.save(booker);

        item = new Item();
        item.setName("Book");
        item.setDescription("For IT");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepo.save(item);
    }

    @Test
    void createItem() throws Exception {
        ItemDto toCreate = new ItemDto();
        toCreate.setName("Book");
        toCreate.setDescription("For IT");
        toCreate.setAvailable(true);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", owner.getId())
                        .content(mapper.writeValueAsString(toCreate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Book"))
                .andExpect(jsonPath("$.description").value("For IT"));
    }

    @Test
    void getById() throws Exception {
        mvc.perform(get("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.name").value("Book"));
    }

    @Test
    void getAllByUser() throws Exception {
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(item.getId()));
    }

    @Test
    void search() throws Exception {
        mvc.perform(get("/items/search")
                        .param("text", "it")
                        .header("X-Sharer-User-Id", owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(item.getId()));
    }

    @Test
    void updateItem() throws Exception {
        ItemDto toUpdate = new ItemDto();
        toUpdate.setId(item.getId());
        toUpdate.setName("eBook");
        toUpdate.setDescription("Electronic book");
        toUpdate.setAvailable(false);

        mvc.perform(put("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .content(mapper.writeValueAsString(toUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("eBook"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void updateItemPatch() throws Exception {
        ItemDto patch = new ItemDto();
        patch.setId(item.getId());
        patch.setName("BookX");

        mvc.perform(patch("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .content(mapper.writeValueAsString(patch))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("BookX"));
    }

    @Test
    void addComment() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Booking past = new Booking();
        past.setItem(item);
        past.setBooker(booker);
        past.setStart(now.minusDays(2));
        past.setEnd(now.minusDays(1));
        past.setStatus(BookingStatus.APPROVED);
        bookingRepo.save(past);

        CommentRequestDto cReq = new CommentRequestDto("Great book");
        String cJson = mvc.perform(post("/items/{id}/comment", item.getId())
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(mapper.writeValueAsString(cReq))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great book"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        CommentDto comment = mapper.readValue(cJson, CommentDto.class);
        assertThat(comment.getAuthorName()).isEqualTo(booker.getName());
    }

    @Test
    void getByIdWithComments() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Booking past = new Booking();
        past.setItem(item);
        past.setBooker(booker);
        past.setStart(now.minusDays(2));
        past.setEnd(now.minusDays(1));
        past.setStatus(BookingStatus.APPROVED);
        bookingRepo.save(past);

        CommentRequestDto cReq = new CommentRequestDto("Nice");
        mvc.perform(post("/items/{id}/comment", item.getId())
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(mapper.writeValueAsString(cReq))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // затем возвращаем вещь и проверяем, что в ответе есть этот коммент
        mvc.perform(get("/items/{id}", item.getId())
                        .header("X-Sharer-User-Id", owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments[0].text").value("Nice"));
    }
}
