package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.exception.GlobalExceptionHandler;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final Long USER_ID = 1L;

    @Test
    void bookItem_ValidRequest_ShouldReturnOk() throws Exception {
        BookItemRequestDto requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        when(bookingClient.bookItem(eq(USER_ID), any(BookItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void bookItem_InvalidDates_ShouldReturnBadRequest() throws Exception {
        BookItemRequestDto requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().minusDays(1)); // start in past
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookItem_MissingStart_ShouldReturnBadRequest() throws Exception {
        BookItemRequestDto requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookItem_MissingUserIdHeader_ShouldReturnBadRequest() throws Exception {
        BookItemRequestDto requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString(USER_ID_HEADER)));
    }

    @Test
    void getBookings_DefaultParams_ShouldReturnOk() throws Exception {
        when(bookingClient.getBookings(eq(USER_ID), eq("ALL"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk());
    }

    @Test
    void getBooking_ShouldReturnOk() throws Exception {
        when(bookingClient.getBooking(eq(USER_ID), eq(5L)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/5")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk());
    }

    @Test
    void approveBooking_ShouldReturnOk() throws Exception {
        when(bookingClient.approveBooking(eq(USER_ID), eq(5L), eq(true)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/5")
                        .header(USER_ID_HEADER, USER_ID)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_ShouldReturnOk() throws Exception {
        when(bookingClient.getOwnerBookings(eq(USER_ID), eq("ALL"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk());
    }
}