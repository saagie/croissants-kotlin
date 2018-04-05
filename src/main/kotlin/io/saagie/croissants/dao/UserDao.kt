package io.saagie.croissants.dao

import io.saagie.croissants.domain.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserDao : CrudRepository<User, String> {
    fun findByEnable(active: Boolean): List<User>
    fun findByUnregister(unregister: Boolean): List<User>
}