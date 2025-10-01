package io.pedrohma07.ImageVault.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Define o status da resposta como 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Cria o corpo da resposta no formato JSON
        Map<String, Object> body = new HashMap<>();
        body.put("statusCode", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("message", "Acesso não autorizado.");
        body.put("data", authException.getMessage()); // Mensagem de erro da exceção
        body.put("path", request.getServletPath());
        body.put("success", false);
        body.put("timestamp", LocalDateTime.now().toString());

        // Usa o ObjectMapper do Jackson para escrever o JSON na resposta
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}