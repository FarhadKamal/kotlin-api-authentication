package com.farhad.auth.repositories

import com.farhad.auth.models.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository:JpaRepository<User,Int> {
    fun findByEmail(email:String): User?

}