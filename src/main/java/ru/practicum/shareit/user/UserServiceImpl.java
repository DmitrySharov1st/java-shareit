package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Map<Long, User> users = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public User create(User user) {
        // Проверка уникальности email
        checkEmailUniqueness(user.getEmail(), null);

        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        Long userId = user.getId();
        if (!users.containsKey(userId)) {
            throw new NotFoundException(String.format("User with id %s not found", userId));
        }

        User existingUser = users.get(userId);

        // Если email изменяется, проверяем уникальность
        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            checkEmailUniqueness(user.getEmail(), userId);
        }

        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }

        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }

        users.put(userId, existingUser);
        return existingUser;
    }

    @Override
    public User getById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException(String.format("User with id %s not found", id));
        }
        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException(String.format("User with id %s not found", id));
        }
        users.remove(id);
    }

    private void checkEmailUniqueness(String email, Long excludeUserId) {
        for (User user : users.values()) {
            // Если нашли пользователя с таким email, и это не тот же самый пользователь (при обновлении)
            if (user.getEmail().equals(email) && !user.getId().equals(excludeUserId)) {
                throw new ConflictException(String.format("Email %s is already in use", email));
            }
        }
    }
}