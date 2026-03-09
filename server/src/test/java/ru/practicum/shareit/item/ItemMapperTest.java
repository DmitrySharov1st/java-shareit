package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    void shouldConvertItemToItemResponseDtoWithComments() {
        User owner = User.builder().id(1L).name("Owner").email("owner@test.com").build();
        Item item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .owner(owner)
                .build();

        List<CommentDto> comments = List.of(
                CommentDto.builder().id(1L).text("Great!").authorName("User").created(LocalDateTime.now()).build()
        );

        ItemResponseDto responseDto = ItemMapper.toItemResponseDto(item, comments);

        assertNotNull(responseDto);
        assertEquals(1L, responseDto.getId());
        assertEquals("Дрель", responseDto.getName());
        assertEquals(1, responseDto.getComments().size());
        assertEquals("Great!", responseDto.getComments().get(0).getText());
    }

    @Test
    void shouldConvertItemToItemOwnerDtoWithBookings() {
        User owner = User.builder().id(1L).name("Owner").email("owner@test.com").build();
        Item item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .owner(owner)
                .build();

        BookingShortDto lastBooking = BookingShortDto.builder()
                .id(10L)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(1))
                .bookerId(2L)
                .build();

        BookingShortDto nextBooking = BookingShortDto.builder()
                .id(11L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .bookerId(3L)
                .build();

        ItemOwnerDto ownerDto = ItemMapper.toItemOwnerDto(item, lastBooking, nextBooking);

        assertNotNull(ownerDto);
        assertEquals(1L, ownerDto.getId());
        assertNotNull(ownerDto.getLastBooking());
        assertNotNull(ownerDto.getNextBooking());
        assertEquals(10L, ownerDto.getLastBooking().getId());
        assertEquals(11L, ownerDto.getNextBooking().getId());
    }
}