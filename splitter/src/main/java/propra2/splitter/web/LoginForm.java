package propra2.splitter.web;

import javax.validation.constraints.Pattern;


public record LoginForm(
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,15}$", message = "Invalider Githubname") String login) {

}
