package wf.garnier.security.samples.authorization;

import java.util.Arrays;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

class CustomUser extends User {

	private final Company company;

	// Don't do this in prod. It's just for demos.
	private static final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

	public CustomUser(String username, Company company, String... authorities) {
		//@formatter:off
		super(
				username,
				encoder.encode("password"),
				// Encode authorities to "roles"
				AuthorityUtils.createAuthorityList(Arrays.stream(authorities).map(a -> "ROLE_" + a).toArray(String[]::new))
		);
		//@formatter:on
		this.company = company;
	}

	public Company getCompany() {
		return company;
	}

}
