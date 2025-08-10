package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoTest {

    @Autowired
    private JacksonTester<UserDto> json;

    private UserDto dto;

    @BeforeEach
    void setup() {
        dto = new UserDto(
                5L,
                "Alex",
                "alex@example.com"
        );
    }

    @Test
    void serializeUserDto() throws IOException {
        JsonContent<UserDto> content = json.write(dto);

        assertThat(content).isNotNull();
        assertThat(content)
                .hasJsonPathNumberValue("$.id")
                .extractingJsonPathNumberValue("$.id").isEqualTo(5);
        assertThat(content)
                .hasJsonPathStringValue("$.name")
                .extractingJsonPathStringValue("$.name").isEqualTo("Alex");
        assertThat(content)
                .hasJsonPathStringValue("$.email")
                .extractingJsonPathStringValue("$.email").isEqualTo("alex@example.com");
    }

    @Test
    void deserializeUserDto() throws IOException {
        String userJson = "{\n" +
                "  \"id\": 42,\n" +
                "  \"name\": \"Bob\",\n" +
                "  \"email\": \"bob@mail.com\"\n" +
                "}";

        UserDto result = json.parseObject(userJson);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getName()).isEqualTo("Bob");
        assertThat(result.getEmail()).isEqualTo("bob@mail.com");
    }
}
