package io.saagie.astonparking.oauth

import io.saagie.astonparking.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.client.OAuth2ClientContext
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.Filter

@Configuration
@EnableOAuth2Client
class SSOFilter(var userService: UserService) : WebSecurityConfigurerAdapter(false) {

    @Qualifier("oauth2ClientContext")
    @Autowired
    var oauth2ClientContext: OAuth2ClientContext? = null


    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        // @formatter:off
        http.antMatcher("/**").authorizeRequests().antMatchers("/", "/login**", "/webjars/**", "/slack/**", "/guide/site/**","/v2/api-docs/**","/swagger-ui.html","/swagger-resources/**").permitAll().anyRequest()
                .authenticated().and().exceptionHandling()
                .authenticationEntryPoint(LoginUrlAuthenticationEntryPoint("/")).and().logout()
                .logoutSuccessUrl("/").permitAll()
                .and()
                .csrf().disable()
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter::class.java)
        // @formatter:on
    }

    @Bean
    fun oauth2ClientFilterRegistration(filter: OAuth2ClientContextFilter): FilterRegistrationBean {
        val registration = FilterRegistrationBean()
        registration.filter = filter
        registration.order = -100
        return registration
    }

    private fun ssoFilter(): Filter {
        val slackFilter = OAuth2ClientAuthenticationProcessingFilter(
                "/login/slack")
        val slackTemplate = OAuth2RestTemplate(slack(), oauth2ClientContext)
        slackFilter.setRestTemplate(slackTemplate)
        val tokenServices = UserInfoTokenServices(slackResource().userInfoUri,
                slack().clientId, userService)
        tokenServices.setRestTemplate(slackTemplate)
        slackFilter.setTokenServices(
                UserInfoTokenServices(slackResource().userInfoUri, slack().clientId, userService))
        return slackFilter
    }

    @Bean
    @ConfigurationProperties("slack.client")
    fun slack(): AuthorizationCodeResourceDetails {
        return AuthorizationCodeResourceDetails()
    }

    @Bean
    @ConfigurationProperties("slack.resource")
    fun slackResource(): ResourceServerProperties {
        return ResourceServerProperties()
    }

}