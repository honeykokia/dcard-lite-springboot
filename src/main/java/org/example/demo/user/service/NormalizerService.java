package org.example.demo.user.service;

import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class NormalizerService {

    public String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public String normalizeDisplayName(String name) {
        if (name == null) {
            return null;
        }
        return name.trim();
    }
}
