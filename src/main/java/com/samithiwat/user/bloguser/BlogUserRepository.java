package com.samithiwat.user.bloguser;

import com.samithiwat.user.bloguser.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface BlogUserRepository extends CrudRepository<User, Long> {
}
