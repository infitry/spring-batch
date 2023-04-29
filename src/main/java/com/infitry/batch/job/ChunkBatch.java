package com.infitry.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChunkBatch {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private static final int CHUNK_SIZE = 3;

    @Bean("chunkJob")
    public Job chunkJob(@Qualifier("chunkStep") Step chunkStep) {
        return new JobBuilder("chunkJob", jobRepository)
                .start(chunkStep)
                .build();
    }

    @Bean("chunkStep")
    @JobScope
    public Step chunkStep(@Value("#{jobParameters[jobId]}") String jobId,
                          ItemReader<String> chunkReader,
                          ItemProcessor<String, Integer> chunkProcessor,
                          ItemWriter<Integer> chunkWriter) {
        return new StepBuilder("chunkStep", jobRepository)
                .<String, Integer>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(chunkReader)
                .processor(chunkProcessor)
                .writer(chunkWriter)
                .build();
    }

    @Bean
    @StepScope
    public ListItemReader<String> chunkReader() {
        return new ListItemReader<>(List.of("1", "2", "3", "4", "5"));
    }

    @Bean
    public ItemProcessor<String, Integer> chunkProcessor() {
        return Integer::parseInt;
    }

    @Bean
    public ItemWriter<Integer> chunkWriter() {
        return item -> log.info(item.toString());
    }
}
