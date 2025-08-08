package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.context.annotation.Configuration;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoTest {

    @Autowired
    JacksonTester<ItemDto> json;

    private ItemDto dto;

    @Configuration
    static class TestConfig {
    }

    @BeforeEach
    void setup() {
        dto = new ItemDto(
                5L,
                "Book",
                "For IT",
                true,
                77L
        );
    }

    @Test
    void serialize() throws Exception {
        JsonContent<ItemDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Book");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("For IT");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(77);
    }

    @Test
    void deserialize() throws Exception {
        String content = json.write(dto).getJson();
        ItemDto parsed = json.parse(content).getObject();

        assertThat(parsed).usingRecursiveComparison().isEqualTo(dto);
    }
}
