package com.airm2m.luat_dev.handlers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class MergeBin extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ComBin combin=new ComBin();
		combin.Merge(true);
		return null;
	}
}
