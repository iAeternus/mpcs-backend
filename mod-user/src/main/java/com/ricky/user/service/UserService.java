package com.ricky.user.service;

import com.ricky.user.command.RegisterCommand;
import com.ricky.user.command.RegisterResponse;

public interface UserService {
    RegisterResponse register(RegisterCommand command);
}
