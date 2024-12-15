package com.practice.springbatch;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
//@Configuration
public class MultiThreadedJobConfig {

    @Bean
    public Job job(
            JobRepository jobRepository,
            Step step
    ) {
        return new JobBuilder("multiThreadJob", jobRepository)
                .start(step)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step step(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            ItemReader<User> jpaPagingItemReader
    ) {
        return new StepBuilder("step", jobRepository)
                .<User, User> chunk(5, platformTransactionManager)
                .reader(jpaPagingItemReader)
                .writer(result -> log.info(result.toString()))
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public JpaPagingItemReader jpaPagingItemReader(
            EntityManagerFactory entityManagerFactory
    ) {
        return new JpaPagingItemReaderBuilder<User>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(5)
                // 병렬 실행(멀티쓰레드 환경)의 특성상 데이터가 순차적으로 처리되지 않으므로 재시작 시 어떤 상태에서 시작할지 결정하기 어려움.
                // 따라서 실패지점을 저장하는 설정을 false로 처리해야함
                .saveState(false)
                .queryString("SELECT u FROM User u ORDER BY u.id")
                .build();
    }
}
