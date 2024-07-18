package com.in3rovert_so.securedoc.security;

import com.in3rovert_so.securedoc.domain.ApiAuthentication;
import com.in3rovert_so.securedoc.domain.RequestContext;
import com.in3rovert_so.securedoc.domain.TokenData;
import com.in3rovert_so.securedoc.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.in3rovert_so.securedoc.enumeration.TokenType.ACCESS;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            //First we extract the token
            var accessToken =jwtService.extractToken(request, ACCESS.getValue());
            //Check if the accessToken is present and valid
            if(accessToken.isPresent()&& jwtService.getTokenData(accessToken.get(), TokenData::isValid)) {
                //Set the authentication in the current thread
                SecurityContextHolder.getContext().setAuthentication(getAuthentication(accessToken.get(), request));
                //Tell it who is logged in
                RequestContext.setUserId(jwtService.getTokenData(accessToken.get(), TokenData::getUser).getId());
            }else {

            }
        }catch (Exception exception) {}
    }

    private Authentication getAuthentication(String token, HttpServletRequest request) {
        var authentication = ApiAuthentication.authenticated(jwtService.getTokenData(token, TokenData::getUser), jwtService.getTokenData(token, TokenData::getAuthorities));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }
}
