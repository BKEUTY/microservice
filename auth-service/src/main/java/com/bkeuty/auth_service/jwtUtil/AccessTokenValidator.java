package com.bkeuty.auth_service.jwtUtil;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.bkeuty.auth_service.dto.TokenValidationResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;

@Service
public class AccessTokenValidator {
    @Autowired
    TenantJwkProvider tenantJwkProvider;
    @Value("${keycloak.issuer}")
    private String issuer;

    @Value("${keycloak.jwks-uri}")
    private String jwksUri;

    public TokenValidationResponseDto validate(String token){
        JwkProvider jwkProvider = tenantJwkProvider.getJwkProvider(jwksUri);
        try{
            DecodedJWT decodedJWT = JWT.decode(token);
            Jwk jwk = jwkProvider.get(decodedJWT.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey)  jwk.getPublicKey(),null);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuer).build();
            verifier.verify(decodedJWT);
        } catch (JWTVerificationException e){
            System.out.println("Verification Exception: "+ e.getMessage());
            return null;
        } catch (Exception e){
            System.out.println("Exception: "+ e.getMessage());
            return null;
        }
        return getUserInfo(token);
    }
    private TokenValidationResponseDto getUserInfo(String token){
        DecodedJWT decodedJWT = JWT.decode(token);
        String userId = null;
        String firstName = null;
        String lastName = null;
        String userRole = null;
        if(decodedJWT.getClaim("sub").asString()!=null){
            userId =  decodedJWT.getClaim("sub").asString();
        }
        if(decodedJWT.getClaim("user_role").asString()!=null){
            userRole =  decodedJWT.getClaim("user_role").asString();
        }
        if(decodedJWT.getClaim("given_name").asString()!=null){
            firstName =  decodedJWT.getClaim("given_name").asString();
        }
        if(decodedJWT.getClaim("family_name").asString()!=null){
            lastName =  decodedJWT.getClaim("family_name").asString();
        }
        return  TokenValidationResponseDto.builder()
                .userId(userId)
                .userRole(userRole)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }


}
