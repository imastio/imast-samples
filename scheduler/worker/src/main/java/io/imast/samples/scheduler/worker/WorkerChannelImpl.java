package io.imast.samples.scheduler.worker;

import io.imast.core.client.ReactiveBaseClient;
import io.imast.core.discovery.DiscoveryClient;
import io.imast.work4j.channel.SchedulerChannel;
import io.imast.work4j.model.execution.CompletionSeverity;
import io.imast.work4j.model.execution.ExecutionIndexEntry;
import io.imast.work4j.model.execution.ExecutionStatus;
import io.imast.work4j.model.execution.ExecutionUpdateInput;
import io.imast.work4j.model.execution.ExecutionsResponse;
import io.imast.work4j.model.execution.JobExecution;
import io.imast.work4j.model.iterate.Iteration;
import io.imast.work4j.model.iterate.IterationInput;
import io.imast.work4j.model.worker.Worker;
import io.imast.work4j.model.worker.WorkerHeartbeat;
import io.imast.work4j.model.worker.WorkerInput;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * The implementation of worker channel.
 * This implementation assumes there is a setup service called controller based on REST protocol.
 * You can implement based on your needs.
 * 
 * @author davitp
 */
public class WorkerChannelImpl extends ReactiveBaseClient implements SchedulerChannel {

    /**
     * Creates new worker channel
     * 
     * @param discoveryClient The discovery client
     */
    public WorkerChannelImpl(DiscoveryClient discoveryClient){
        super("worker", "controller", discoveryClient, null);
    }
    
    /**
     * Pull job groups for the given cluster
     * 
     * @param tenant The target tenant
     * @param cluster The target cluster
     * @return Returns execution index entries
     */
    @Override
    public Mono<List<ExecutionIndexEntry>> executionIndex(String tenant, String cluster){
        // build URL
        var url = UriComponentsBuilder
                .fromUriString(this.getApiUrl("api/v1/scheduler/executions"))
                .queryParam("tenant", tenant)
                .queryParam("cluster", cluster)
                .build()
                .toUriString();
        
        // get the mono stream
        return this.webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(ExecutionIndexEntry[].class)
                .map(Arrays::asList);
    }
    
    /**
     * Exchange current status with modified entries
     * 
     * @param ids The executions request ids
     * @return Returns executions response
     */
    @Override
    public Mono<ExecutionsResponse> executions(List<String> ids){
        // build URL
        var url = UriComponentsBuilder
                .fromUriString(this.getApiUrl("api/v1/scheduler/executions"))
                .queryParam("ids", ids)
                .build()
                .toUriString();
        
        // get the mono stream
        return this.webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(ExecutionsResponse.class);
    }
    
    /**
     * Completes the job execution in scheduler
     * 
     * @param id The identifier of job execution
     * @param severity The severity of completion
     * @return Returns updated job execution
     */
    @Override
    public Mono<JobExecution> complete(String id, CompletionSeverity severity){
        
        // build URL
        var url = UriComponentsBuilder
                .fromUriString(String.format(this.getApiUrl("api/v1/scheduler/executions/%s"), id))
                .build()
                .toUriString();
        
        // the input to submit
        var input = new ExecutionUpdateInput(ExecutionStatus.COMPLETED, severity);
        
        // get the mono stream
        return this.webClient
                .put()
                .uri(url)
                .body(BodyInserters.fromValue(input))
                .retrieve()
                .bodyToMono(JobExecution.class);   
    }
    
    /**
     * Adds iteration information to scheduler
     * 
     * @param iteration The iteration to register
     * @return Returns registered iteration
     */
    @Override
    public Mono<Iteration> iterate(IterationInput iteration){
        // build URL
        var url = UriComponentsBuilder
                .fromUriString(this.getApiUrl("api/v1/scheduler/iterations"))
                .build()
                .toUriString();
        
        // get the mono stream
        return this.webClient
                .post()
                .uri(url)
                .body(BodyInserters.fromValue(iteration))
                .retrieve()
                .bodyToMono(Iteration.class);   
    }
    
    /**
     * Registers worker into the scheduler
     * 
     * @param input The worker input to register
     * @return Returns registered worker
     */
    @Override
    public Mono<Worker> registration(WorkerInput input){
        // build URL
        var url = UriComponentsBuilder
                .fromUriString(this.getApiUrl("api/v1/scheduler/workers"))
                .build()
                .toUriString();
                
        // get the mono stream
        return this.webClient
                .post()
                .uri(url)
                .body(BodyInserters.fromValue(input))
                .retrieve()
                .bodyToMono(Worker.class);  
    }
    
    /**
     * Send a Heartbeat signal to from worker scheduler
     * 
     * @param id The identifier of worker instance
     * @param heartbeat The worker reported heartbeat
     * @return Returns updated agent definition
     */
    @Override
    public Mono<Worker> heartbeat(String id, WorkerHeartbeat heartbeat){
        // build URL
        var url = UriComponentsBuilder
                .fromUriString(this.getApiUrl(String.format("api/v1/scheduler/workers/%s", id)))
                .build()
                .toUriString();
        
        // get the mono stream
        return this.webClient
                .post()
                .uri(url)
                .body(BodyInserters.fromValue(heartbeat))
                .retrieve()
                .bodyToMono(Worker.class);   
    }    
}
