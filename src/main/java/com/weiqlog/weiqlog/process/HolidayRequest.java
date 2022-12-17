package com.weiqlog.weiqlog.process;

import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
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
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    }

    public static void test(){

        // https://tkjohn.github.io/flowable-userguide/#getting.started.delegate
        /**
         * org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration：流程引擎独立运行。Flowable自行处理事务。在默认情况下，数据库检查只在引擎启动时进行（如果Flowable表结构不存在或表结构版本不对，会抛出异常）。
         *
         * org.flowable.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration：这是一个便于使用单元测试的类。Flowable自行处理事务。默认使用H2内存数据库。数据库会在引擎启动时创建，并在引擎关闭时删除。使用这个类时，很可能不需要更多的配置（除了使用任务执行器或邮件等功能时）。
         *
         * org.flowable.spring.SpringProcessEngineConfiguration：在流程引擎处于Spring环境时使用。查看Spring集成章节了解更多信息。
         *
         * org.flowable.engine.impl.cfg.JtaProcessEngineConfiguration：用于引擎独立运行，并使用JTA事务的情况。
         */
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1")
                .setJdbcUsername("sa")
                .setJdbcPassword("")
                .setJdbcDriver("org.h2.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        ProcessEngine processEngine = cfg.buildProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("holiday-request.bpmn20.xml")
                .deploy();
        System.out.println(deployment.getId());
        // 10001
        start(processEngine);
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

    public void getDuration(ProcessEngine processEngine, ProcessInstance processInstance){
        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricActivityInstance> activities =
                historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(processInstance.getId())
                        .finished()
                        .orderByHistoricActivityInstanceEndTime().asc()
                        .list();

        for (HistoricActivityInstance activity : activities) {
            System.out.println(activity.getActivityId() + " took "
                    + activity.getDurationInMillis() + " milliseconds");
        }
    }

    public static void deletedDeployment(ProcessEngine processEngine, String deploymentId){
        RepositoryService repositoryService = processEngine.getRepositoryService();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        repositoryService.deleteDeployment(deploymentId);
    }
}
