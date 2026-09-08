package com.example.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Security error handlers")
class SecurityHandlersTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Nested
    @DisplayName("RestAuthenticationEntryPoint")
    class EntryPointTest {

        private final RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(objectMapper);

        @Test
        @DisplayName("writes a 401 JSON error body when authentication is required")
        void writesUnauthorizedJsonBody() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AuthenticationException exception = new AuthenticationException("not authenticated") {};

            entryPoint.commence(request, response, exception);

            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getContentType()).startsWith("application/json");
            assertThat(response.getContentAsString()).contains("\"code\":\"UNAUTHORIZED\"");
        }
    }

    @Nested
    @DisplayName("RestAccessDeniedHandler")
    class AccessDeniedHandlerTest {

        private final RestAccessDeniedHandler handler = new RestAccessDeniedHandler(objectMapper);

        @Test
        @DisplayName("writes a 403 JSON error body when access is denied")
        void writesForbiddenJsonBody() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            handler.handle(request, response, new AccessDeniedException("denied"));

            assertThat(response.getStatus()).isEqualTo(403);
            assertThat(response.getContentType()).startsWith("application/json");
            assertThat(response.getContentAsString()).contains("\"code\":\"FORBIDDEN\"");
        }
    }
}
