package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestDtoTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    private ItemRequestDto dto;

    @BeforeEach
    void setup() {
        ItemResponseDto resp = new ItemResponseDto(7L, "Book", 99L);
        dto = new ItemRequestDto(
                5L,
                "Need a book",
                LocalDateTime.of(2025, 5, 29, 12, 0, 0),
                List.of(resp)
        );
    }

    @Test
    void serializeItemRequestDto() throws IOException {
        JsonContent<ItemRequestDto> content = json.write(dto);

        assertThat(content).isNotNull();
        assertThat(content)
                .hasJsonPathNumberValue("$.id")
                .extractingJsonPathNumberValue("$.id").isEqualTo(5);
        assertThat(content)
                .hasJsonPathStringValue("$.description")
                .extractingJsonPathStringValue("$.description")
                .isEqualTo("Need a book");
        assertThat(content)
                .hasJsonPathStringValue("$.created")
                .extractingJsonPathStringValue("$.created")
                .startsWith("2025-05-29T12:00:00");
        assertThat(content)
                .hasJsonPathArrayValue("$.items")
                .extractingJsonPathNumberValue("$.items[0].id").isEqualTo(7);
        assertThat(content)
                .extractingJsonPathStringValue("$.items[0].name").isEqualTo("Book");
        assertThat(content)
                .extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(99);
    }

    @Test
    void deserializeItemRequestDto() throws IOException {
        String jsonString = "{\n" +
                "  \"id\": 10,\n" +
                "  \"description\": \"Looking for a book\",\n" +
                "  \"created\": \"2025-05-29T12:34:56\",\n" +
                "  \"items\": [\n" +
                "    { \"id\": 3, \"name\": \"Book\", \"ownerId\": 7 }\n" +
                "  ]\n" +
                "}";

        ItemRequestDto result = json.parseObject(jsonString);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getDescription()).isEqualTo("Looking for a book");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.parse("2025-05-29T12:34:56"));
        assertThat(result.getItems()).hasSize(1);
        ItemResponseDto item = result.getItems().get(0);
        assertThat(item.getId()).isEqualTo(3L);
        assertThat(item.getName()).isEqualTo("Book");
        assertThat(item.getOwnerId()).isEqualTo(7L);
    }
}
