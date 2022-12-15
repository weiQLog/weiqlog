package com.weiqlog.weiqlog.process;

import org.flowable.engine.*;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author hwq
 * @date 2022/12/15
 */
public class HolidayRequest {
    public static void main(String[] args) {
        // https://tkjohn.github.io/flowable-userguide/#getting.started.delegate
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:mysql://127.0.0.1:3306/flowable?useSSL=false&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&transformedBitIsBoolean=true&tinyInt1isBit=false&allowMultiQueries=true&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true")
                .setJdbcUsername("root")
                .setJdbcPassword("1qaz@WSX")
                .setJdbcDriver("com.mysql.cj.jdbc.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        ProcessEngine processEngine = cfg.buildProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
//        Deployment deployment = repositoryService.createDeployment()
//                .addClasspathResource("holiday-request.bpmn20.xml")
//                .deploy();
//        System.out.println(deployment.getId());
//        start(processEngine)；
        taskList(processEngine);
    }

    /**
     * 开始
     * @param processEngine
     */
    public static void start( ProcessEngine processEngine){

        Scanner scanner= new Scanner(System.in);

        System.out.println("Who are you?");
        String employee = scanner.nextLine();

        System.out.println("How many holidays do you want to request?");
        Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());

        System.out.println("Why do you need them?");
        String description = scanner.nextLine();

        RuntimeService runtimeService = processEngine.getRuntimeService();

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("employee", employee);
        variables.put("nrOfHolidays", nrOfHolidays);
        variables.put("description", description);
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey("holidayRequest", variables);
        System.out.println(processInstance.getId());
        System.out.println(processInstance.getProcessInstanceId());
        System.out.println(processInstance.getProcessDefinitionId());
        System.out.println(processInstance.getProcessDefinitionKey());
    }

    public static void taskList(ProcessEngine processEngine){
        Scanner scanner= new Scanner(System.in);
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
        System.out.println("You have " + tasks.size() + " tasks:");
        for (int i=0; i<tasks.size(); i++) {
            System.out.println((i+1) + ") " + tasks.get(i).getName());
        }
        System.out.println("Which task would you like to complete?");
        int taskIndex = Integer.parseInt(scanner.nextLine());
        Task task = tasks.get(taskIndex - 1);
        Map<String, Object> processVariables = taskService.getVariables(task.getId());
        System.out.println(processVariables.get("employee") + " wants " +
                processVariables.get("nrOfHolidays") + " of holidays. Do you approve this?");
        boolean approved = scanner.nextLine().toLowerCase().equals("y");
        processVariables = new HashMap<String, Object>();
        processVariables.put("approved", approved);
        taskService.complete(task.getId(), processVariables);

    }

    public static void deletedDeployment(ProcessEngine processEngine, String deploymentId){
        RepositoryService repositoryService = processEngine.getRepositoryService();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        repositoryService.deleteDeployment(deploymentId);
    }
}
