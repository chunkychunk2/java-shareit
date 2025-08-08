package ru.practicum.shareit.item;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

@JsonTest
public class ItemDtoTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    private ItemDto dto;

    @BeforeEach
    void setup() {
        dto = new ItemDto(
                5L,
                "Book",
                "For IT",
                true,
                77L,
                null,
                null,
                Collections.emptyList()
        );
    }

    @Test
    void serializeItemDto() throws IOException {
        JsonContent<ItemDto> content = json.write(dto);

        assertThat(json.write(dto))
                .hasJsonPathNumberValue("$.id")
                .extractingJsonPathNumberValue("$.id").isEqualTo(5);
        assertThat(json.write(dto))
                .hasJsonPathStringValue("$.name")
                .extractingJsonPathStringValue("$.name").isEqualTo("Book");
        assertThat(json.write(dto))
                .hasJsonPathBooleanValue("$.available")
                .extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(json.write(dto))
                .hasJsonPathNumberValue("$.requestId")
                .extractingJsonPathNumberValue("$.requestId").isEqualTo(77);
        assertThat(content).extractingJsonPathValue("$.lastBooking").isNull();
        assertThat(content).extractingJsonPathValue("$.nextBooking").isNull();
        assertThat(content)
                .hasJsonPathArrayValue("$.comments")
                .extractingJsonPathArrayValue("$.comments")
                .isEmpty();
    }

    @Test
    void deserializeItemDto() throws IOException {
        String content = "{\n" +
                "  \"id\": 99,\n" +
                "  \"name\": \"Book\",\n" +
                "  \"description\": \"For Java\",\n" +
                "  \"available\": false,\n" +
                "  \"requestId\": null\n" +
                "}";

        ItemDto result = json.parseObject(content);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getName()).isEqualTo("Book");
        assertThat(result.getDescription()).isEqualTo("For Java");
        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getRequestId()).isNull();

        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
        assertThat(result.getComments()).isNullOrEmpty();
    }
}
