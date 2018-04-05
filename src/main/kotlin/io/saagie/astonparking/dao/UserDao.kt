package io.saagie.astonparking.dao

import io.saagie.astonparking.domain.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserDao : CrudRepository<User, String> {
    fun findByEnable(active: Boolean): List<User>
    fun findByUnregister(unregister: Boolean): List<User>
}