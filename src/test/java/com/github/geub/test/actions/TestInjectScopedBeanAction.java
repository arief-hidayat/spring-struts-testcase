package com.github.geub.test.actions;

import org.junit.Rule;
import org.junit.Test;

import com.github.geub.sstc.annotations.StrutsAction;
import com.github.geub.sstc.rule.SpringMockStrutsRule;

public class TestInjectScopedBeanAction {

	@Rule
	public SpringMockStrutsRule springMockStrutsRule = new SpringMockStrutsRule();

	@Test
	@StrutsAction(requestPathInfo = "/injectScopeBean", forward = "success", forwardPath = "/inject/beanScopedBean.jsp")
	public void testInjectionOfScopedBeansSucessFull() {
		this.springMockStrutsRule.prepareAction();
	}

}
