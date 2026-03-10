package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BookingClientTest {

    private BookingClient bookingClient;
    private MockRestServiceServer mockServer;
    private RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        objectMapper.registerModule(new JavaTimeModule());

        bookingClient = new BookingClient("http://localhost:9090", new RestTemplateBuilder());

        // Получаем RestTemplate из клиента
        Field restField = bookingClient.getClass().getSuperclass().getDeclaredField("rest");
        restField.setAccessible(true);
        restTemplate = (RestTemplate) restField.get(bookingClient);

        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void bookItemSendsPostRequest() throws Exception {
        BookItemRequestDto requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        mockServer.expect(requestTo("http://localhost:9090/bookings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(requestDto)))
                .andRespond(withSuccess());

        bookingClient.bookItem(1L, requestDto);

        mockServer.verify();
    }

    @Test
    void getBookingsSendsGetRequestWithParams() {
        mockServer.expect(requestTo("http://localhost:9090/bookings?state=ALL&from=0&size=10"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess());

        bookingClient.getBookings(1L, "ALL", 0, 10);

        mockServer.verify();
    }

    @Test
    void getBookingSendsGetRequest() {
        mockServer.expect(requestTo("http://localhost:9090/bookings/5"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess());

        bookingClient.getBooking(1L, 5L);

        mockServer.verify();
    }

    @Test
    void approveBookingSendsPatchRequest() {
        mockServer.expect(requestTo("http://localhost:9090/bookings/5?approved=true"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess());

        bookingClient.approveBooking(1L, 5L, true);

        mockServer.verify();
    }

    @Test
    void getOwnerBookingsSendsGetRequest() {
        mockServer.expect(requestTo("http://localhost:9090/bookings/owner?state=ALL&from=0&size=10"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess());

        bookingClient.getOwnerBookings(1L, "ALL", 0, 10);

        mockServer.verify();
    }
}