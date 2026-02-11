package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final Long VALID_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель для бетона")
                .available(true)
                .build();
    }

    @Test
    void shouldCreateItemWhenValidItem() throws Exception {
        when(itemService.create(any(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description").value("Мощная дрель для бетона"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void shouldReturnBadRequestWhenUserIdHeaderMissing() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required header: " + USER_ID_HEADER));
    }

    @Test
    void shouldReturnBadRequestWhenItemNameEmpty() throws Exception {
        itemDto.setName("");

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateItemWhenValidUpdate() throws Exception {
        ItemDto updatedItem = ItemDto.builder()
                .id(1L)
                .name("Дрель обновленная")
                .description("Обновленное описание")
                .available(false)
                .build();

        when(itemService.update(anyLong(), any(), anyLong())).thenReturn(updatedItem);

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Дрель обновленная"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void shouldReturnItemWhenValidId() throws Exception {
        when(itemService.getById(1L)).thenReturn(itemDto);

        mockMvc.perform(get("/items/{itemId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void shouldReturnNotFoundWhenNonExistentId() throws Exception {
        when(itemService.getById(999L)).thenThrow(new NotFoundException("Item not found"));

        mockMvc.perform(get("/items/{itemId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnItemListWhenOwnerHasItems() throws Exception {
        when(itemService.getAllByOwner(anyLong())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, VALID_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void shouldReturnItemsWhenValidSearchText() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void shouldReturnEmptyListWhenEmptySearchText() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}