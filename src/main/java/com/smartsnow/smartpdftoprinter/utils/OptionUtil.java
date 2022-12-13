package com.smartsnow.smartpdftoprinter.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 此选项已经包含h(help)、c(channel_id)、s(proc_id)、z(zkRoot)、k(zkConnectList)、i(haId)、e(once)的处理 vf:n:s:b:z:k:i:e
 */
public class OptionUtil {
	private static Logger logger = LoggerFactory.getLogger(OptionUtil.class);
	private Options options = new Options();
	private CommandLineParser parser = new DefaultParser();
	private CommandLineResult commandLineResult = null;
	private String moduleName = "moduleName";
	private HelpFormatter formatter = new HelpFormatter();

	public class CommandLineResult {
		private CommandLine commandLine = null;

		public CommandLineResult(CommandLine commandLine) {
			this.commandLine = commandLine;
		}

		public boolean hasOption(String opt) {
			return commandLine.hasOption(opt);
		}

		public boolean hasOption(char opt) {
			return commandLine.hasOption(opt);
		}

		public String[] getOptionValues(String opt) {
			return commandLine.getOptionValues(opt);
		}

		public String[] getOptionValues(char opt) {
			return commandLine.getOptionValues(opt);
		}

		public String getOptionValue(String opt) {
			return commandLine.getOptionValue(opt);
		}

		public String getOptionValue(char opt) {
			return commandLine.getOptionValue(opt);
		}
	}

	public CommandLineResult getCommandLine() {
		return commandLineResult;
	}

	/** 加入不带参数的选项 */
	public void addOptWithNoArg(String opt, String longOpt, String optDescription, boolean isOptional) {
		options.addOption(Option.builder(opt).longOpt(longOpt).desc(optDescription).optionalArg(isOptional).build());
	}

	/**
	 * 加入带一个参数的选项，如-n 20 sep为value多值时的分隔符
	 */
	public void addOptWithOneArg(String opt, String longOpt, String optDescription, boolean isOptional,
			String argName) {
		options.addOption(Option.builder(opt).longOpt(longOpt).desc(optDescription).optionalArg(isOptional).hasArg()
				.valueSeparator().argName(argName).build());
	}

	public void addOptWithOneArg(String opt, String longOpt, String optDescription, boolean isOptional, String argName,
			char sep) {
		options.addOption(Option.builder(opt).longOpt(longOpt).desc(optDescription).optionalArg(isOptional).hasArg()
				.valueSeparator(sep).argName(argName).numberOfArgs(Option.UNLIMITED_VALUES).build());
	}

	/**
	 * 加入带两个或两个以上参数的选项，如-Dkey=value,每个key=value有两个参数，一个是key,一个是value，sep为key和value的分隔符
	 */
	public void addOptWithMultiArg(String opt, String longOpt, String optDescription, boolean isOptional,
			String argName) {
		options.addOption(Option.builder(opt).longOpt(longOpt).desc(optDescription).optionalArg(isOptional).hasArgs()
				.valueSeparator().argName(argName).build());
	}

	public void addOptWithMultiArg(String opt, String longOpt, String optDescription, boolean isOptional,
			String argName, char sep) {
		options.addOption(Option.builder(opt).longOpt(longOpt).desc(optDescription).optionalArg(isOptional).hasArgs()
				.valueSeparator(sep).argName(argName).numberOfArgs(Option.UNLIMITED_VALUES).build());
	}

