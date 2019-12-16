package eniso.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * CasServiceApplication
 *
 * @author Eniso
 */
@SpringBootApplication
@ComponentScan(basePackages = {"eniso.common", "eniso.sample"})
public class CasServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CasServiceApplication.class, args);
    }

}
