package com.paymentapp.security;
 
import java.util.Date;
import java.util.stream.Collectors;
 
import javax.crypto.SecretKey;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
 
import com.paymentapp.entity.User;
import com.paymentapp.exception.UserApiException;
import com.paymentapp.repository.UserRepository;
 
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
 
@Component
public class JwtTokenProvider {
	@Autowired
	private UserRepository userRepo;
	@Value("${app.jwt-secret}")
	private String jwtSecret;
 
	@Value("${app-jwt-expiration-milliseconds}")
	private long jwtExpirationMillis;
 
	public String generateToken(Authentication authentication) {
		String email = authentication.getName();
		Date now = new Date();
		Date expires = new Date(now.getTime() + jwtExpirationMillis);
		
		User user = userRepo.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));
 
		Claims claims = Jwts.claims().setSubject(email);
		claims.put("userId", user.getUserId());
		claims.put("roles",
				authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
						.collect(Collectors.toList()));
		
		if (user.getOrganization() != null) {
			claims.put("orgId", user.getOrganization().getOrgId());
			claims.put("orgStatus", user.getOrganization().getStatus()); 
		}
		//new change
		   if (user.getEmployee() != null) {
		        claims.put("empId", user.getEmployee().getEmpId());
		    }
		
		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(expires)
				.signWith(key())
				.compact();
	}
 
	public String generateTokenWithEmail(String email) {
		Date now = new Date();
		Date expires = new Date(now.getTime() + jwtExpirationMillis);
 
		return Jwts.builder().setSubject(email).setIssuedAt(now).setExpiration(expires).signWith(key()).compact();
	}
 
	private SecretKey key() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
	}
 
	public String getUsername(String token) {
		Claims claims = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
		return claims.getSubject();
	}
 
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
			return true;
		} catch (MalformedJwtException ex) {
			throw new UserApiException("Invalid JWT token");
		} catch (ExpiredJwtException ex) {
			throw new UserApiException("Expired JWT token");
		} catch (UnsupportedJwtException ex) {
			throw new UserApiException("Unsupported JWT token");
		} catch (IllegalArgumentException ex) {
			throw new UserApiException("JWT claims string is empty.");
		} catch (Exception e) {
			throw new UserApiException("Invalid Credentials");
		}
	}
}
 
 
 