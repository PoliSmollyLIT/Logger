package bg.tu.varna.sit.si.service;

import bg.tu.varna.sit.si.model.LogMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class LoggingService {

    @Inject
    EntityManager entityManager;

    @Transactional
    public void addLog(LogMessage logMessage) {
        entityManager.persist(logMessage);
    }
}
