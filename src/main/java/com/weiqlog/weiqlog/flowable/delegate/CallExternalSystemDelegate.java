package com.weiqlog.weiqlog.flowable.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * @author hwq
 * @date 2022/12/15
 */
public class CallExternalSystemDelegate implements JavaDelegate {

    public void execute(DelegateExecution execution) {
        System.out.println("Calling the external system for employee "
                + execution.getVariable("employee"));
    }

}