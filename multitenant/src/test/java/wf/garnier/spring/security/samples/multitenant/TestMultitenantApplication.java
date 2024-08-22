package wf.garnier.spring.security.samples.multitenant;

import org.springframework.boot.SpringApplication;

public class TestMultitenantApplication {

	public static void main(String[] args) {
		SpringApplication.from(MultitenantApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
