package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.context.annotation.Configuration;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoTest {

    @Autowired
    JacksonTester<BookingDto> json;

    private BookingDto dto;
    private LocalDateTime start;
    private LocalDateTime end;

    @Configuration
    static class TestConfig {
    }

    @BeforeEach
    void setup() {
        start = LocalDateTime.of(2025, 5, 28, 12, 00);
        end = start.plusHours(2);
        dto = new BookingDto(
                100L,
                start,
                end,
                BookingState.WAITING,
                new BookingDto.BookerInfo(42L),
                new BookingDto.ItemInfo(5L, "Book")
        );
    }

    @Test
    void serialize() throws Exception {
        JsonContent<BookingDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(100);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(42);
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Book");

        String isoStart = start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String isoEnd   = end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assertThat(result).extractingJsonPathStringValue("$.start").startsWith(isoStart);
        assertThat(result).extractingJsonPathStringValue("$.end").startsWith(isoEnd);
    }

    @Test
    void deserialize() throws Exception {
        String content = json.write(dto).getJson();
        BookingDto parsed = json.parse(content).getObject();

        assertThat(parsed).usingRecursiveComparison().isEqualTo(dto);
    }
}
