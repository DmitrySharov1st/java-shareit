package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException(
                    String.format("Email %s is already in use", user.getEmail()));
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(User user) {
        Long userId = user.getId();
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %d not found", userId)));

        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new ConflictException(
                        String.format("Email %s is already in use", user.getEmail()));
            }
            existingUser.setEmail(user.getEmail());
        }

        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }

        return existingUser;
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %d not found", id)));
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(
                    String.format("User with id %d not found", id));
        }
        userRepository.deleteById(id);
    }
}