package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
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
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingRequestDto requestDto;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("Owner").email("owner@test.com").build();
        booker = User.builder().id(2L).name("Booker").email("booker@test.com").build();
        item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Desc")
                .available(true)
                .owner(owner)
                .build();
        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        requestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingDto = BookingDto.builder()
                .id(1L)
                .start(requestDto.getStart())
                .end(requestDto.getEnd())
                .build();
    }

    @Test
    void createBookingShouldReturnBookingDtoWhenValid() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingMapper.toBooking(requestDto, item, booker)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.create(requestDto, 2L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void createBookingShouldThrowValidationExceptionWhenDatesInvalid() {
        requestDto.setStart(LocalDateTime.now().plusDays(2));
        requestDto.setEnd(LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class, () -> bookingService.create(requestDto, 2L));
    }

    @Test
    void createBookingShouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(requestDto, 99L));
    }

    @Test
    void createBookingShouldThrowNotFoundExceptionWhenItemNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        requestDto.setItemId(99L);
        assertThrows(NotFoundException.class, () -> bookingService.create(requestDto, 2L));
    }

    @Test
    void createBookingShouldThrowValidationExceptionWhenItemNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.create(requestDto, 2L));
    }

    @Test
    void createBookingShouldThrowNotFoundExceptionWhenOwnerBookOwnItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.create(requestDto, 1L));
    }

    @Test
    void approveBookingShouldReturnApprovedBookingWhenValid() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.approve(1L, 1L, true);

        assertEquals(BookingStatus.APPROVED, booking.getStatus());
        assertNotNull(result);
    }

    @Test
    void approveBookingShouldThrowNotFoundExceptionWhenBookingNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.approve(99L, 1L, true));
    }

    @Test
    void approveBookingShouldThrowValidationExceptionWhenUserNotOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.approve(1L, 2L, true));
    }

    @Test
    void approveBookingShouldThrowValidationExceptionWhenNotWaiting() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.approve(1L, 1L, true));
    }

    @Test
    void getBookingByIdShouldReturnBookingDtoWhenUserIsBooker() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.getById(1L, 2L);

        assertNotNull(result);
    }

    @Test
    void getBookingByIdShouldReturnBookingDtoWhenUserIsOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.getById(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void getBookingByIdShouldThrowNotFoundExceptionWhenUserNotRelated() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getById(1L, 3L));
    }

    @Test
    void getUserBookingsShouldReturnListForAllState() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerId(eq(2L), any(Sort.class))).thenReturn(List.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getUserBookings(2L, BookingState.ALL);

        assertEquals(1, result.size());
    }

    // Аналогичные тесты для других состояний (CURRENT, PAST, FUTURE, WAITING, REJECTED) можно добавить по желанию.
}