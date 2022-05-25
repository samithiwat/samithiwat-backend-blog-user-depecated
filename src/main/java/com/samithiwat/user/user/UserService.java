package com.samithiwat.user.user;

import com.samithiwat.user.grpc.dto.User;

public interface UserService {
    User findOne(Long id);
}
