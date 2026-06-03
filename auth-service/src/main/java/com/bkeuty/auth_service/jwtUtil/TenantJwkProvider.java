package com.bkeuty.auth_service.jwtUtil;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Component
public class TenantJwkProvider {
    private JwkProvider jwkProvider = null;
    public JwkProvider getJwkProvider(String jwkUrl) {
        if (jwkProvider == null) {
            try {
                URL url = URI.create(jwkUrl).toURL();
                jwkProvider = new JwkProviderBuilder(url).cached(10,24, TimeUnit.HOURS).rateLimited(10,1,TimeUnit.MINUTES).build();
            }catch (Exception e){
                throw new RuntimeException("Cannot build jwk provider for "+ jwkUrl,e);
            }
        }
        return jwkProvider;
    }
}
