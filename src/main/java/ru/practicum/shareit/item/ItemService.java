package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(Long itemId, ItemDto itemDto, Long userId);

    ItemDto getById(Long itemId);

    List<ItemDto> getAllByOwner(Long ownerId);
    
    List<ItemDto> search(String text);
}