package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.GlobalExceptionHandler;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RequestController.class)
@Import(GlobalExceptionHandler.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestClient requestClient;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final Long USER_ID = 1L;

    @Test
    void create_ValidDescription_ShouldReturnOk() throws Exception {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("Need a tool");

        when(requestClient.create(any(ItemRequestCreateDto.class), eq(USER_ID)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk());
    }

    @Test
    void create_EmptyDescription_ShouldReturnBadRequest() throws Exception {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("");

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_MissingUserIdHeader_ShouldReturnBadRequest() throws Exception {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("Need a tool");

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value(org.hamcrest.Matchers.containsString("X-Sharer-User-Id")));
    }

    @Test
    void getUserRequests_ShouldReturnOk() throws Exception {
        when(requestClient.getUserRequests(USER_ID)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_ShouldReturnOk() throws Exception {
        when(requestClient.getAllRequests(USER_ID)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk());
    }

    @Test
    void getRequestById_ShouldReturnOk() throws Exception {
        when(requestClient.getRequestById(5L, USER_ID)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/5")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk());
    }
}