package com.seeyon.v3x.htmlToPdf;

import com.jfinal.kit.StrKit;
//import com.xixudi.focus.log.FocusLogger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author YangZheng 328170112@qq.com
 * @desc 执行命令的帮助类
 * @date 2019-02-22 21:46
 */
public class CommandHelper {
    //这样就不能适配其他slf4j的实现了,但是为了打印进度,...
//    private static final FocusLogger log = (FocusLogger) LoggerFactory.getLogger(CommandHelper.class);
    private ProcessBuilder pb;
    private String command;
    private String[] envp;
    private File dir;
    private String[] cmdarray;

    public CommandHelper(String command, String[] envp, String dirPath) {
        this.envp = envp;
        if (dirPath != null) dir = new File(dirPath);
        if (command.length() == 0) throw new IllegalArgumentException("Empty command");
        this.command = command;
        StringTokenizer st = new StringTokenizer(command);
        cmdarray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++)
            cmdarray[i] = st.nextToken();
        getPb();
    }

    public CommandHelper(String[] cmdarray, String[] envp, String dirPath) {
        this.envp = envp;
        if (dirPath != null) dir = new File(dirPath);
        this.cmdarray = cmdarray;
        this.command = StrKit.join(cmdarray);
        getPb();
    }

    public ProcessBuilder getPb() {
        if (pb == null) {
            pb = new ProcessBuilder(cmdarray);
            Map<String, String> env = pb.environment();
            if (envp != null) {
                for (String envstring : envp) {
                    if (envstring.indexOf((int) '\u0000') != -1) envstring = envstring.replaceFirst("\u0000.*", "");
                    int eqlsign = envstring.indexOf('=', 1);
                    if (eqlsign != -1) env.put(envstring.substring(0, eqlsign), envstring.substring(eqlsign + 1));
                }
            }
            pb.redirectErrorStream(true);//重定向错误流到输入流,一起打印,防止缓存满
            pb.directory(dir);
        }
        return pb;
    }

    public void run() {
//        log.info("command:{} dir:{}", command, dir);
        BufferedReader reader = null;
        try {
            Process p = getPb().start();
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s = null;
            int a = 0;
            while ((s = reader.readLine()) != null) {
                //直接打印
//                if(!"".equals(s)) log.info(s);
                //按进度打印,但是会破坏彩色,所以加sleep.原因未知.可以选择不sleep
                if (a > 0) {
                    String join = String.join("", Collections.nCopies(a + 80, "\b"));
//                    log.infoExpert(false, false, join);
                    a = 0;
                }
                if (!"".equals(s)) {
                    if (s.matches("[^\\n]*\\[[=*>\\s]+\\][^\\n]*")) {
                        a = s.length();
//                        log.infoExpert(true, false, s);
//                        Thread.sleep(300);
                    } else {
//                        log.info(s);
                    }
                }
            }
            p.waitFor();
//            log.info("execute end");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void run(String command, String[] envp, String dir) {
        new CommandHelper(command, envp, dir).run();
    }
}
