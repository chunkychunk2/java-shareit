package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.context.annotation.Configuration;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoTest {

    @Autowired
    JacksonTester<UserDto> json;

    private UserDto dto;

    @Configuration
    static class TestConfig {
    }

    @BeforeEach
    void setup() {
        dto = new UserDto(5L, "Alex", "alex@example.com");
    }

    @Test
    void serialize() throws Exception {
        JsonContent<UserDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Alex");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("alex@example.com");
    }

    @Test
    void deserialize() throws Exception {
        String content = json.write(dto).getJson();
        UserDto parsed = json.parse(content).getObject();
        assertThat(parsed).usingRecursiveComparison().isEqualTo(dto);
    }
}
