package com.example._4.__thuc_hanh_quan_he.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import java.io.IOException;

public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = "Sai tài khoản hoặc mật khẩu!";

        if (exception instanceof DisabledException) {
            errorMessage = "Tài khoản của bạn đã bị khóa bởi quản trị viên!";
        }

        String encodedMessage = java.net.URLEncoder.encode(errorMessage, "UTF-8");

        getRedirectStrategy().sendRedirect(request, response, "/login?error=true&msg=" + encodedMessage);
    }
}