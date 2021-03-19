package ehealth.cashregisterintegration.rest;

import ehealth.cashregisterintegration.data.rest.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@CrossOrigin("*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String LOGOUT_MSG = "Logged out successful";

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest req, HttpServletResponse res) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(req, res, auth);
        }

        return ResponseEntity.ok(ApiResponse.builder().message(LOGOUT_MSG).build());
    }
}
