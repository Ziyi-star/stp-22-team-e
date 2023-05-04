package de.uniks.pioneers.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.Constants;
import retrofit2.HttpException;

import java.util.Objects;

public class ErrorHandling {

    public String handleError(Throwable error, ObjectMapper mapper) {
        // If there is an error get the message from error body
        if (error instanceof HttpException) {
            try {
                // Cast throwable error to http exception and get the error body
                String errorBody = Objects.requireNonNull(Objects.requireNonNull(((HttpException) error).response()).errorBody()).string();

                // Create a json node from error body
                JsonNode jsonNode = mapper.readTree(errorBody);
                JsonNode node = jsonNode.get("message");

                // Check if node is an array
                String msg;
                if (node.isArray()) {
                    msg = node.get(0).asText();
                } else {
                    msg = node.asText();
                }
                return msg;
            } catch (Exception e) {
                return Constants.CUSTOM_ERROR;
            }
        } else {
            return Constants.CUSTOM_ERROR;
        }
    }
}
