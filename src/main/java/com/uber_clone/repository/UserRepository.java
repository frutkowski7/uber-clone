package com.uber_clone.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uber_clone.model.User;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor

public class UserRepository {

        private final JdbcTemplate jdbcTemplate;

        public Optional<User> findByEmail(String email) {
                String sql = """
                                SELECT id, email, password, role
                                FROM users
                                WHERE email = ?
                                """;

                // jdbcTemplate.query

                return jdbcTemplate
                                // tworzę nowa instancję User
                                .query(sql, (rs, rowNum) -> new User(
                                                // resultSet
                                                rs.getObject("id", UUID.class),
                                                rs.getString("email"),
                                                rs.getString("password"),
                                                // User.Role.valueOf
                                                User.Role.valueOf(rs.getString("role"))),
                                                email)
                                .stream().findFirst();
        }

        public Optional<UUID> getIdByEmail(String email) {
                String sql = """
                                        SELECT id FROM users WHERE email = ?
                                """;

                return jdbcTemplate.query(
                                sql,
                                (rs, rowNum) -> rs.getObject("id", UUID.class),
                                email).stream().findFirst();

        }

        public User save(User user) {

                String sql = """
                                INSERT INTO users (email,password, role)
                                VALUES (?, ?, ?)
                                returning id
                                        """;

                UUID id = jdbcTemplate.queryForObject(
                                sql, // QUERY
                                UUID.class, // typ zwracanego row .class przekazuje typ
                                user.getEmail(), // VALUE ?
                                user.getPassword(), // VALUE ?
                                user.getRole().name() // VALUE ?
                );

                user.setId(id);
                return user;
        }

}