package com.relieflink.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();//get the URI
        /// To see what uri gets from the handler

        System.out.println("uri: " + uri);
        //This retrieves the current running session. False indicate that no new session will be createed
        HttpSession session = request.getSession(false);

        if (uri.startsWith("/api/")) {//get data from index page
            if (session == null || session.getAttribute("userId") == null) {//checks if session is live or user id is null
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Unauthorized. Please login first.\"}");
                return false;
            }

            //there is a admin page now so but code will be used later when we will have admin page
            if (uri.startsWith("/api/admin/")) {//get the data from admin page
                String userRole = (String) session.getAttribute("userRole");
                //System.out.println("userRole: " + userRole);
                if (!"ADMIN".equals(userRole)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"error\": \"Forbidden. Admin access required.\"}");
                    return false;
                }
            }
        }
        
        return true;
    }
}
