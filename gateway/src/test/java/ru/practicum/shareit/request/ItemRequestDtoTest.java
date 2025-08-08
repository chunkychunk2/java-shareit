package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import org.springframework.context.annotation.Configuration;
import ru.practicum.shareit.request.dto.ItemResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestDtoTest {

    @Autowired
    JacksonTester<ItemRequestDto> json;

    private ItemRequestDto dto;

    @Configuration
    static class TestConfig {
    }

    @BeforeEach
    void setup() {
        LocalDateTime now = LocalDateTime.of(2025,5,28,12,0);
        ItemResponseDto resp = new ItemResponseDto(5L, "Book", 2L);
        dto = new ItemRequestDto(42L, "Need a book", now, List.of(resp));
    }

    @Test
    void serialize() throws Exception {
        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(42);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need a book");
        assertThat(result).hasJsonPathStringValue("$.created");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Book");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(2);
    }

    @Test
    void deserialize() throws Exception {
        String content = json.write(dto).getJson();
        ItemRequestDto parsed = json.parse(content).getObject();
        assertThat(parsed).usingRecursiveComparison().isEqualTo(dto);
    }
}
