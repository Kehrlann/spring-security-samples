package wf.garnier.spring.security.samples.oauth2.resourceauth;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class DemoController {

	@GetMapping("/")
	public String index() {
		return "Hello World";
	}

	record Conference(String name, String country, String city) {
	}

	//@formatter:off
	private static final List<Conference> conferences = List.of(
			new Conference("Spring I/O", "Spain", "Barcelona"),
			new Conference("Voxxed Brussels", "Belgium", "Brussels"),
			new Conference("JFall", "Netherlands", "Ede"),
			new Conference("Devoxx France", "France", "Paris"),
			new Conference("Javaland", "Germany", "Nürburg")
	);
	//@formatter:on

	@GetMapping("/conferences")
	List<Conference> conferences() {
		return conferences;
	}

}
