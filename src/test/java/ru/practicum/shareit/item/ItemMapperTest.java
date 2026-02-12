package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    @Test
    void shouldConvertItemToItemDto() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@example.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .owner(owner)
                .requestId(100L)
                .build();

        ItemDto itemDto = ItemMapper.toItemDto(item);

        assertNotNull(itemDto);
        assertEquals(1L, itemDto.getId());
        assertEquals("Дрель", itemDto.getName());
        assertEquals("Мощная дрель", itemDto.getDescription());
        assertTrue(itemDto.getAvailable());
        assertEquals(100L, itemDto.getRequestId());
    }

    @Test
    void shouldConvertItemDtoToItem() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@example.com")
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .requestId(100L)
                .build();

        Item item = ItemMapper.toItem(itemDto, owner);

        assertNotNull(item);
        assertEquals(1L, item.getId());
        assertEquals("Дрель", item.getName());
        assertEquals("Мощная дрель", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(owner, item.getOwner());
        assertEquals(100L, item.getRequestId());
    }
}