package com.example.autoboard.repository;

import com.example.autoboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByFirstName(String name);

    User findByLastName(String name);
}
