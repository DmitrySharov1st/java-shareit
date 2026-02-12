package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        User createdUser = userService.create(user);
        return UserMapper.toUserDto(createdUser);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId,
                          @RequestBody UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        user.setId(userId);
        User updatedUser = userService.update(user);
        return UserMapper.toUserDto(updatedUser);
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable Long userId) {
        User user = userService.getById(userId);
        return UserMapper.toUserDto(user);
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}