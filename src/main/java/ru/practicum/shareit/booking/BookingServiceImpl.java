package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public BookingDto create(BookingRequestDto requestDto, Long bookerId) {
        // Проверка дат
        if (requestDto.getStart().isAfter(requestDto.getEnd()) ||
                requestDto.getStart().equals(requestDto.getEnd())) {
            throw new ValidationException("Start date must be before end date");
        }

        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %d not found", bookerId)));

        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow(() -> new NotFoundException(
                        String.format("Item with id %d not found", requestDto.getItemId())));

        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available for booking");
        }

        if (item.getOwner().getId().equals(bookerId)) {
            throw new NotFoundException("Owner cannot book own item");
        }

        Booking booking = bookingMapper.toBooking(requestDto, item, booker);
        booking = bookingRepository.save(booking);
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional
    public BookingDto approve(Long bookingId, Long userId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Booking with id %d not found", bookingId)));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Only owner can approve/reject booking");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking is not in WAITING state");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Booking with id %d not found", bookingId)));

        // Доступ: автор бронирования или владелец вещи
        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Access denied");
        }

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %d not found", userId)));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerId(userId, SORT_BY_START_DESC);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(
                        userId, now, now, SORT_BY_START_DESC);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBefore(userId, now, SORT_BY_START_DESC);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfter(userId, now, SORT_BY_START_DESC);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatus(
                        userId, BookingStatus.WAITING, SORT_BY_START_DESC);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatus(
                        userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
                break;
            default:
                throw new IllegalArgumentException("Unsupported state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, BookingState state) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %d not found", ownerId)));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByOwnerId(ownerId, SORT_BY_START_DESC);
                break;
            case CURRENT:
                bookings = bookingRepository.findByOwnerIdCurrent(ownerId, now, SORT_BY_START_DESC);
                break;
            case PAST:
                bookings = bookingRepository.findByOwnerIdPast(ownerId, now, SORT_BY_START_DESC);
                break;
            case FUTURE:
                bookings = bookingRepository.findByOwnerIdFuture(ownerId, now, SORT_BY_START_DESC);
                break;
            case WAITING:
                bookings = bookingRepository.findByOwnerIdAndStatus(
                        ownerId, BookingStatus.WAITING, SORT_BY_START_DESC);
                break;
            case REJECTED:
                bookings = bookingRepository.findByOwnerIdAndStatus(
                        ownerId, BookingStatus.REJECTED, SORT_BY_START_DESC);
                break;
            default:
                throw new IllegalArgumentException("Unsupported state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}