package wf.garnier.security.samples.authorization;

import org.springframework.stereotype.Component;

@Component
class CompanyRepository {

	private static final Company ALPHA_CORP = new Company("alpha", "Alpha Corp");

	private static final Company OMEGA_INC = new Company("omega", "Omega Inc");

	public Company findById(String id) {
		return switch (id) {
			case "alpha" -> ALPHA_CORP;
			case "omega" -> OMEGA_INC;
			default -> throw new IllegalArgumentException("Unknown company: " + id);
		};
	}

}
