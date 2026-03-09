package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private ItemRequest request1;
    private ItemRequest request2;
    private ItemRequest request3;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(User.builder()
                .name("User 1")
                .email("user1@test.com")
                .build());

        user2 = userRepository.save(User.builder()
                .name("User 2")
                .email("user2@test.com")
                .build());

        request1 = requestRepository.save(ItemRequest.builder()
                .description("Request 1")
                .requestor(user1)
                .created(LocalDateTime.now().minusDays(2))
                .build());

        request2 = requestRepository.save(ItemRequest.builder()
                .description("Request 2")
                .requestor(user1)
                .created(LocalDateTime.now().minusDays(1))
                .build());

        request3 = requestRepository.save(ItemRequest.builder()
                .description("Request 3")
                .requestor(user2)
                .created(LocalDateTime.now())
                .build());
    }

    @Test
    void findByRequestorId_ShouldReturnRequestsOrderedByCreatedDesc() {
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        List<ItemRequest> result = requestRepository.findByRequestorId(user1.getId(), sort);

        assertEquals(2, result.size());
        assertEquals(request2.getId(), result.get(0).getId()); // более новый (created ближе к now)
        assertEquals(request1.getId(), result.get(1).getId());
    }

    @Test
    void findByRequestorIdNot_ShouldReturnRequestsOfOtherUsers() {
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        List<ItemRequest> result = requestRepository.findByRequestorIdNot(user1.getId(), sort);

        assertEquals(1, result.size());
        assertEquals(request3.getId(), result.get(0).getId());
    }
}