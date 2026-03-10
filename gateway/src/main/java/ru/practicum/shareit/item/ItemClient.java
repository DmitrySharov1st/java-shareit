package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .build()
        );
    }

    public ResponseEntity<Object> create(ItemCreateDto itemDto, Long userId) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> update(Long itemId, ItemUpdateDto itemDto, Long userId) {
        return patch(String.format("/%d", itemId), userId, itemDto);
    }

    public ResponseEntity<Object> getById(Long itemId, Long userId) {
        return get(String.format("/%d", itemId), userId);
    }

    public ResponseEntity<Object> getAllByOwner(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> search(String text) {
        Map<String, Object> parameters = Map.of("text", text);
        return get("/search?text={text}", null, parameters);
    }

    public ResponseEntity<Object> addComment(Long itemId, Long userId, CommentCreateDto commentDto) {
        return post(String.format("/%d/comment", itemId), userId, commentDto);
    }
}