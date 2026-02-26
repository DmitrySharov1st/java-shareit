package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %d not found", userId)));
        Item item = ItemMapper.toItem(itemDto, owner);
        item = itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public ItemDto update(Long itemId, ItemDto itemDto, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Item with id %d not found", itemId)));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("User is not the owner of the item");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemResponseDto getById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Item with id %d not found", itemId)));

        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        return ItemMapper.toItemResponseDto(item, comments);
    }

    @Override
    public List<ItemOwnerDto> getAllByOwner(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %d not found", ownerId)));

        List<Item> items = itemRepository.findByOwnerId(ownerId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    BookingShortDto lastBooking = bookingRepository
                            .findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                                    item.getId(), now, BookingStatus.APPROVED)
                            .map(this::toBookingShort)
                            .orElse(null);
                    BookingShortDto nextBooking = bookingRepository
                            .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                                    item.getId(), now, BookingStatus.APPROVED)
                            .map(this::toBookingShort)
                            .orElse(null);
                    return ItemMapper.toItemOwnerDto(item, lastBooking, nextBooking);
                })
                .collect(Collectors.toList());
    }

    private BookingShortDto toBookingShort(Booking booking) {
        return BookingShortDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CommentCreateDto commentDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Item with id %d not found", itemId)));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %d not found", userId)));

        boolean hasBooked = bookingRepository.existsByItemIdAndBookerIdAndEndBeforeAndStatus(
                itemId, userId, LocalDateTime.now(), BookingStatus.APPROVED);
        if (!hasBooked) {
            throw new ValidationException("User has not rented this item or the rental is not finished");
        }

        Comment comment = CommentMapper.toComment(itemId, author, commentDto.getText());
        comment.setItem(item);
        comment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public ItemDetailedDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Item with id %d not found", itemId)));

        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;
        LocalDateTime now = LocalDateTime.now();

        if (userId != null && userId.equals(item.getOwner().getId())) {
            lastBooking = bookingRepository
                    .findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                            itemId, now, BookingStatus.APPROVED)
                    .map(this::toBookingShort)
                    .orElse(null);
            nextBooking = bookingRepository
                    .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                            itemId, now, BookingStatus.APPROVED)
                    .map(this::toBookingShort)
                    .orElse(null);
        }

        return ItemMapper.toItemDetailedDto(item, comments, lastBooking, nextBooking);
    }
}