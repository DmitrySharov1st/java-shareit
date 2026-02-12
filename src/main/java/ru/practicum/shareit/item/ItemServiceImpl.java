package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final Map<Long, Item> items = new HashMap<>();
    private Long nextId = 1L;
    private final UserService userService;

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        User owner = userService.getById(userId);
        Item item = ItemMapper.toItem(itemDto, owner);
        item.setId(nextId++);
        items.put(item.getId(), item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(Long itemId, ItemDto itemDto, Long userId) {
        Item existingItem = items.get(itemId);
        if (existingItem == null) {
            throw new NotFoundException(String.format("Item with id %s not found", itemId));
        }

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("User is not the owner of the item");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        items.put(itemId, existingItem);
        return ItemMapper.toItemDto(existingItem);
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException(String.format("Item with id %s not found", itemId));
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        String searchText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> item.getName().toLowerCase().contains(searchText) ||
                        item.getDescription().toLowerCase().contains(searchText))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}