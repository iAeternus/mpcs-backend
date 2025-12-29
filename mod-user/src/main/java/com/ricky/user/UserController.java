package com.ricky.user;

import com.ricky.user.domain.dto.cmd.RegisterCommand;
import com.ricky.user.domain.dto.resp.RegisterResponse;
import com.ricky.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/registration")
    @ResponseStatus(CREATED)
    public RegisterResponse register(@RequestBody @Valid RegisterCommand command) {
        return userService.register(command);
    }

}
