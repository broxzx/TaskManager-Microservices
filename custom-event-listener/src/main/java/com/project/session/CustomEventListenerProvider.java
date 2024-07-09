package com.project.session;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserSessionModel;

import java.util.List;

@Slf4j
public class CustomEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;


    public CustomEventListenerProvider(KeycloakSession session) {
        this.session = session;
    }


    @Override
    public void onEvent(Event event) {
        if (event.getType() == EventType.LOGIN) {
            log.info("Login event detected for user: {}", event.getUserId());

            String userId = event.getUserId();
            List<UserSessionModel> userSessions = session.sessions().getUserSessionsStream(session.getContext().getRealm(),
                            session.users().getUserById(session.getContext().getRealm(), userId))
                    .toList();

            System.out.println("userSessions: " + userSessions.size());
            if (userSessions.size() >= 2) {
                UserSessionModel oldestSession = userSessions.get(0);
                for (UserSessionModel userSession : userSessions) {
                    if (userSession.getStarted() < oldestSession.getStarted()) {
                        oldestSession = userSession;
                    }
                }


                session.sessions().removeUserSession(session.getContext().getRealm(), oldestSession);
                log.info("Oldest session removed for user: {}", userId);
            }
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {

    }

    @Override
    public void close() {

    }
}