package ru.worm.discord.chill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import ru.worm.discord.chill.util.ExceptionUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ExitBlock implements InitializingBean {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ExecutorService service;
    private CountDownLatch latch;

    public ExitBlock() {
        this.service = Executors.newSingleThreadExecutor();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(latch::countDown));
        service.execute(() -> {
            try {
                log.info("infinite await is set to keep spring from exiting");
                latch.await();
                log.info("yey! i've been killed");
            } catch (InterruptedException e) {
                log.error(ExceptionUtils.getStackTrace(e));
            }
        });
    }
}
