package io.saagie.astonparking.service

import io.saagie.astonparking.dao.ScheduleDao
import io.saagie.astonparking.dao.UserDao


import io.saagie.astonparking.domain.User
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.function.Consumer
@Service
class UserService(
        val userDao: UserDao,
        val emailService: EmailService) {

    fun registerUser(username: String, id: String): Boolean {
        if (!userDao.exists(id)) {
            userDao.save(User(id = id, username = username))
            return true
        }
        return false
    }


    fun unregisterUser(userId: String):Boolean {
        val user = userDao.findOne(userId)
        user.unregister=!user.unregister
        userDao.save(user)
        return user.unregister
    }

    fun updateUserInfo(map: Map<String, Any>) {
        val userMap = map.get("user") as Map<*, *>
        val id = userMap.get("id") as String
        if (userDao.exists(id)) {
            val user = userDao.findOne(id)
            val activatedUser = user.activated
            user.apply {
                email = userMap.get("email") as String
                image_24 = userMap.get("image_24") as String
                image_32 = userMap.get("image_32") as String
                image_48 = userMap.get("image_48") as String
                image_72 = userMap.get("image_72") as String
                image_192 = userMap.get("image_192") as String
                image_512 = userMap.get("image_512") as String
                activated = true

            }
            if (!activatedUser) {
                user.enable = true
                emailService.profileCreated(user)
            }
            userDao.save(user)
        }
    }

    fun get(id: String): User {
        if (userDao.exists(id)) {
            return userDao.findOne(id)
        }
        throw IllegalArgumentException("User (id:${id}) not found")
    }

    fun getAll(): List<User> {
        return userDao.findAll() as List<User>
    }

    fun getAllActive(): List<User> {
        return userDao.findByEnable(true)
    }

    fun changeStatus(id: String) {
        val user = get(id)
        user.enable = !user.enable
        userDao.save(user)
        emailService.profileStatusChange(user)
    }

    fun changeStatus(id: String, status: Boolean) {
        val user = get(id)
        user.enable = status
        userDao.save(user)
        emailService.profileStatusChange(user)
    }

    fun save(user: User) {
        userDao.save(user)
    }

    fun saveall(users: List<User>) {
        userDao.save(users)
    }


    fun resetAllSelectedAttribution() {
        userDao.findAll()
                .forEach(
                        Consumer {
                            it.alreadySelected = false
                            userDao.save(it)
                        })
    }

    fun findByUnregister(unregister: Boolean) = userDao.findByUnregister(unregister)
    fun delete(userId: String) = userDao.delete(userId)

}