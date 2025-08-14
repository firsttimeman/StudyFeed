//package FeedStudy.StudyFeed.global.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.concurrent.Executor;
//import java.util.concurrent.ThreadPoolExecutor;
//
//@Configuration
//@EnableAsync
//public class AsyncConfig {
//
//    @Bean(name = "ioExecutor")
//    public Executor ioExecutor() {
//        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
//        ex.setCorePoolSize(8);
//        ex.setMaxPoolSize(32);
//        ex.setQueueCapacity(1000);
//        ex.setThreadNamePrefix("io-");
//        ex.initialize();
//        return ex;
//    }
//}
