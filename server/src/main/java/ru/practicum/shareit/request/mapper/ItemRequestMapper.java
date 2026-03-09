package ru.practicum.shareit.request.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemRequestMapper {

    public ItemRequest toItemRequest(ItemRequestCreateDto dto, User requestor) {
        return ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
    }

    public ItemRequestDto toItemRequestDto(ItemRequest request, List<Item> items) {
        List<ItemForRequestDto> itemDtos = items.stream()
                .map(item -> ItemForRequestDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .ownerId(item.getOwner().getId())
                        .build())
                .collect(Collectors.toList());

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemDtos)
                .build();
    }
}