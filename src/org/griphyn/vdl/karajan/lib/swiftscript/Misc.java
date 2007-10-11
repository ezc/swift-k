package org.griphyn.vdl.karajan.lib.swiftscript;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;
import org.griphyn.vdl.karajan.lib.SwiftArg;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.NoSuchTypeException;
import org.griphyn.vdl.type.Types;

public class Misc extends FunctionsCollection {

	private static final Logger logger = Logger.getLogger(FunctionsCollection.class);

	public static final SwiftArg PA_INPUT = new SwiftArg.Positional("input");
	public static final SwiftArg PA_PATTERN = new SwiftArg.Positional("regexp");
	public static final SwiftArg PA_TRANSFORM = new SwiftArg.Positional("transform");

	static {
		setArguments("swiftscript_trace", new Arg[] { Arg.VARGS });
		setArguments("swiftscript_strcat", new Arg[] { Arg.VARGS });
		setArguments("swiftscript_strcut", new Arg[] { PA_INPUT, PA_PATTERN });
		setArguments("swiftscript_regexp", new Arg[] { PA_INPUT, PA_PATTERN, PA_TRANSFORM });
	}

	private static final Logger traceLogger = Logger.getLogger("org.globus.swift.trace");
	public DSHandle swiftscript_trace(VariableStack stack) throws ExecutionException, NoSuchTypeException,
			InvalidPathException {
		Object[] args = SwiftArg.VARGS.asArray(stack);
		StringBuffer buf = new StringBuffer();
		buf.append("SwiftScript trace: ");
		for (int i = 0; i < args.length; i++) {
			if(i!=0) buf.append(", ");
			buf.append(TypeUtil.toString(args[i]));
		}
		traceLogger.warn(buf);
		return null;
	}

	public DSHandle swiftscript_strcat(VariableStack stack) throws ExecutionException, NoSuchTypeException,
			InvalidPathException {
		Object[] args = SwiftArg.VARGS.asArray(stack);
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < args.length; i++) {
			buf.append(TypeUtil.toString(args[i]));
		}
		DSHandle handle = new RootDataNode(Types.STRING);
		handle.setValue(buf.toString());
		handle.closeShallow();
		return handle;
	}

	public DSHandle swiftscript_strcut(VariableStack stack) throws ExecutionException, NoSuchTypeException,
			InvalidPathException {
		String inputString = TypeUtil.toString(PA_INPUT.getValue(stack));
		String pattern = TypeUtil.toString(PA_PATTERN.getValue(stack));
		if (logger.isDebugEnabled()) {
			logger.debug("strcut will match '" + inputString + "' with pattern '" + pattern + "'");
		}

		String group;
		try {
			Pattern p = Pattern.compile(pattern);
			// TODO probably should memoize this?

			Matcher m = p.matcher(inputString);
			m.find();
			group = m.group(1);
		}
		catch (IllegalStateException e) {
			throw new ExecutionException("@strcut could not match pattern " + pattern
					+ " against string " + inputString, e);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("strcut matched '" + group + "'");
		}
		DSHandle handle = new RootDataNode(Types.STRING);
		handle.setValue(group);
		handle.closeShallow();
		return handle;
	}

	public DSHandle swiftscript_regexp(VariableStack stack) throws ExecutionException, NoSuchTypeException,
			InvalidPathException {
		String inputString = TypeUtil.toString(PA_INPUT.getValue(stack));
		String pattern = TypeUtil.toString(PA_PATTERN.getValue(stack));
		String transform = TypeUtil.toString(PA_TRANSFORM.getValue(stack));
		if (logger.isDebugEnabled()) {
			logger.debug("regexp will match '" + inputString + "' with pattern '" + pattern + "'");
		}

		String group;
		try {
			Pattern p = Pattern.compile(pattern);
			// TODO probably should memoize this?

			Matcher m = p.matcher(inputString);
			m.find();
			group = m.replaceFirst(transform);
		}
		catch (IllegalStateException e) {
			throw new ExecutionException("@regexp could not match pattern " + pattern
					+ " against string " + inputString, e);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("regexp replacement produced '" + group + "'");
		}
		DSHandle handle = new RootDataNode(Types.STRING);
		handle.setValue(group);
		handle.closeShallow();
		return handle;
	}
}

