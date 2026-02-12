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
    void createItemShouldReturnItemDtoWhenValidItem() {
        when(userService.getById(1L)).thenReturn(owner);

        ItemDto result = itemService.create(itemDto, 1L);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Дрель", result.getName());
        assertTrue(result.getAvailable());
    }

    @Test
    void createItemShouldThrowNotFoundExceptionWhenInvalidUser() {
        when(userService.getById(999L)).thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class, () -> itemService.create(itemDto, 999L));
    }

    @Test
    void updateItemShouldReturnUpdatedItemDtoWhenValidUpdate() {
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
    void updateItemShouldThrowNotFoundExceptionWhenNotOwner() {
        when(userService.getById(1L)).thenReturn(owner);
        ItemDto createdItem = itemService.create(itemDto, 1L);

        ItemDto updateDto = ItemDto.builder()
                .name("Обновленная дрель")
                .build();

        assertThrows(NotFoundException.class, () ->
                itemService.update(createdItem.getId(), updateDto, 2L));
    }

    @Test
    void updateItemShouldThrowNotFoundExceptionWhenNonExistentItem() {
        ItemDto updateDto = ItemDto.builder()
                .name("Обновленная дрель")
                .build();

        assertThrows(NotFoundException.class, () ->
                itemService.update(999L, updateDto, 1L));
    }

    @Test
    void getItemByIdShouldReturnItemDtoWhenValidId() {
        when(userService.getById(1L)).thenReturn(owner);
        ItemDto createdItem = itemService.create(itemDto, 1L);

        ItemDto result = itemService.getById(createdItem.getId());

        assertNotNull(result);
        assertEquals(createdItem.getId(), result.getId());
        assertEquals("Дрель", result.getName());
    }

    @Test
    void getItemByIdShouldThrowNotFoundExceptionWhenNonExistentId() {
        assertThrows(NotFoundException.class, () -> itemService.getById(999L));
    }

    @Test
    void getAllItemsByOwnerShouldReturnItemList() {
        when(userService.getById(1L)).thenReturn(owner);
        itemService.create(itemDto, 1L);

        List<ItemDto> result = itemService.getAllByOwner(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Дрель", result.get(0).getName());
    }

    @Test
    void getAllItemsByOwnerShouldReturnEmptyListWhenNoItems() {
        List<ItemDto> result = itemService.getAllByOwner(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchItemsShouldReturnAvailableItemsWhenValidText() {
        when(userService.getById(1L)).thenReturn(owner);
        itemService.create(itemDto, 1L);

        List<ItemDto> result = itemService.search("дрель");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Дрель", result.get(0).getName());
        assertTrue(result.get(0).getAvailable());
    }

    @Test
    void searchItemsShouldReturnEmptyListWhenEmptyText() {
        List<ItemDto> result = itemService.search("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchItemsShouldReturnEmptyListWhenNoMatchingItems() {
        when(userService.getById(1L)).thenReturn(owner);
        itemService.create(itemDto, 1L);

        List<ItemDto> result = itemService.search("несуществующий");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchItemsShouldReturnFilteredListWhenOnlyAvailableItems() {
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
    void searchItemsShouldReturnItemsWhenCaseInsensitiveSearch() {
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
    void updateItemShouldReturnPartiallyUpdatedItemWhenPartialUpdate() {
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