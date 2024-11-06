package wf.garnier.security.samples.authorization;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationApplicationTests {

	@Autowired
	MockMvcTester mvc;

	@Test
	@WithCustomUser(name = "alice", companyId = "alpha", roles = { "user" })
	void validUser() {
		mvc.get()
			.uri("/company/alpha")
			.exchange()
			.assertThat()
			.hasStatus(HttpStatus.OK)
			.bodyText()
			.contains("Here are the company details for Alpha Corp.");
	}

	@Test
	@WithCustomUser(name = "alice", companyId = "omega", roles = { "user" })
	void invalidUser() {
		//@formatter:off
		mvc.get()
			.uri("/company/alpha")
			.exchange()
			.assertThat()
			.hasStatus(HttpStatus.FORBIDDEN);
		//@formatter:on
	}

	@Test
	@WithCustomUser(name = "alice", companyId = "alpha", roles = { "user", "admin" })
	void validAdmin() {
		mvc.get()
			.uri("/company/alpha/admin")
			.exchange()
			.assertThat()
			.hasStatus(HttpStatus.OK)
			.bodyText()
			.contains("This is the admin page for Alpha Corp.");
	}

	@Test
	@WithCustomUser(name = "alice", companyId = "alpha", roles = { "user" })
	void invalidAdmin() {
		//@formatter:off
		mvc.get()
				.uri("/company/alpha/admin")
				.exchange()
				.assertThat()
				.hasStatus(HttpStatus.FORBIDDEN);
		//@formatter:on
	}

}
