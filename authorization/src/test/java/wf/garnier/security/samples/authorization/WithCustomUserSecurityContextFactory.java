package wf.garnier.security.samples.authorization;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

class WithCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomUser> {

	private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
		.getContextHolderStrategy();

	@Override
	public SecurityContext createSecurityContext(WithCustomUser withUser) {
		var company = new Company(withUser.companyId(), "test-" + withUser.companyId());
		var principal = new CustomUser(withUser.name(), company, withUser.roles());
		Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(principal,
				principal.getPassword(), principal.getAuthorities());
		SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
		context.setAuthentication(authentication);
		return context;
	}

}
