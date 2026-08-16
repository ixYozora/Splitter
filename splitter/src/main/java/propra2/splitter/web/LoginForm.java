package propra2.splitter.web;

import jakarta.validation.constraints.Pattern;

// Die Namensregeln von GitHub: 1 bis 39 Zeichen, alphanumerisch oder einzelne Bindestriche,
// nicht am Anfang und nicht am Ende.
public record LoginForm(
    @Pattern(
            regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$",
            message = "Invalider Githubname")
        String login) {}
