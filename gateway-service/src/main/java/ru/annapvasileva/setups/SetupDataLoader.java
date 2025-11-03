package ru.annapvasileva.setups;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.annapvasileva.persistence.Roles;
import ru.annapvasileva.persistence.UserEntity;
import ru.annapvasileva.persistence.UserRepository;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class SetupDataLoader implements
        ApplicationListener<ContextRefreshedEvent> {

    boolean alreadySetup = false;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final String adminUsername = "admin";
    private static final String adminPassword = "password";
    private static final String firstNameExample = "Michael";
    private static final String lastNameExample = "Jackson";
    private static final LocalDate birthdayExample = LocalDate.of(1958, 8, 29);

    public SetupDataLoader(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup)
            return;
        if (userRepository.findByUsername(adminUsername).isPresent()) {
            alreadySetup = true;

            return;
        }


        UserEntity user = new UserEntity();
        user.setUsername(adminUsername);
        user.setPassword(passwordEncoder.encode(adminPassword));
        user.setRole(Roles.ROLE_ADMIN);
        user.setOwnerId(UUID.randomUUID());
        userRepository.save(user);

        alreadySetup = true;
    }
}
