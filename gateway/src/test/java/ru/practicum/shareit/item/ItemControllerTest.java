package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@Import(GlobalExceptionHandler.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final Long USER_ID = 1L;

    @Test
    void createValidItemReturnsOk() throws Exception {
        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setName("Drill");
        createDto.setDescription("Powerful");
        createDto.setAvailable(true);
        createDto.setRequestId(null);

        when(itemClient.create(any(ItemCreateDto.class), eq(USER_ID)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk());
    }

    @Test
    void createInvalidNameReturnsBadRequest() throws Exception {
        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setName("");
        createDto.setDescription("Powerful");
        createDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInvalidDescriptionReturnsBadRequest() throws Exception {
        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setName("Drill");
        createDto.setDescription("");
        createDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createNullAvailableReturnsBadRequest() throws Exception {
        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setName("Drill");
        createDto.setDescription("Powerful");
        createDto.setAvailable(null);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMissingUserIdHeaderReturnsBadRequest() throws Exception {
        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setName("Drill");
        createDto.setDescription("Powerful");
        createDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("X-Sharer-User-Id")));
    }

    @Test
    void updateValidDataReturnsOk() throws Exception {
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Updated");

        when(itemClient.update(eq(1L), any(ItemUpdateDto.class), eq(USER_ID)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getByIdReturnsOk() throws Exception {
        when(itemClient.getById(1L, USER_ID)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/1")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByOwnerReturnsOk() throws Exception {
        when(itemClient.getAllByOwner(USER_ID)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isOk());
    }

    @Test
    void searchReturnsOk() throws Exception {
        when(itemClient.search("drill")).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk());
    }

    @Test
    void addCommentValidTextReturnsOk() throws Exception {
        CommentCreateDto commentDto = new CommentCreateDto();
        commentDto.setText("Great!");

        when(itemClient.addComment(eq(1L), eq(USER_ID), any(CommentCreateDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());
    }

    @Test
    void addCommentEmptyTextReturnsBadRequest() throws Exception {
        CommentCreateDto commentDto = new CommentCreateDto();
        commentDto.setText("");

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }
}