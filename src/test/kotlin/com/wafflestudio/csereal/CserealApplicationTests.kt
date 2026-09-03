package com.wafflestudio.csereal

import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@ActiveProfiles("test")
@SpringBootTest
@Import(MySQLTestContainerConfig::class)
class CserealApplicationTests {

    @Test
    fun contextLoads() {
    }
}
