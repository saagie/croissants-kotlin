package io.saagie.croissants.service

import io.saagie.croissants.dao.UserDao


import io.saagie.croissants.domain.User
import org.springframework.stereotype.Service
@Service
class UserService(
        val userDao: UserDao,
        val emailService: EmailService,
        val historyService: HistoryService

) {

    fun registerUser(username: String, id: String): Boolean {
        if (!userDao.exists(id)) {
            val allHistory = historyService.getAll()
            val weight = if (allHistory.isNotEmpty()) {
                allHistory.groupBy { it.idUser }.minBy { it.component2().size }!!.component2().size
            } else {
                0
            }
            userDao.save(User(id = id, username = username, initialWeight = weight))
            return true
        }
        return false
    }

    fun updateUserInfo(map: Map<String, Any>) {
        val userMap = map.get("user") as Map<*, *>
        val id = userMap.get("id") as String

        if (! userDao.exists(id)) {
            registerUser(userMap.get("name") as String, id)
        }
        if (userDao.exists(id)) {
            val user = userDao.findOne(id)
            user.apply {
                email = userMap.get("email") as String
                image_24 = userMap.get("image_24") as String
                image_32 = userMap.get("image_32") as String
                image_48 = userMap.get("image_48") as String
                image_72 = userMap.get("image_72") as String
                image_192 = userMap.get("image_192") as String
                image_512 = userMap.get("image_512") as String

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

    fun getWeightedCoefficient(user: User): Int {
        val countSelection = historyService.getAllByUser(user.id).size
        return Math.floor((user.coefficient / ((countSelection+1)*0.5))*10).toInt()
    }

    fun findByUnregister(unregister: Boolean) = userDao.findByUnregister(unregister)
    fun delete(userId: String) = userDao.delete(userId)

}