package com.polaris.workflow;

import org.apache.dubbo.config.annotation.Service;

import com.polaris.workflow.api.service.WorkflowService;
import com.polaris.workflow.entry.WorkflowEntry;

@Service(version = "1.0.0")
public class ApplicationDubbo extends WorkflowEntry implements WorkflowService{
}