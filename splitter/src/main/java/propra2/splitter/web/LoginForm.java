package propra2.splitter.web;

import jakarta.validation.constraints.Pattern;

// Deckt GitHub-Konten und die oft mailfoermigen Keycloak-Namen ab: 1 bis 64 Zeichen,
// alphanumerisch, dazwischen einzelne . _ - oder @, nicht am Anfang und nicht am Ende.
public record LoginForm(
    @Pattern(
            regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9]|[._@-](?=[a-zA-Z0-9])){0,63}$",
            message = "Invalider Name")
        String login) {}
