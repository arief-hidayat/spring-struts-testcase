package com.github.geub.sstc.rule;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionServlet;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import servletunit.HttpServletRequestSimulator;

import com.github.geub.sstc.annotations.StrutsAction;
import com.github.geub.sstc.mock.SpringMockStrutsTestCase;

public class SpringMockStrutsRule extends TestWatcher {

	private ActionServlet actionServlet;
	private SpringMockStrutsTestCase springMockStrutsTestCase = new SpringMockStrutsTestCase();
	private boolean actionExecutedAlready = false;
	private StrutsStatus strutsStatus = StrutsStatus.STARTED;

	public SpringMockStrutsRule(ActionServlet customActionServlet) {
		this.actionServlet = customActionServlet;
	}

	public SpringMockStrutsRule() {
		this(new ActionServlet());
	}

	@Override
	protected void starting(Description description) {
		try {
			validateStrutsStatus(StrutsStatus.STARTED);
			StrutsAction strutsAction = getStrutsActionAnnotation(description);
			this.springMockStrutsTestCase.setUp(strutsAction.requestPath(), this.actionServlet);
			this.strutsStatus = StrutsStatus.CONFIGURED;
			if (strutsAction.prepareAction()) {
				prepareAction();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void succeeded(Description description) {
		if (!this.actionExecutedAlready) {
			doAction();
		}
		StrutsAction strutsActionAnnotation = getStrutsActionAnnotation(description);
		this.springMockStrutsTestCase.verifyForward(strutsActionAnnotation.expectedForward(), strutsActionAnnotation.expectedForwardPath());
	}

	@Override
	protected void finished(Description description) {
		this.springMockStrutsTestCase.endRequest();
	}

	private StrutsAction getStrutsActionAnnotation(Description description) {
		return description.getAnnotation(StrutsAction.class);
	}

	public Action getAction() {
		return this.springMockStrutsTestCase.getAction();
	}

	public HttpServletRequest getRequest() {
		return this.springMockStrutsTestCase.getRequest();
	}

	public void setCookies(Cookie... cookies) {
		getRequestSimulator().setCookies(cookies);
	}

	public void addCookie(String key, String value) {
		getRequestSimulator().addCookie(new Cookie(key, value));
	}

	private HttpServletRequestSimulator getRequestSimulator() {
		return (HttpServletRequestSimulator) getRequest();
	}

	public void prepareAction() {
		validateStrutsStatus(StrutsStatus.CONFIGURED);
		this.springMockStrutsTestCase.actionPerform();
		this.strutsStatus = StrutsStatus.REQUEST_PROCESSED;
	}

	public HttpServletResponse doAction() {
		validateStrutsStatus(StrutsStatus.REQUEST_PROCESSED);
		this.springMockStrutsTestCase.executeAction();
		this.strutsStatus = StrutsStatus.ACTION_EXECUTED;
		return this.springMockStrutsTestCase.getResponse();
	}

	private void validateStrutsStatus(StrutsStatus expected) {
		if (!expected.equals(this.strutsStatus)) {
			throw new IllegalStateException(String.format("Invalid status, should be %s and is %s", expected.toString(), this.strutsStatus.toString()));
		}
	}

}

enum StrutsStatus {
	STARTED,
	CONFIGURED,
	REQUEST_PROCESSED,
	ACTION_EXECUTED

}
