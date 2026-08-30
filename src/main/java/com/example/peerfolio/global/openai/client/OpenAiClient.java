package com.example.peerfolio.global.openai.client;

import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import com.example.peerfolio.global.openai.code.OpenAiErrorCode;
import com.example.peerfolio.global.openai.dto.OpenAiRequest;
import com.example.peerfolio.global.openai.dto.OpenAiResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OpenAiClient {

    private final RestClient restClient;

    public OpenAiClient(
            @Qualifier("openAiRestClient") RestClient restClient
    ) {
        this.restClient = restClient;
    }

    public String createResponse(OpenAiRequest request) {
        try {
            OpenAiResponse response = restClient.post()
                    .uri("/responses")
                    .body(request)
                    .retrieve()
                    .body(OpenAiResponse.class);

            return extractResponseText(response);
        } catch (ResourceAccessException e) {
            throw new ProjectException(
                    OpenAiErrorCode.REQUEST_TIMEOUT
            );
        } catch (RestClientException e) {
            throw new ProjectException(
                    OpenAiErrorCode.REQUEST_FAILED
            );
        }
    }

    private String extractResponseText(OpenAiResponse response) {
        if (response == null) {
            throw new ProjectException(
                    OpenAiErrorCode.INVALID_RESPONSE
            );
        }

        if (response.hasRefusal()) {
            throw new ProjectException(
                    OpenAiErrorCode.RESPONSE_REFUSED
            );
        }

        if (!"completed".equals(response.status())) {
            throw new ProjectException(
                    OpenAiErrorCode.INVALID_RESPONSE
            );
        }

        String outputText = response.extractOutputText();

        if (outputText == null || outputText.isBlank()) {
            throw new ProjectException(
                    OpenAiErrorCode.INVALID_RESPONSE
            );
        }

        return outputText;
    }
}
