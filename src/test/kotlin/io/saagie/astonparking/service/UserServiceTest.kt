package io.saagie.astonparking.service

import com.nhaarman.mockito_kotlin.*
import io.saagie.astonparking.dao.ScheduleDao
import io.saagie.astonparking.dao.UserDao
import io.saagie.astonparking.domain.User
import org.amshove.kluent.*
import org.junit.Test
import org.mockito.ArgumentCaptor
import java.time.Instant
import java.util.*


class UserServiceTest {

    val allUser = initAllUser()

    val userCaptor = ArgumentCaptor.forClass(User::class.java)

    val userDao: UserDao = mock<UserDao> {
        on { findOne("ID1") }.doReturn(allUser.filter { it.id == "ID1" }.first())
        on { findOne("ID3") }.doReturn(allUser.filter { it.id == "ID3" }.first())
        on { exists("id") }.doReturn(false)
        on { exists("ID1") }.doReturn(true)
        on { exists("ID3") }.doReturn(true)
        on { findAll() }.doReturn(allUser)
        on { findByEnable(true) }.doReturn(allUser.filter { it.activated })
    }

    val emailService: EmailService = mock<EmailService> {

    }


    val userService = UserService(userDao, emailService)

    @Test
    fun should_return_an_existing_user() {
        //Given
        //When
        val user = userService.get(allUser.first().id!!)
        //Then
        user `should equal` allUser.first()
    }

    @Test
    fun should_return_all_users() {
        //Given
        //When
        val users = userService.getAll()
        //Then
        users `should equal` allUser
    }

    @Test
    fun should_return_all_active_users() {
        //Given
        //When
        val users = userService.getAllActive()
        //Then
        users `should equal` allUser.filter { it.activated }
    }

    @Test
    fun should_change_status() {
        //Given
        val user = allUser.filter { it.enable }.first()
        val id = user.id!!
        //When
        userService.changeStatus(id)
        //Then
        verify(userDao, times(1)).findOne(id)
        verify(userDao, times(1)).save(userCaptor.capture())
        userCaptor.value.enable `should be` false
    }


    @Test
    fun should_change_status_with_value() {
        //Given
        val user = allUser.filter { it.enable }.first()
        val id = user.id!!
        //When
        userService.changeStatus(id, false)
        //Then
        verify(userDao, times(1)).findOne(id)
        verify(userDao, times(1)).save(userCaptor.capture())
        userCaptor.value.enable `should be` false
    }

    @Test
    fun should_register_user() {
        //Given
        //When
        val registerUser = userService.registerUser("username", "id")
        //Then
        registerUser `should be` true
        verify(userDao, times(1)).save(userCaptor.capture())
        userCaptor.value.id `should equal` "id"
        userCaptor.value.username `should equal` "username"
        userCaptor.value.creationDate `should not equal` null
        userCaptor.value.enable `should be` false
        userCaptor.value.activated `should be` false
        userCaptor.value.email `should be` null
    }

    @Test
    fun should_not_register_user_if_already_exist() {
        //Given
        val user = allUser.first()
        //When
        val registerUser = userService.registerUser(user.username, user.id!!)
        //Then
        registerUser `should be` false
        verify(userDao, never()).save(userCaptor.capture())
    }


    @Test
    fun should_update_user() {
        //Given
        val user = allUser.filter { !it.activated }.first()
        val mapUser = mapOf<String, Any>(
                Pair("user", mapOf(
                        Pair("id", user.id!!),
                        Pair("email", "mail@mail.com"),
                        Pair("image_24", "image_24"),
                        Pair("image_32", "image_32"),
                        Pair("image_48", "image_48"),
                        Pair("image_72", "image_72"),
                        Pair("image_192", "image_192"),
                        Pair("image_512", "image_512")
                ))
        )
        //When
        userService.updateUserInfo(mapUser)
        //Then
        verify(userDao, times(1)).save(userCaptor.capture())
        val userSaved = userCaptor.value
        userSaved.activated `should be` true
        userSaved.enable `should be` true
        userSaved.image_24 shouldEqual "image_24"
        userSaved.image_32 shouldEqual "image_32"
        userSaved.image_48 shouldEqual "image_48"
        userSaved.image_72 shouldEqual "image_72"
        userSaved.image_192 shouldEqual "image_192"
        userSaved.image_512 shouldEqual "image_512"
    }

    @Test
    fun should_save_user() {
        //Given
        //When
        userService.save(allUser.first())
        //Then
        verify(userDao, times(1)).save(userCaptor.capture())
        userCaptor.value shouldEqual allUser.first()
    }

    @Test
    fun should_reset_already_selected() {
        //Given
        //When
        userService.resetAllSelectedAttribution()
        //Then
        verify(userDao, times(1)).findAll()
        verify(userDao, times(allUser.size)).save(userCaptor.capture())
        userCaptor.allValues.map { it.alreadySelected } `should contain` false
    }

    @Test
    fun should_unregister_user(){
        //Given
        //When
        val unregisterUser = userService.unregisterUser(allUser.first().id!!)
        //Then
        unregisterUser `should be equal to` true
    }

    @Test
    fun should_cancel_unregister_user(){
        //Given
        userService.unregisterUser(allUser.first().id!!)
        //When
        val unregisterUser = userService.unregisterUser(allUser.first().id!!)
        //Then
        unregisterUser `should be equal to` false
    }

    private fun initAllUser(): List<User> {
        return arrayListOf<User>(
                User(
                        id = "ID1",
                        username = "User1",
                        email = "mail1@mail.com",
                        creationDate = Date.from(Instant.now()),
                        attribution = 0,
                        enable = true,
                        activated = true),
                User(
                        id = "ID2",
                        username = "User2",
                        email = "mail2@mail.com",
                        creationDate = Date.from(Instant.now()),
                        attribution = 0,
                        enable = true,
                        activated = true),
                User(
                        id = "ID3",
                        username = "User3",
                        email = null,
                        creationDate = Date.from(Instant.now()),
                        attribution = 0,
                        enable = false,
                        activated = false),
                User(
                        id = "ID4",
                        username = "User4",
                        email = "mail4@mail.com",
                        creationDate = Date.from(Instant.now()),
                        attribution = 0,
                        enable = false,
                        activated = true)
        )
    }
}
