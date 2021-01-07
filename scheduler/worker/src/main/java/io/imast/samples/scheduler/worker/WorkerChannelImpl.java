package io.imast.samples.scheduler.worker;

import io.imast.core.client.ReactiveBaseClient;
import io.imast.core.discovery.DiscoveryClient;
import io.imast.core.scheduler.JobDefinition;
import io.imast.core.scheduler.JobStatus;
import io.imast.core.scheduler.agent.AgentDefinition;
import io.imast.core.scheduler.agent.AgentHealth;
import io.imast.core.scheduler.api.WorkerChannel;
import io.imast.core.scheduler.exchange.JobMetadataRequest;
import io.imast.core.scheduler.exchange.JobMetadataResponse;
import io.imast.core.scheduler.exchange.JobStatusExchangeRequest;
import io.imast.core.scheduler.exchange.JobStatusExchangeResponse;
import io.imast.core.scheduler.iterate.JobIteration;
import java.util.Optional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The implementation of worker channel.
 * This implementation assumes there is a setup service called controller based on REST protocol.
 * You can implement based on your needs.
 * 
 * @author davitp
 */
public class WorkerChannelImpl extends ReactiveBaseClient implements WorkerChannel {

    /**
     * Creates new worker channel
     * 
     * @param discoveryClient The discovery client
     */
    public WorkerChannelImpl(DiscoveryClient discoveryClient){
        super("worker", "controller", discoveryClient, null);
    }
    
    /**
     * A method to load metadata 
     * 
     * @param request The metadata request
     * @return Returns metadata response
     */
    @Override
    public Optional<JobMetadataResponse> metadata(JobMetadataRequest request) {
        
        // build URL
        var url = UriComponentsBuilder
                .fromUriString(this.getApiUrl("api/v1/jobs/_metadata"))
                .build()
                .toUriString();
        
        // get the mono stream
        return this.webClient
                .post()
                .uri(url)
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(JobMetadataResponse.class).blockOptional();   
    }

    /**
     * A method to do status exchange
     * 
     * @param status The current status to exchange
     * @return Returns status exchange result
     */
    @Override
    public Optional<JobStatusExchangeResponse> statusExchange(JobStatusExchangeRequest status) {
        // build URL
        var url = UriComponentsBuilder
                .fromUriString(this.getApiUrl("api/v1/jobs/_exchange"))
                .build()
                .toUriString();
        
        // get the mono stream
        return this.webClient
                .post()
                .uri(url)
                .body(BodyInserters.fromValue(status))
                .retrieve()
                .bodyToMono(JobStatusExchangeResponse.class).blockOptional();     
    }

    /**
     * Create an iteration in controller
     * 
     * @param iteration The iteration to insert
     * @return Returns inserted iteration
     */
    @Override
    public Optional<JobIteration> iterate(JobIteration iteration) {
        
        // build URL
        var url = UriComponentsBuilder
                .fromUriString(String.format(this.getApiUrl("api/v1/jobs/%s/_iterations"), iteration.getJobId()))
                .build()
                .toUriString();
        
        // get the mono stream
        return this.webClient
                .post()
                .uri(url)
                .body(BodyInserters.fromValue(iteration))
                .retrieve()
                .bodyToMono(JobIteration.class).blockOptional();     
    }

    /**
     * A method to change status of job 
     * 
     * @param id The job id
     * @param status The job status
     * @return Returns modified definition
     */
    @Override
    public Optional<JobDefinition> markAs(String id, JobStatus status) {
        // build URL
        var url = UriComponentsBuilder
                .fromUriString(String.format(this.getApiUrl("api/v1/jobs/%s/_status?status=%s"), id, status))
                .build()
                .toUriString();
        
        // get the mono stream
        return this.webClient
                .put()
                .uri(url)
                .retrieve()
                .bodyToMono(JobDefinition.class).blockOptional();  
    }

    /**
     * Add agent definition into the system
     * 
     * @param agent The agent to add
     * @return Returns added agent
     */
    @Override
    public Optional<AgentDefinition> registration(AgentDefinition agent) {

        // build URL
        var url = UriComponentsBuilder
                .fromUriString(this.getApiUrl("api/v1/agents"))
                .build()
                .toUriString();
        
        // get the mono stream
        return this.webClient
                .post()
                .uri(url)
                .body(BodyInserters.fromValue(agent))
                .retrieve()
                .bodyToMono(AgentDefinition.class).blockOptional();      
    }

    /**
     * Add heartbeat to agent
     * 
     * @param id The agent id
     * @param health The new health info
     * @return Returns modified agent
     */
    @Override
    public Optional<AgentDefinition> heartbeat(String id, AgentHealth health) {
        // build URL
        var url = UriComponentsBuilder
                .fromUriString(this.getApiUrl(String.format("api/v1/agents/%s/health", id)))
                .build()
                .toUriString();
        
        // get the mono stream
        return this.webClient
                .put()
                .uri(url)
                .body(BodyInserters.fromValue(health))
                .retrieve()
                .bodyToMono(AgentDefinition.class).blockOptional();
    }
}