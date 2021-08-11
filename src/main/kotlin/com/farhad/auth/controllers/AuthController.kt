package com.farhad.auth.controllers

import com.farhad.auth.dtos.LoginDTO
import com.farhad.auth.dtos.Message
import com.farhad.auth.dtos.RegisterDTO
import com.farhad.auth.models.User
import com.farhad.auth.services.UserService
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("api")
class AuthController(private val userService:UserService) {
    @PostMapping("register")
    fun register(@RequestBody body:RegisterDTO): ResponseEntity<Any> {
        if(this.userService.findByEmail(body.email)!=null)
            return ResponseEntity.badRequest().body(Message("Email has taken!"))


        val user = User()
        user.name = body.name
        user.email = body.email
        user.password = body.password
        return ResponseEntity.ok(this.userService.save(user))
    }

    @PostMapping("login")
    fun login(@RequestBody body: LoginDTO, response: HttpServletResponse): ResponseEntity<Any> {
        val user = this.userService.findByEmail(body.email)
                ?: return ResponseEntity.badRequest().body(Message("User not found!"))

        if (!user.comparePassword(body.password)) {
            return ResponseEntity.badRequest().body(Message("Invalid password!"))
        }

        val issuer = user.id.toString()

        val jwt = Jwts.builder()
                .setIssuer(issuer)
                .setExpiration(Date(System.currentTimeMillis() + 60* 60*1000)) // 1 hour
                .signWith(SignatureAlgorithm.HS512, "secret").compact()

        val cookie = Cookie("jwt007", jwt)
        cookie.isHttpOnly = true
        cookie.secure=true

        response.addCookie(cookie)

        return ResponseEntity.ok(this.userService.getById(issuer.toInt()))
    }

    @GetMapping("verify")
    fun verify(@CookieValue("jwt007") jwt: String?): ResponseEntity<Any> {
        try {
            if (jwt == null) {
                return ResponseEntity.status(401).body(Message("Unauthenticated"))
            }

            val body = Jwts.parser().setSigningKey("secret").parseClaimsJws(jwt).body

            return ResponseEntity.ok(this.userService.getById(body.issuer.toInt()))
        } catch (e: Exception) {
            return ResponseEntity.status(401).body(Message(e.message.toString()))
        }
    }


    fun auth( jwt:String?): Boolean {

        try {
            if (jwt == null) {
                return false
            }

            val body = Jwts.parser().setSigningKey("secret").parseClaimsJws(jwt).body

            this.userService.getById(body.issuer.toInt()) ?: return false

            return true
        } catch (e: Exception) {
            return false
        }
    }


    @GetMapping("users")
    fun users(@CookieValue("jwt007") jwt: String?): ResponseEntity<Any> {

        if(!auth(jwt))
            return ResponseEntity.status(401).body(Message("Unauthenticated"))
        return ResponseEntity.ok(this.userService.getAll())
    }

    @PostMapping("logout")
    fun logout(response: HttpServletResponse): ResponseEntity<Any> {
        val cookie = Cookie("jwt007", "")
        cookie.maxAge = 0
        cookie.isHttpOnly = true
        cookie.secure=true

        response.addCookie(cookie)

        return ResponseEntity.ok(Message("Success"))
    }
}