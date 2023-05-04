package de.uniks.pioneers.dto;

public record Event<T>(
        String event,
        T data) {
}
