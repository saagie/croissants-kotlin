package io.saagie.astonparking.oauth

import io.saagie.astonparking.service.UserService
import org.apache.commons.logging.LogFactory
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedAuthoritiesExtractor
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedPrincipalExtractor
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.client.OAuth2RestOperations
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices
import java.util.*


open class UserInfoTokenServices(private val userInfoEndpointUrl: String, private val clientId: String, val userService: UserService) : ResourceServerTokenServices {
    var restTemplate: OAuth2RestOperations? = null

    internal var logger = LogFactory.getLog(this.javaClass)
    internal var authoritiesExtractor: AuthoritiesExtractor = FixedAuthoritiesExtractor()
    internal var principalExtractor: PrincipalExtractor = FixedPrincipalExtractor()

    fun setRestTemplate(restTemplate: OAuth2RestTemplate) {
        this.restTemplate = restTemplate
    }

    override fun loadAuthentication(accessToken: String): OAuth2Authentication {
        val map = this.getMap(this.userInfoEndpointUrl, accessToken)
        if (map.containsKey("error")) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("userinfo returned error: " + map.get("error"))
            }

            throw InvalidTokenException(accessToken)
        } else {
            userService.updateUserInfo(map)
            return this.extractAuthentication(map)
        }
    }

    private fun getMap(path: String, accessToken: String): Map<String, Any> {
        if (this.logger.isDebugEnabled) {
            this.logger.debug("Getting user info from: " + path)
        }

        try {
            var restTemplate: OAuth2RestOperations? = this.restTemplate
            if (restTemplate == null) {
                val resource = BaseOAuth2ProtectedResourceDetails()
                resource.clientId = this.clientId
                restTemplate = OAuth2RestTemplate(resource)
            }

            val existingToken = restTemplate.oAuth2ClientContext.accessToken
            if (existingToken == null || accessToken != existingToken.value) {
                val token = DefaultOAuth2AccessToken(accessToken)
                restTemplate.oAuth2ClientContext.accessToken = token
            }

            return restTemplate.getForEntity(path + accessToken, Map::class.java, *arrayOfNulls(0)).body as Map<String, Any>
        } catch (var6: Exception) {
            this.logger.warn("Could not fetch user details: " + var6.javaClass + ", " + var6.message)
            return Collections.singletonMap<String, Any>("error", "Could not fetch user details")
        }

    }

    private fun extractAuthentication(map: Map<String, Any>): OAuth2Authentication {
        val principal = this.getPrincipal(map)
        val authorities = this.authoritiesExtractor.extractAuthorities(map)
        val request = OAuth2Request(null, this.clientId, null, true, null, null, null as String?, null, null)
        val token = UsernamePasswordAuthenticationToken(principal, "N/A", authorities)
        token.details = map
        return OAuth2Authentication(request, token)
    }

    protected fun getPrincipal(map: Map<String, Any>): Any {
        val principal = this.principalExtractor.extractPrincipal(map)
        return principal ?: "unknown"
    }

    override fun readAccessToken(p0: String?): OAuth2AccessToken {
        throw UnsupportedOperationException("Not supported: read access token")
    }

}
