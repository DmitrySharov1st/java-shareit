package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestMapperTest {

    private final ItemRequestMapper mapper = new ItemRequestMapper();

    @Test
    void toItemRequest_ShouldConvertCreateDtoToEntity() {
        User requestor = User.builder().id(1L).name("User").build();
        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("Need item");

        ItemRequest request = mapper.toItemRequest(createDto, requestor);

        assertNotNull(request);
        assertNull(request.getId());
        assertEquals("Need item", request.getDescription());
        assertEquals(requestor, request.getRequestor());
        assertNotNull(request.getCreated());
    }

    @Test
    void toItemRequestDto_ShouldConvertEntityToDto() {
        User requestor = User.builder().id(1L).name("User").build();
        ItemRequest request = ItemRequest.builder()
                .id(10L)
                .description("Need item")
                .requestor(requestor)
                .created(LocalDateTime.of(2023, 1, 1, 12, 0))
                .build();

        User owner = User.builder().id(2L).name("Owner").build();
        Item item = Item.builder()
                .id(100L)
                .name("Drill")
                .owner(owner)
                .build();

        ItemRequestDto dto = mapper.toItemRequestDto(request, List.of(item));

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Need item", dto.getDescription());
        assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0), dto.getCreated());
        assertNotNull(dto.getItems());
        assertEquals(1, dto.getItems().size());

        ItemForRequestDto itemDto = dto.getItems().get(0);
        assertEquals(100L, itemDto.getId());
        assertEquals("Drill", itemDto.getName());
        assertEquals(2L, itemDto.getOwnerId());
    }

    @Test
    void toItemRequestDto_ShouldHandleEmptyItems() {
        User requestor = User.builder().id(1L).name("User").build();
        ItemRequest request = ItemRequest.builder()
                .id(10L)
                .description("Need item")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        ItemRequestDto dto = mapper.toItemRequestDto(request, List.of());

        assertNotNull(dto);
        assertTrue(dto.getItems().isEmpty());
    }
}