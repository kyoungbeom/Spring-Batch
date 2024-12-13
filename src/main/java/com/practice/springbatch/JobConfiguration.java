package com.practice.springbatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
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
                .build();
    }

//    @Bean
//    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
//        return new StepBuilder("step", jobRepository)
//                .tasklet((contribution, chunkContext) -> {
//                    log.info("step 실행");
//                    return RepeatStatus.FINISHED;
//                }, platformTransactionManager)
//                .build();
//    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        final ItemReader<Integer> itemReader = new ItemReader<>() {
            private int count = 0;

            @Override
            public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                count++;

                log.info("Read {}", count);

                if (count == 20) {
                    return null;
                }

//                if (count >= 15) {
//                    throw new IllegalStateException("예외 발생");
//                }

                return count;
            }
        };

        final ItemProcessor<Integer, Integer> itemProcessor = new ItemProcessor<>() {
            @Override
            public Integer process(Integer item) throws Exception {

                if(item == 15) {
                    throw new IllegalStateException();
                }

                return item;
            }
        };

        return new StepBuilder("step", jobRepository)
                .<Integer, Integer>chunk(10, platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(read -> {})
                .faultTolerant()
                .retry(IllegalStateException.class) // processor가 있어야 사용가능
                .retryLimit(5)
                .build();
    }

}
