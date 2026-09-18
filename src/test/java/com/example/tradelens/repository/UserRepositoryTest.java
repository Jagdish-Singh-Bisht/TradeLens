package com.example.tradelens.repository;


import com.example.tradelens.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveUser() {

        User user = new User();

        user.setUsername("Jagdish");
        user.setEmail("jagdish@mail.com");
        user.setPassword("1234");

        User savedUser = userRepository.save(user);

        System.out.println("Saved user ID: " + savedUser.getId());

    }


}
