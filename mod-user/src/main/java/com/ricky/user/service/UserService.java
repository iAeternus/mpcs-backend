package com.ricky.user.service;

import com.ricky.user.domain.dto.cmd.RegisterCommand;
import com.ricky.user.domain.dto.resp.RegisterResponse;

public interface UserService {
    RegisterResponse register(RegisterCommand command);
}
