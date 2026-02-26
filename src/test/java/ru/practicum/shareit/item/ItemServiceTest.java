package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemDto itemDto;
    private Comment comment;
    private CommentDto commentDto;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@test.com")
                .build();
        booker = User.builder()
                .id(2L)
                .name("Booker")
                .email("booker@test.com")
                .build();
        item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .owner(owner)
                .build();
        itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();
        comment = Comment.builder()
                .id(1L)
                .text("Great item!")
                .item(item)
                .author(booker)
                .created(LocalDateTime.now())
                .build();
        commentDto = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName(booker.getName())
                .created(comment.getCreated())
                .build();
        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
    }

    @Test
    void createItemShouldReturnItemDtoWhenValidItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.create(itemDto, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Дрель", result.getName());
        assertTrue(result.getAvailable());
    }

    @Test
    void createItemShouldThrowNotFoundExceptionWhenInvalidUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.create(itemDto, 999L));
    }

    @Test
    void updateItemShouldReturnUpdatedItemDtoWhenValidUpdate() {
        ItemDto updateDto = ItemDto.builder()
                .name("Обновленная дрель")
                .available(false)
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        // save не вызывается отдельно, изменения происходят через управление JPA, поэтому можно не мокать save

        ItemDto result = itemService.update(1L, updateDto, 1L);

        assertNotNull(result);
        assertEquals("Обновленная дрель", result.getName());
        assertFalse(result.getAvailable());
        assertEquals("Мощная дрель", result.getDescription()); // не менялось
    }

    @Test
    void updateItemShouldThrowNotFoundExceptionWhenNotOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemDto updateDto = ItemDto.builder().name("New").build();

        assertThrows(NotFoundException.class, () -> itemService.update(1L, updateDto, 2L));
    }

    @Test
    void updateItemShouldThrowNotFoundExceptionWhenNonExistentItem() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.update(999L, itemDto, 1L));
    }

    @Test
    void getItemByIdShouldReturnItemResponseDtoWithComments() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(1L)).thenReturn(List.of(comment));

        ItemResponseDto result = itemService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1, result.getComments().size());
        assertEquals("Great item!", result.getComments().get(0).getText());
    }

    @Test
    void getItemByIdShouldThrowNotFoundExceptionWhenNonExistentId() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getById(999L));
    }

    @Test
    void getAllItemsByOwnerShouldReturnItemOwnerDtoList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(1L)).thenReturn(List.of(item));
        when(bookingRepository.findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.empty());

        List<ItemOwnerDto> result = itemService.getAllByOwner(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        ItemOwnerDto dto = result.get(0);
        assertNotNull(dto.getLastBooking());
        assertNull(dto.getNextBooking());
        assertEquals(booking.getId(), dto.getLastBooking().getId());
    }

    @Test
    void getAllItemsByOwnerShouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getAllByOwner(999L));
    }

    @Test
    void searchItemsShouldReturnAvailableItemsWhenValidText() {
        when(itemRepository.search("дрель")).thenReturn(List.of(item));

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
        verify(itemRepository, never()).search(anyString());
    }

    @Test
    void searchItemsShouldReturnEmptyListWhenNoMatchingItems() {
        when(itemRepository.search("xyz")).thenReturn(List.of());

        List<ItemDto> result = itemService.search("xyz");

        assertTrue(result.isEmpty());
    }

    @Test
    void addCommentShouldReturnCommentDtoWhenValid() {
        CommentCreateDto createDto = new CommentCreateDto("Great item!");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        // Используем any(LocalDateTime.class) для гибкости
        when(bookingRepository.existsByItemIdAndBookerIdAndEndBeforeAndStatus(
                anyLong(), anyLong(), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = itemService.addComment(1L, 2L, createDto);

        assertNotNull(result);
        assertEquals("Great item!", result.getText());
        assertEquals(booker.getName(), result.getAuthorName());
    }

    @Test
    void addCommentShouldThrowValidationExceptionWhenUserNeverBooked() {
        CommentCreateDto createDto = new CommentCreateDto("Bad item?");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        // Также используем any(LocalDateTime.class)
        when(bookingRepository.existsByItemIdAndBookerIdAndEndBeforeAndStatus(
                anyLong(), anyLong(), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(false);

        assertThrows(ValidationException.class, () -> itemService.addComment(1L, 2L, createDto));
    }

    @Test
    void getItemByIdShouldReturnItemDetailedDtoWithCommentsAndNullBookingsForNonOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(1L)).thenReturn(List.of(comment));
        // Для невладельца (userId=2) не должны вызываться методы поиска бронирований
        ItemDetailedDto result = itemService.getById(1L, 2L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1, result.getComments().size());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        verify(bookingRepository, never()).findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(any(), any(), any());
    }

    @Test
    void getItemByIdShouldReturnItemDetailedDtoWithBookingsForOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(1L)).thenReturn(List.of(comment));
        when(bookingRepository.findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.empty());

        ItemDetailedDto result = itemService.getById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNotNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }
}