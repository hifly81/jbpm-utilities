package org.redhat.bpm.rules;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;

import java.util.HashMap;
import java.util.Map;

public class ProcessRuleTest extends JbpmJUnitBaseTestCase {
	
	private RuntimeManager runtimeManager;
	private RuntimeEngine runtimeEngine;
	private KieSession kieSession;

	public ProcessRuleTest() {
		super(true, true);
	}
	
	@Before
	public void before() {
		Map<String, ResourceType> res = new HashMap<String, ResourceType>();
		res.put("org/redhat/bpm/rules/process-rule.bpmn2", ResourceType.BPMN2);
		res.put("org/redhat/bpm/rules/MyRule.drl", ResourceType.DRL);
		runtimeManager = createRuntimeManager(res);
		runtimeEngine = getRuntimeEngine();
		kieSession = runtimeEngine.getKieSession();
	}
	
	@After
	public void after() {
		runtimeManager.disposeRuntimeEngine(runtimeEngine);
		runtimeManager.close();
	}

	@Test
	public void test() {
		Person person = new Person();
		person.setFirstName("Giovanni");
		person.setLastName("Marigi");
		person.setHourlyRate(100);
		person.setWage(100);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("person", person);

		ProcessInstance pi = kieSession.startProcess("org.redhat.bpm.rules.process-rule", parameters);
		
		assertProcessInstanceCompleted(pi.getId());
	}
	
	
}
