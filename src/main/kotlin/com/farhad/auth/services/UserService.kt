package com.farhad.auth.services

import com.farhad.auth.models.User
import com.farhad.auth.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {
    fun save(user: User): User {
        return this.userRepository.save(user)
    }

    fun findByEmail(email: String): User? {
        return this.userRepository.findByEmail(email)
    }

    fun getById(id: Int): User {
        return this.userRepository.getById(id)
    }

    fun getAll(): List<User> {
        return this.userRepository.findAll()
    }
}