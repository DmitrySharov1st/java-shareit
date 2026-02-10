package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private ItemServiceImpl itemService;

    private ItemDto itemDto;
    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@example.com")
                .build();

        itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();
    }

    @Test
    void createItem_ValidItem_ReturnsItemDto() {
        when(userService.getById(1L)).thenReturn(owner);

        ItemDto result = itemService.create(itemDto, 1L);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Дрель", result.getName());
        assertTrue(result.getAvailable());
    }

    @Test
    void createItem_InvalidUser_ThrowsNotFoundException() {
        when(userService.getById(999L)).thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class, () -> itemService.create(itemDto, 999L));
    }

    @Test
    void updateItem_ValidUpdate_ReturnsUpdatedItemDto() {
        when(userService.getById(1L)).thenReturn(owner);
        ItemDto createdItem = itemService.create(itemDto, 1L);

        ItemDto updateDto = ItemDto.builder()
                .name("Обновленная дрель")
                .available(false)
                .build();

        ItemDto result = itemService.update(createdItem.getId(), updateDto, 1L);

        assertNotNull(result);
        assertEquals("Обновленная дрель", result.getName());
        assertFalse(result.getAvailable());
    }

    @Test
    void updateItem_NotOwner_ThrowsNotFoundException() {
        when(userService.getById(1L)).thenReturn(owner);
        ItemDto createdItem = itemService.create(itemDto, 1L);

        // Заглушка для userService.getById(2L) не нужна, так как метод update не вызывает userService.getById(2L)
        ItemDto updateDto = ItemDto.builder()
                .name("Обновленная дрель")
                .build();

        assertThrows(NotFoundException.class, () ->
                itemService.update(createdItem.getId(), updateDto, 2L));
    }

    @Test
    void updateItem_NonExistentItem_ThrowsNotFoundException() {
        ItemDto updateDto = ItemDto.builder()
                .name("Обновленная дрель")
                .build();

        assertThrows(NotFoundException.class, () ->
                itemService.update(999L, updateDto, 1L));
    }

    @Test
    void getItemById_ValidId_ReturnsItemDto() {
        when(userService.getById(1L)).thenReturn(owner);
        ItemDto createdItem = itemService.create(itemDto, 1L);

        ItemDto result = itemService.getById(createdItem.getId());

        assertNotNull(result);
        assertEquals(createdItem.getId(), result.getId());
        assertEquals("Дрель", result.getName());
    }

    @Test
    void getItemById_NonExistentId_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> itemService.getById(999L));
    }

    @Test
    void getAllItemsByOwner_ReturnsItemList() {
        when(userService.getById(1L)).thenReturn(owner);
        itemService.create(itemDto, 1L);

        List<ItemDto> result = itemService.getAllByOwner(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Дрель", result.get(0).getName());
    }

    @Test
    void getAllItemsByOwner_NoItems_ReturnsEmptyList() {
        List<ItemDto> result = itemService.getAllByOwner(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchItems_ValidText_ReturnsAvailableItems() {
        when(userService.getById(1L)).thenReturn(owner);
        itemService.create(itemDto, 1L);

        List<ItemDto> result = itemService.search("дрель");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Дрель", result.get(0).getName());
        assertTrue(result.get(0).getAvailable());
    }

    @Test
    void searchItems_EmptyText_ReturnsEmptyList() {
        List<ItemDto> result = itemService.search("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchItems_NoMatchingItems_ReturnsEmptyList() {
        when(userService.getById(1L)).thenReturn(owner);
        itemService.create(itemDto, 1L);

        List<ItemDto> result = itemService.search("несуществующий");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchItems_OnlyAvailableItems_ReturnsFilteredList() {
        when(userService.getById(1L)).thenReturn(owner);

        // Создаем доступную вещь
        itemService.create(itemDto, 1L);

        // Создаем недоступную вещь
        ItemDto unavailableItem = ItemDto.builder()
                .name("Сломанная дрель")
                .description("Не работает")
                .available(false)
                .build();
        itemService.create(unavailableItem, 1L);

        List<ItemDto> result = itemService.search("дрель");

        assertNotNull(result);
        assertEquals(1, result.size()); // Только доступная вещь
        assertEquals("Дрель", result.get(0).getName());
        assertTrue(result.get(0).getAvailable());
    }

    @Test
    void searchItems_CaseInsensitiveSearch_ReturnsItems() {
        when(userService.getById(1L)).thenReturn(owner);
        itemService.create(itemDto, 1L);

        List<ItemDto> result1 = itemService.search("ДРЕЛЬ"); // В верхнем регистре
        List<ItemDto> result2 = itemService.search("дрель"); // В нижнем регистре
        List<ItemDto> result3 = itemService.search("Дрель"); // Смешанный регистр

        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
        assertEquals(1, result3.size());
    }

    @Test
    void updateItem_PartialUpdate_ReturnsPartiallyUpdatedItem() {
        when(userService.getById(1L)).thenReturn(owner);
        ItemDto createdItem = itemService.create(itemDto, 1L);

        // Обновляем только название
        ItemDto updateDto = ItemDto.builder()
                .name("Новое название")
                .build();

        ItemDto result = itemService.update(createdItem.getId(), updateDto, 1L);

        assertEquals("Новое название", result.getName());
        assertEquals("Мощная дрель", result.getDescription()); // Осталось прежним
        assertTrue(result.getAvailable()); // Осталось прежним
    }
}