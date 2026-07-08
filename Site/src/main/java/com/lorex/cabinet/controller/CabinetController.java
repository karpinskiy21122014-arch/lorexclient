package com.lorex.cabinet.controller;

import com.lorex.cabinet.model.User;
import com.lorex.cabinet.model.SubscriptionKey;
import com.lorex.cabinet.repository.UserRepository;
import com.lorex.cabinet.repository.KeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class CabinetController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeyRepository keyRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Логин занят!");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRegistrationDate(LocalDate.now().toString());
        user.setSubscriptionTill(LocalDate.now().minusDays(1).toString());
        userRepository.save(user);
        return ResponseEntity.ok("Регистрация успешна!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return ResponseEntity.ok(userOpt.get());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверные данные!");
    }

    @PostMapping("/generate-key")
    public ResponseEntity<String> generateKey(@RequestParam Long userId, @RequestParam int days) {
        if (userId != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ запрещен!");
        }

        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random r = new Random();
        StringBuilder keyBuilder = new StringBuilder("LOREX-");

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                keyBuilder.append(alphabet.charAt(r.nextInt(alphabet.length())));
            }
            if (i < 2) keyBuilder.append("-");
        }

        String finalKey = keyBuilder.toString();

        SubscriptionKey keyEntity = new SubscriptionKey();
        keyEntity.setKeyCode(finalKey);
        keyEntity.setDays(days);
        keyEntity.setUsed(false);
        keyRepository.save(keyEntity);

        return ResponseEntity.ok(finalKey);
    }

    @PostMapping("/activate-key")
    public ResponseEntity<?> activateKey(@RequestParam String username, @RequestParam String key) {
        Optional<SubscriptionKey> keyOpt = keyRepository.findByKeyCode(key.toUpperCase().trim());
        if (keyOpt.isEmpty() || keyOpt.get().isUsed()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ключ не существует или уже активирован!");
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден.");
        }

        SubscriptionKey subKey = keyOpt.get();
        User user = userOpt.get();

        LocalDate currentSubEnd;
        try {
            currentSubEnd = LocalDate.parse(user.getSubscriptionTill());
        } catch (Exception e) {
            currentSubEnd = LocalDate.now();
        }

        if (currentSubEnd.isBefore(LocalDate.now())) {
            currentSubEnd = LocalDate.now();
        }

        user.setSubscriptionTill(currentSubEnd.plusDays(subKey.getDays()).toString());
        userRepository.save(user);

        subKey.setUsed(true);
        keyRepository.save(subKey);

        return ResponseEntity.ok(user);
    }

    // ==========================================
    // НОВЫЕ ЭНДПОИНТЫ ПРЯМОГО УПРАВЛЕНИЯ ДЛЯ АДМИНА
    // ==========================================

    // Получить список абсолютно всех пользователей
    @GetMapping("/admin/users")
    public ResponseEntity<?> getAllUsers(@RequestParam Long adminId) {
        if (adminId != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ запрещен!");
        }
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // Изменить подписку напрямую (Добавить дней или Сбросить)
    @PostMapping("/admin/update-subscription")
    public ResponseEntity<String> updateSubscription(
            @RequestParam Long adminId,
            @RequestParam Long targetUserId,
            @RequestParam String action,
            @RequestParam(required = false, defaultValue = "0") int days) {

        if (adminId != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ запрещен!");
        }

        Optional<User> userOpt = userRepository.findById(targetUserId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден.");
        }

        User user = userOpt.get();

        if ("add".equals(action)) {
            LocalDate currentSubEnd;
            try {
                currentSubEnd = LocalDate.parse(user.getSubscriptionTill());
            } catch (Exception e) {
                currentSubEnd = LocalDate.now();
            }

            // Если подписка уже кончилась, отсчет идет от сегодняшнего дня
            if (currentSubEnd.isBefore(LocalDate.now())) {
                currentSubEnd = LocalDate.now();
            }
            user.setSubscriptionTill(currentSubEnd.plusDays(days).toString());
        }
        else if ("remove".equals(action)) {
            // Ставим вчерашний день, деактивируя подписку
            user.setSubscriptionTill(LocalDate.now().minusDays(1).toString());
        }

        userRepository.save(user);
        return ResponseEntity.ok("Данные успешно сохранены!");
    }
}