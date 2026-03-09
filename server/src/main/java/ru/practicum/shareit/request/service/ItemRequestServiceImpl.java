package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper mapper;

    private static final Sort SORT_BY_CREATED_DESC = Sort.by(Sort.Direction.DESC, "created");

    @Override
    @Transactional
    public ItemRequestDto create(ItemRequestCreateDto dto, Long requestorId) {
        User requestor = userRepository.findById(requestorId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        ItemRequest request = mapper.toItemRequest(dto, requestor);
        request = requestRepository.save(request);
        return mapper.toItemRequestDto(request, List.of());
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<ItemRequest> requests = requestRepository.findByRequestorId(userId, SORT_BY_CREATED_DESC);
        return enrichRequestsWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<ItemRequest> requests = requestRepository.findByRequestorIdNot(userId, SORT_BY_CREATED_DESC);
        return enrichRequestsWithItems(requests);
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));
        List<Item> items = itemRepository.findByRequestId(requestId);
        return mapper.toItemRequestDto(request, items);
    }

    private List<ItemRequestDto> enrichRequestsWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        List<Item> items = itemRepository.findByRequestIdIn(requestIds);
        Map<Long, List<Item>> itemsByRequest = items.stream()
                .collect(Collectors.groupingBy(Item::getRequestId));
        return requests.stream()
                .map(req -> mapper.toItemRequestDto(req, itemsByRequest.getOrDefault(req.getId(), List.of())))
                .collect(Collectors.toList());
    }
}