package org.redhat.bpm.wid;

import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class LoadCSV implements WorkItemHandler {

	private static Logger LOG = LoggerFactory.getLogger(LoadCSV.class);
	private final RuntimeManager runtimeManager;

	public LoadCSV(RuntimeManager runtimeManager) {
		this.runtimeManager = runtimeManager;
	}

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		Integer csvHeaderNumber = (Integer) workItem.getParameter("csvHeaderNumber");
		String csvFile = (String) workItem.getParameter("csvFile");

		if(csvFile == null || csvFile.equals("")) {
			LOG.error("LoadCSV can't be executed, no csvFile!");
			manager.abortWorkItem(workItem.getId());
			return;
		}

		List<Set<VariableHolder>> variableHolders = readVariables(csvHeaderNumber, csvFile);
		
		LOG.info("Found {} variables", variableHolders.size());

		HashMap<String,Object> results = new HashMap<String, Object>();
		results.put("variables", variableHolders);
		manager.completeWorkItem(workItem.getId(), results);
		
	}

	protected List<Set<VariableHolder>> readVariables(Integer csvHeaderNumber, String csvFile) {
		List<Set<VariableHolder>> result = new ArrayList<>();
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("csv/" + csvFile).getFile());

		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			List<String> variablesName = new ArrayList<>(csvHeaderNumber);
			int count = 0;
			while ((line = br.readLine()) != null) {
				//headers
				if(count == 0) {
					for(int tmp = 0; tmp < csvHeaderNumber; tmp++)
						variablesName.add(line.split(",")[tmp]);
				}
				if(count != 0) {
					Set<VariableHolder> content = new HashSet<>();
					for(int tmp = 0; tmp < csvHeaderNumber; tmp++) {
						content.add(new VariableHolder(variablesName.get(tmp), line.split(",")[tmp]));
					}
					result.add(content);
				}
				count++;
			}
		} catch (FileNotFoundException e) {
			LOG.warn("File not existing {}", csvFile);
		} catch (IOException e) {
			LOG.warn("Can't load File {}", csvFile);
		}
		return result;
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		throw new UnsupportedOperationException();
	}
	
}