	public void parseOpt(String moduleName, String[] args) {
		this.moduleName = moduleName;// "hvf:n:c:s:b:k:z:i:"
		addOptWithNoArg("h", "help", "print help information", false);
		addOptWithNoArg("v", "version", "version", false);
		addOptWithOneArg("f", "configurefile", "configure file  full path", false, "configure-file");
		addOptWithOneArg("n", "runname", "change app runnable name", false, moduleName);
		addOptWithOneArg("c", "channelid", "module instance id", false, "channel-id");
		addOptWithOneArg("s", "subchannelid", "actually is proccess instance id", false, "sub-channel-id");
		addOptWithNoArg("e", "once", "execute once and auto-exit", false);
//		addOptWithOneArg("b", "biztype", "app use this to catelogry situation,appfrm not use this", false, "");
//		addOptWithOneArg("k", "zkconnectlist", "zk connect list string", false, "zk-connect-list");
//		addOptWithOneArg("z", "zkroot", "zkroot,such as /roam-gsm", false, "zkroot");
//		addOptWithOneArg("i", "haid", "haId,such as roam-deal-app1.roam,come from TD_HOST_HA", false, "ha-id");

		try {
			CommandLine commandLine = parser.parse(options, args);
			if (commandLine == null) {
				logger.error("fail to parse options:{}", options.toString());
				System.exit(0);
			}
			commandLineResult = new CommandLineResult(commandLine);
		} catch (ParseException e) {
			logger.error("fail to parse opt:", e);
			System.out.println(e);
			System.exit(0);
		}
		if (commandLineResult.hasOption("h")) {
			help();
			System.exit(0);
		}
		if (commandLineResult.hasOption("v")) {
			version();
			System.exit(0);
		}
	}

	public void help() {
		formatter.printHelp(moduleName, options, true);
	}

	public void version() {
		System.out.println("==================================================");
		System.out.println("System Name: AI-NJ.025.ANTAB.D1\r\n"
				+ "-------------------------------------------------\r\n" + "Version    : V01.001.000\r\n"
				+ "-------------------------------------------------\r\n" + "build  : 1\r\n" + "update : 2005/01/05\r\n"
				+ "comment: Default Version Information.\r\n" + "appname: NBilling\r\n"
				+ "author : AI NBilling System Development Team\r\n" + "declare: AI All right reserved.");
		System.out.println("==================================================");

	}

	/////////////////////////////////////////
	// "hvf:n:c:s:b:o:z:" 定制化api
	public boolean hasHelpOpt() {
		return commandLineResult.hasOption('h');
	}

	public boolean hasVersionOpt() {
		return commandLineResult.hasOption('v');
	}
	public String getValue(String opt) {
		return getValue(opt,null);
	}
	public String getValue(String opt,String defaultValue) {
		if (commandLineResult.hasOption(opt))
			return commandLineResult.commandLine.getOptionValue(opt);
		else
			return defaultValue;
	}
	public String getConfigureFile(String defaultValue) {
		char opt = 'f';
		if (commandLineResult.hasOption(opt))
			return commandLineResult.commandLine.getOptionValue(opt);
		else
			return defaultValue;
	}

	public String getRunName(String defaultValue) {
		char opt = 'n';
		if (commandLineResult.hasOption(opt))
			return commandLineResult.commandLine.getOptionValue(opt);
		else
			return defaultValue;
	}

	public String getChannelId(String defaultValue) {
		char opt = 'c';
		if (commandLineResult.hasOption(opt))
			return commandLineResult.commandLine.getOptionValue(opt);
		else
			return defaultValue;
	}

	public String getSubChannelId(String defaultValue) {
		char opt = 's';
		if (commandLineResult.hasOption(opt))
			return commandLineResult.commandLine.getOptionValue(opt);
		else
			return defaultValue;
	}
	
	public boolean isExecuteOnce(boolean defaultValue) {
		char opt = 'e';
		if (commandLineResult.hasOption(opt))
			return true;
		else
			return defaultValue;
	}

//	public String getBizType(String defaultValue) {
//		char opt = 'b';
//		if (commandLineResult.hasOption(opt))
//			return commandLineResult.commandLine.getOptionValue(opt);
//		else
//			return defaultValue;
//	}
//
//	public String getZkConnectList(String defaultValue) {
//		char opt = 'k';
//		if (commandLineResult.hasOption(opt))
//			return commandLineResult.commandLine.getOptionValue(opt);
//		else
//			return defaultValue;
//	}
//
//	public String getZkRootPath(String defaultValue) {
//		char opt = 'z';
//		if (commandLineResult.hasOption(opt))
//			return commandLineResult.commandLine.getOptionValue(opt);
//		else
//			return defaultValue;
//	}
//	
//	public String getHaId(String defaultValue) {
//		char opt = 'i';
//		if (commandLineResult.hasOption(opt))
//			return commandLineResult.commandLine.getOptionValue(opt);
//		else
//			return defaultValue;
//	}
}
