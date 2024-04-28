package com.infitry.batch.job;

import com.infitry.batch.persistence.entity.BookEntity;
import jakarta.persistence.EntityManagerFactory;
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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
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
    private final EntityManagerFactory entityManagerFactory;
    private static final int CHUNK_SIZE = 3;

    @Bean
    public Job chunkJob(Step chunkStep,
                        Step chunkTaskletStep) {
        return new JobBuilder("chunkJob", jobRepository)
                .start(chunkStep)
                .next(chunkTaskletStep)
                .build();
    }

    @Bean
    @JobScope
    public Step chunkStep(@Value("#{jobParameters[jobId]}") String jobId,
                          ItemReader<String> chunkReader,
                          ItemProcessor<String, BookEntity> chunkProcessor,
                          ItemWriter<BookEntity> chunkWriter) {
        return new StepBuilder("chunkStep", jobRepository)
                .<String, BookEntity>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(chunkReader)
                .processor(chunkProcessor)
                .writer(chunkWriter)
                .build();
    }

    @Bean
    @StepScope
    public ListItemReader<String> chunkReader() {
        return new ListItemReader<>(List.of("1", "2", "3", "4", "5", "6", "7", "8"));
    }

    @Bean
    public ItemProcessor<String, BookEntity> chunkProcessor() {
        return item -> {
            System.out.println("======== processed item :" + item + " ===============");
            return new BookEntity(null, "book" + item, item);
        };
    }

    @Bean
    public JpaItemWriter<BookEntity> chunkWriter() {
        return new JpaItemWriterBuilder<BookEntity>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }

    @Bean
    @JobScope
    public Step chunkTaskletStep() {
        return new StepBuilder("chunkTaskletStep", jobRepository)
                .tasklet((a, b) -> {
                    log.info("================= Start Step2 =================");
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)
                .build();
    }
}
