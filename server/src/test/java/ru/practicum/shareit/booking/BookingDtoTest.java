package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    private BookingDto.BookerInfo booker;
    private BookingDto.ItemInfo itemInfo;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingDto dto;

    @BeforeEach
    void setup() {
        booker = new BookingDto.BookerInfo(42L);
        itemInfo = new BookingDto.ItemInfo(7L, "Book");
        start = LocalDateTime.of(2025, 5, 30, 9, 0, 0);
        end   = LocalDateTime.of(2025, 5, 31, 9, 0, 0);
        dto = new BookingDto(
                10L,
                start,
                end,
                BookingStatus.WAITING,
                booker,
                itemInfo
        );
    }

    @Test
    void serializeBookingDto() throws IOException {
        JsonContent<BookingDto> content = json.write(dto);

        assertThat(content).isNotNull();
        assertThat(content)
                .hasJsonPathNumberValue("$.id")
                .extractingJsonPathNumberValue("$.id").isEqualTo(10);
        assertThat(content)
                .hasJsonPathStringValue("$.status")
                .extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(content)
                .hasJsonPathStringValue("$.start")
                .extractingJsonPathStringValue("$.start")
                .startsWith("2025-05-30T09:00:00");
        assertThat(content)
                .hasJsonPathStringValue("$.end")
                .extractingJsonPathStringValue("$.end")
                .startsWith("2025-05-31T09:00:00");
        assertThat(content)
                .hasJsonPathNumberValue("$.booker.id")
                .extractingJsonPathNumberValue("$.booker.id").isEqualTo(42);
        assertThat(content)
                .hasJsonPathNumberValue("$.item.id")
                .extractingJsonPathNumberValue("$.item.id").isEqualTo(7);
        assertThat(content)
                .hasJsonPathStringValue("$.item.name")
                .extractingJsonPathStringValue("$.item.name")
                .isEqualTo("Book");
    }

    @Test
    void deserializeBookingDto() throws IOException {
        String input = "{\n" +
                "  \"id\": 99,\n" +
                "  \"start\": \"2025-06-01T08:30:00\",\n" +
                "  \"end\":   \"2025-06-02T08:30:00\",\n" +
                "  \"status\": \"APPROVED\",\n" +
                "  \"booker\": { \"id\": 21 },\n" +
                "  \"item\":   { \"id\": 5, \"name\": \"Book\" }\n" +
                "}";

        BookingDto result = json.parseObject(input);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.parse("2025-06-01T08:30:00"));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.parse("2025-06-02T08:30:00"));
        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
        // bookerInfo
        assertThat(result.getBooker()).isNotNull();
        assertThat(result.getBooker().getId()).isEqualTo(21L);
        // itemInfo
        assertThat(result.getItem()).isNotNull();
        assertThat(result.getItem().getId()).isEqualTo(5L);
        assertThat(result.getItem().getName()).isEqualTo("Book");
    }
}
