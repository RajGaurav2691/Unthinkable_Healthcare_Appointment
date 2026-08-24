package com.project.config;

import com.project.repository.GoogleCalendarTokenRepository;
import com.project.repository.UserRepository;
import com.project.service.calendar.CalendarService;
import com.project.service.calendar.GoogleCalendarService;
import com.project.service.calendar.MockCalendarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CalendarConfig {

    @Value("${application.google.calendar-enabled:false}")
    private boolean calendarEnabled;

    @Bean
    public CalendarService calendarService(GoogleCalendarTokenRepository tokenRepository, UserRepository userRepository) {
        if (!calendarEnabled) {
            log.info("Initializing MockCalendarService (calendar-enabled is false)");
            return new MockCalendarService();
        }
        log.info("Initializing GoogleCalendarService");
        return new GoogleCalendarService(tokenRepository, userRepository);
    }
}
