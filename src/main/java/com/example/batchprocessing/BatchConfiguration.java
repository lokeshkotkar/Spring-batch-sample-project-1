package com.example.batchprocessing;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
/*
 * the methods named reader,processor,writer are the methods  used for initial setup 
 * the methods importUserJob and step1 are the methods used for actual job configurations
 */
// tag::setup[]
@Configuration

// this annotation automatically creates datasource automatically, this annotation enables many batch related operations
@EnableBatchProcessing
public class BatchConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

/*
 * this method created the itemReader 
 * reader() creates an ItemReader.
 *  It looks for a file called sample-data.csv and parses each line item with enough information to turn it into a Person
 */
	@Bean
	public FlatFileItemReader<Person> reader() {
		return new FlatFileItemReaderBuilder<Person>()
			.name("personItemReader")
			.resource(new ClassPathResource("sample-data.csv"))
			.delimited()
			.names(new String[]{"firstName", "lastName"})
			.fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
				setTargetType(Person.class);
			}})
			.build();
	}

	/*
	 * processor() creates an instance of the PersonItemProcessor that you defined earlier, meant to converth the data to upper case.
	 */
	@Bean
	public PersonItemProcessor processor() {
		return new PersonItemProcessor();
	}
	
/*
 * 	
 */

	@Bean
	public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Person>()
			.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
			.sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
			.dataSource(dataSource)
			.build();
	}

	/*
	 * this methods defines job
	 * In this job definition, you need an incrementer, because jobs use a database to maintain execution state. 
	 * You then list each step, (though this job has only one step).
	 *  The job ends, and the Java API produces a perfectly configured job.
	 */
	
	@Bean
	public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
		return jobBuilderFactory.get("importUserJob")
			.incrementer(new RunIdIncrementer())
			.listener(listener)
			.flow(step1)
			.end()
			.build();
	}

	/*
	 * this methods define steps, Jobs are built from steps, where each step can
	 * involve a reader, a processor, and a writer In the step definition, you
	 * define how much data to write at a time. In this case, it writes up to ten
	 * records at a time. Next, you configure the reader, processor, and writer by
	 * using the beans injected earlier.
	 * 
	 * chunk() is prefixed <Person,Person> because it is a generic method. This
	 * represents the input and output types of each “chunk” of processing and lines
	 * up with ItemReader<Person> and ItemWriter<Person>.
	 */
	@Bean
	public Step step1(JdbcBatchItemWriter<Person> writer) {
		return stepBuilderFactory.get("step1")
			.<Person, Person> chunk(10)
			.reader(reader())
			.processor(processor())
			.writer(writer)
			.build();
	}
	// end::jobstep[]
}
