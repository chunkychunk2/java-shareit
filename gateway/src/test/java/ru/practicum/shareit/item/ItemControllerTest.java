package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(ItemController.class)
    static class TestConfig {
    }

    @Test
    void createItem() throws Exception {
        long userId = 42L;
        ItemCreateDto createDto = new ItemCreateDto("Book", "For IT", true, null);
        ItemDto responseDto = new ItemDto(1L, "Book", "For IT", true, null);

        when(itemClient.createItem(eq(userId), any(ItemCreateDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(createDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Book")))
                .andExpect(jsonPath("$.description", is("For IT")));

        verify(itemClient).createItem(eq(userId), any(ItemCreateDto.class));
    }

    @Test
    void addComment() throws Exception {
        long userId = 42L;
        long itemId = 7L;
        CommentRequestDto commentReq = new CommentRequestDto("Great book");
        CommentDto responseComment = new CommentDto(100L, "Great book", "Alex", LocalDateTime.now());

        when(itemClient.addComment(eq(userId), eq(itemId), any(CommentRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(responseComment));

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(commentReq))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.text", is("Great book")));

        verify(itemClient).addComment(eq(userId), eq(itemId), any(CommentRequestDto.class));
    }

    @Test
    void updateItem() throws Exception {
        long userId = 42L;
        long itemId = 5L;
        ItemCreateDto updateDto = new ItemCreateDto("Book", "For IT", true, 10L);
        ItemDto responseDto = new ItemDto(itemId, "Book", "For IT", true, 10L);

        when(itemClient.updateItem(eq(userId), eq(itemId), any(ItemCreateDto.class)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(updateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) itemId)))
                .andExpect(jsonPath("$.name", is("Book")));

        verify(itemClient).updateItem(eq(userId), eq(itemId), any(ItemCreateDto.class));
    }

    @Test
    void getById() throws Exception {
        long userId = 42L;
        long itemId = 3L;
        ItemDto responseDto = new ItemDto(itemId, "Book", "For IT", false, null);

        when(itemClient.getById(eq(userId), eq(itemId)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Book")))
                .andExpect(jsonPath("$.available", is(false)));

        verify(itemClient).getById(eq(userId), eq(itemId));
    }

    @Test
    void getItems() throws Exception {
        long userId = 42L;
        ItemDto dto1 = new ItemDto(1L, "Book", "For IT", true, null);
        ItemDto dto2 = new ItemDto(2L, "Cup", "For tea", true, null);

        when(itemClient.getItems(eq(userId)))
                .thenReturn(ResponseEntity.ok(List.of(dto1, dto2)));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)));

        verify(itemClient).getItems(eq(userId));
    }

    @Test
    void searchItems() throws Exception {
        long userId = 42L;
        String text = "book";
        ItemDto dto = new ItemDto(5L, "Book", "For IT", true, null);

        when(itemClient.searchItems(eq(userId), eq(text)))
                .thenReturn(ResponseEntity.ok(List.of(dto)));

        mvc.perform(get("/items/search")
                        .param("text", text)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(5)));

        verify(itemClient).searchItems(eq(userId), eq(text));
    }
}