package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final Long USER_ID = 1L;

    @Test
    void createValidRequestReturnsDto() throws Exception {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("Need a tool");

        ItemRequestDto responseDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a tool")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();

        when(requestService.create(any(ItemRequestCreateDto.class), eq(USER_ID)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a tool"));
    }

    @Test
    void getUserRequestsReturnsList() throws Exception {
        ItemRequestDto responseDto = ItemRequestDto.builder()
                .id(1L)
                .description("Request")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();

        when(requestService.getUserRequests(USER_ID)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getAllRequestsReturnsList() throws Exception {
        ItemRequestDto responseDto = ItemRequestDto.builder()
                .id(2L)
                .description("Request from another")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();

        when(requestService.getAllRequests(USER_ID)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L));
    }

    @Test
    void getRequestByIdReturnsDto() throws Exception {
        ItemRequestDto responseDto = ItemRequestDto.builder()
                .id(5L)
                .description("Specific request")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();

        when(requestService.getRequestById(5L, USER_ID)).thenReturn(responseDto);

        mockMvc.perform(get("/requests/5")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L));
    }

    @Test
    void createWithNullDescriptionPasses() throws Exception {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto(); // description = null

        when(requestService.create(any(ItemRequestCreateDto.class), eq(USER_ID)))
                .thenThrow(new RuntimeException("Service called with null description"));

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isInternalServerError());
    }
}