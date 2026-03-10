package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestMapper mapper;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private User requestor;
    private User anotherUser;
    private ItemRequest request;
    private ItemRequestCreateDto createDto;
    private ItemRequestDto responseDto;
    private Item item;

    @BeforeEach
    void setUp() {
        requestor = User.builder()
                .id(1L)
                .name("Requestor")
                .email("req@test.com")
                .build();

        anotherUser = User.builder()
                .id(2L)
                .name("Another")
                .email("another@test.com")
                .build();

        request = ItemRequest.builder()
                .id(10L)
                .description("Need a tool")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        createDto = new ItemRequestCreateDto();
        createDto.setDescription("Need a tool");

        item = Item.builder()
                .id(100L)
                .name("Drill")
                .description("Powerful drill")
                .owner(anotherUser)
                .requestId(10L)
                .build();

        responseDto = ItemRequestDto.builder()
                .id(10L)
                .description("Need a tool")
                .created(request.getCreated())
                .items(Collections.emptyList())
                .build();
    }

    @Test
    void createValidRequestReturnsDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(mapper.toItemRequest(createDto, requestor)).thenReturn(request);
        when(requestRepository.save(request)).thenReturn(request);
        when(mapper.toItemRequestDto(request, Collections.emptyList())).thenReturn(responseDto);

        ItemRequestDto result = requestService.create(createDto, 1L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Need a tool", result.getDescription());
        verify(requestRepository).save(request);
    }

    @Test
    void createThrowsNotFoundWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.create(createDto, 99L));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void getUserRequestsReturnsListWhenUserExists() {
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findByRequestorId(1L, sort)).thenReturn(List.of(request));
        when(itemRepository.findByRequestIdIn(List.of(10L))).thenReturn(List.of(item));
        when(mapper.toItemRequestDto(request, List.of(item))).thenReturn(responseDto);

        List<ItemRequestDto> result = requestService.getUserRequests(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    void getUserRequestsThrowsNotFoundWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.getUserRequests(99L));
    }

    @Test
    void getAllRequestsReturnsOtherUsersRequests() {
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findByRequestorIdNot(1L, sort)).thenReturn(List.of(request));
        when(itemRepository.findByRequestIdIn(List.of(10L))).thenReturn(List.of(item));
        when(mapper.toItemRequestDto(request, List.of(item))).thenReturn(responseDto);

        List<ItemRequestDto> result = requestService.getAllRequests(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getRequestByIdReturnsDtoWhenRequestExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(10L)).thenReturn(List.of(item));
        when(mapper.toItemRequestDto(request, List.of(item))).thenReturn(responseDto);

        ItemRequestDto result = requestService.getRequestById(10L, 1L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    void getRequestByIdThrowsNotFoundWhenRequestNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.getRequestById(99L, 1L));
    }
}