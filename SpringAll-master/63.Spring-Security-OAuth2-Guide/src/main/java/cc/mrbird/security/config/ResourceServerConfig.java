package cc.mrbird.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

/**
 * @author MrBird
 */
@Configuration
@EnableResourceServer
@Order(2)
public class ResourceServerConfig  {

}
