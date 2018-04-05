package io.saagie.astonparking

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.util.concurrent.Executor


@SpringBootApplication(scanBasePackages = arrayOf("me.ramswaroop.jbot", "io.saagie.astonparking"))
@EnableAsync
@EnableScheduling
class AstonParkingApplication {
    @Bean
    fun asyncExecutor(): Executor {
        var executor = ThreadPoolTaskExecutor()
        executor.apply {
            corePoolSize = 2
            maxPoolSize = 4
            setQueueCapacity(500)
        }
        executor.initialize()
        return executor
    }

}

@Configuration
@EnableSwagger2
class SwaggerConfig {
    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(AstonParkingApplication::class.java, *args)
}
