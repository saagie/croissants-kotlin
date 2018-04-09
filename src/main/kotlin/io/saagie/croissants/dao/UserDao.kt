package io.saagie.croissants.dao

import io.saagie.croissants.domain.User
import org.hibernate.validator.constraints.Email
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserDao : CrudRepository<User, String> {
    fun findByEnable(active: Boolean): List<User>
    fun findOneByEmail(email: String): User
    fun findOneById(id: String): User
}