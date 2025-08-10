package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestClient requestClient;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(ItemRequestController.class)
    static class TestConfig {
    }

    @Test
    void createRequest() throws Exception {
        long userId = 1L;
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Need a book");
        LocalDateTime now = LocalDateTime.now();
        ItemResponseDto resp = new ItemResponseDto(5L, "Book", 2L);
        ItemRequestDto responseDto = new ItemRequestDto(10L, "Need a book", now, List.of(resp));

        when(requestClient.createRequest(eq(userId), any(ItemRequestCreateDto.class)))
                .thenReturn(ResponseEntity.status(201).body(responseDto));

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(createDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.description", is("Need a book")))
                .andExpect(jsonPath("$.items[0].id", is(5)))
                .andExpect(jsonPath("$.items[0].name", is("Book")))
                .andExpect(jsonPath("$.items[0].ownerId", is(2)));

        verify(requestClient).createRequest(eq(userId), any(ItemRequestCreateDto.class));
    }

    @Test
    void getOwnRequests() throws Exception {
        long userId = 1L;
        ItemResponseDto resp = new ItemResponseDto(5L, "Book", 2L);
        ItemRequestDto dto1 = new ItemRequestDto(10L, "Need a book", LocalDateTime.now(), List.of(resp));
        ItemRequestDto dto2 = new ItemRequestDto(11L, "Need a cup", LocalDateTime.now(), List.of());

        when(requestClient.getOwnRequests(eq(userId)))
                .thenReturn(ResponseEntity.ok(List.of(dto1, dto2)));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[1].id", is(11)));

        verify(requestClient).getOwnRequests(userId);
    }

    @Test
    void getAllRequests() throws Exception {
        long userId = 1L;
        int from = 0, size = 10;
        ItemRequestDto dto = new ItemRequestDto(12L, "Need a book", LocalDateTime.now(), List.of());

        when(requestClient.getAllRequests(eq(userId), eq(from), eq(size)))
                .thenReturn(ResponseEntity.ok(List.of(dto)));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(12)));

        verify(requestClient).getAllRequests(userId, from, size);
    }

    @Test
    void getRequestById_ReturnsOk() throws Exception {
        long userId = 1L;
        long reqId = 20L;
        ItemRequestDto dto = new ItemRequestDto(reqId, "Need a lamp", LocalDateTime.now(), List.of());

        when(requestClient.getRequestById(eq(userId), eq(reqId)))
                .thenReturn(ResponseEntity.ok(dto));

        mvc.perform(get("/requests/{id}", reqId)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Need a lamp")))
                .andExpect(jsonPath("$.id", is(20)));

        verify(requestClient).getRequestById(userId, reqId);
    }
}
