package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(Long itemId, ItemDto itemDto, Long userId);

    // теперь возвращает ItemResponseDto (с комментариями)
    ItemResponseDto getById(Long itemId);

    // теперь возвращает список ItemOwnerDto (с датами бронирований)
    List<ItemOwnerDto> getAllByOwner(Long ownerId);

    List<ItemDto> search(String text);

    // новый метод для добавления комментария
    CommentDto addComment(Long itemId, Long userId, CommentCreateDto commentDto);

    ItemDetailedDto getById(Long itemId, Long userId);
}