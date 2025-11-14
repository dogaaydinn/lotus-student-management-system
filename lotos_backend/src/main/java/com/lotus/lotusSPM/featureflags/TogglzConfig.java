package com.lotus.lotusSPM.featureflags;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Togglz Configuration for Feature Flag Management.
 *
 * Enterprise Pattern: Feature Toggle Configuration
 *
 * Provides:
 * - Feature state management
 * - User-based activation
 * - Role-based activation
 * - Admin console for toggling
 * - Persistent state storage
 */
@Configuration
public class TogglzConfig {

    /**
     * Feature provider based on FeatureFlags enum.
     */
    @Bean
    public FeatureProvider featureProvider() {
        return new EnumBasedFeatureProvider(FeatureFlags.class);
    }

    /**
     * User provider for role-based feature activation.
     */
    @Bean
    public UserProvider userProvider() {
        return new UserProvider() {
            @Override
            public FeatureUser getCurrentUser() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                if (auth != null && auth.isAuthenticated()) {
                    boolean isAdmin = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                    return new SimpleFeatureUser(auth.getName(), isAdmin);
                }

                return new SimpleFeatureUser("anonymous", false);
            }
        };
    }
}
