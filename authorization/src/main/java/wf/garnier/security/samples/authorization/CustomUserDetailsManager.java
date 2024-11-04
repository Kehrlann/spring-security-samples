package wf.garnier.security.samples.authorization;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import static java.util.stream.Collectors.toUnmodifiableMap;

class CustomUserDetailsManager implements UserDetailsService {

	private final Map<String, CustomUser> users;

	public CustomUserDetailsManager(CustomUser... users) {
		this.users = Arrays.stream(users).collect(toUnmodifiableMap(CustomUser::getUsername, Function.identity()));
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new CustomUser(users.get(username));
	}

}
