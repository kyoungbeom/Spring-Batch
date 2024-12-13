package com.practice.springbatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class JobConfiguration {

    // JobRepository는 Spring Batch를 사용하면 자동으로 Bean로 등록됨 -> DefaultBatchConfiguration 확인
    @Bean
    public Job job(JobRepository jobRepository, Step step) {
        return new JobBuilder("job", jobRepository)
                .start(step)
                .incrementer(new RunIdIncrementer())
                .build()
                ;
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        final Tasklet tasklet = new Tasklet() {

            private int count = 0;

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                count++;

                if (count == 15) {
                    log.info("Tasklet FINISHED");
                    return RepeatStatus.FINISHED;
                }

                log.info("Tasklet CONTINUABLE {}", count);
                return RepeatStatus.CONTINUABLE;
            }
        };

        return new StepBuilder("step", jobRepository)
                .tasklet(tasklet, platformTransactionManager)
                .build();
    }

}
