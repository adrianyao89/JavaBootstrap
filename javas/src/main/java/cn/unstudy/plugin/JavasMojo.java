package cn.unstudy.plugin;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adrian Yao on 2016/9/13.
 */

/**
 * @goal package
 * @phase package
 * @phase process-sources
 */
public class JavasMojo extends AbstractMojo {

    /**
     * @parameter expression="${project.basedir}"
     * @required
     * @readonly
     */
    private File basedir;

    /**
     * @parameter expression="${project.build.directory}"
     * @required
     * @readonly
     */
    private File builddir;

    /**
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     * @readonly
     */
    private File sourcedir;
    /**
     * @parameter expression="${project.resources}"
     * @required
     * @readonly
     */
    private List<Resource> resources;

    /**
     * @parameter expression="${javas.output}"
     * @required
     */
    private String output;

    /**
     * @parameter expression="${javas.mainclass}"
     * @required
     */
    private String mainclass;
    /**
     * @parameter expression="${javas.vmoptions}"
     * @required
     */
    private String vmoptions;

    public static void copyFile(File sourceFile, File targetFile)
            throws IOException {
        // 新建文件输入流并对它进行缓冲
        FileInputStream input = new FileInputStream(sourceFile);
        BufferedInputStream inBuff = new BufferedInputStream(input);

        // 新建文件输出流并对它进行缓冲
        FileOutputStream output = new FileOutputStream(targetFile);
        BufferedOutputStream outBuff = new BufferedOutputStream(output);

        // 缓冲数组
        byte[] b = new byte[1024 * 5];
        int len;
        while ((len = inBuff.read(b)) != -1) {
            outBuff.write(b, 0, len);
        }
        // 刷新此缓冲的输出流
        outBuff.flush();

        //关闭流
        inBuff.close();
        outBuff.close();
        output.close();
        input.close();
    }

    public static void copyDirectiory(String sourceDir, String targetDir)
            throws IOException {
        // 新建目标目录
        (new File(targetDir)).mkdirs();
        // 获取源文件夹当前下的文件或目录
        File[] file = (new File(sourceDir)).listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                // 源文件
                File sourceFile = file[i];
                // 目标文件
                File targetFile = new
                        File(new File(targetDir).getAbsolutePath()
                        + File.separator + file[i].getName());
                copyFile(sourceFile, targetFile);
            }
            if (file[i].isDirectory()) {
                // 准备复制的源文件夹
                String dir1 = sourceDir + "/" + file[i].getName();
                // 准备复制的目标文件夹
                String dir2 = targetDir + "/" + file[i].getName();
                copyDirectiory(dir1, dir2);
            }
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("javas打包开始");

            String builddirPath = builddir.getPath();
            String javasDir = builddirPath + "/" + output;
            File javas = new File(javasDir);

            if (javas.exists()) {
                javas.delete();
                getLog().info("删除就版本输出目录" + javas.getPath());
            }

            getLog().info("创建输出目录" + javas.getPath());
            javas.mkdir();


            //创建app.info文件
            File appinfo = new File(javasDir + "/app.info");
            FileOutputStream fos = new FileOutputStream(appinfo);
            fos.write(("mainclass:" + mainclass.trim()).getBytes());
            fos.write("\r\n".getBytes());
            fos.write(("vmoptions: " + vmoptions.trim()).getBytes());
            fos.flush();
            fos.close();

            getLog().info("创建app.info完成");

            getLog().info("开始复制class文件到bin目录...");
            //复制编译的class文件到bin目录
            List<String> sources = new ArrayList<String>();
            List<String> sourceFiles = new ArrayList<String>();
            File[] sourcecdir = sourcedir.listFiles();
            for (File f : sourcecdir) {
                if (f.isDirectory()) {
                    sources.add(f.getName());
                }
                if (f.isFile()) {
                    sourceFiles.add(f.getName());
                }
            }

            String bindir = javasDir + "/bin";
            File binDir = new File(bindir);
            if (!binDir.exists()) {
                binDir.mkdir();
                getLog().info("创建bin目录 " + bindir);
            }

            for (String source : sources) {
                copyDirectiory(builddirPath + "/classes/" + source, bindir + "/" + source);
                getLog().info("拷贝 目录 target/classes/" + source + "到 目录 target/" +  output + "/bin/" + source);
            }

            for (String sourceFile : sourceFiles) {
                File f = new File(builddirPath + "/classes/" + sourceFile);
                if (f.exists()) {
                    copyFile(f , new File(bindir + "/" + sourceFile));
                    getLog().info("拷贝 文件 target/classes/" + f.getName() + "到 文件target/" +  output + "/bin/" + sourceFile);
                }
            }
            getLog().info("完成复制class文件到bin目录");

            getLog().info("开始复制resources下配置文件到config目录...");
            //复制配置文件到config目录
            List<String> recourcesFilePath = new ArrayList<String>();
            List<String> recourcesDirPath = new ArrayList<String>();

            for (Resource res : resources) {
                File r = new File(res.getDirectory());
                for (File f : r.listFiles()) {
                    if (f.isFile()) {
                        recourcesFilePath.add(f.getName());
                    } else if (f.isDirectory()) {
                        recourcesDirPath.add(f.getName());
                    }

                }
            }

            String configdir = javasDir + "/config";
            File configDir = new File(configdir);
            if (!configDir.exists()) {
                configDir.mkdir();
                getLog().info("创建config目录 " + configdir);
            }

            for (String recourceDirPath : recourcesDirPath) {
                copyDirectiory(builddirPath + "/classes/" + recourceDirPath, configdir + "/" + recourceDirPath);
                getLog().info("拷贝 目录 target/classes/" + recourceDirPath + "到 目录 target/" +  output + "/config/" + recourceDirPath);
            }

            for (String resouceFilePath : recourcesFilePath) {
                File f = new File(builddirPath + "/classes/" + resouceFilePath);
                if (f.exists()) {
                    copyFile(f , new File(configdir + "/" + resouceFilePath));
                    getLog().info("拷贝 文件 target/classes/" + f.getName() + "到 文件target/" +  output + "/config/" + resouceFilePath);
                }
            }
            getLog().info("完成复制resources下配置文件到config目录");

            //复制依赖包到lib目录
            copyDirectiory(builddirPath + "/lib", javasDir + "/lib");
            getLog().info("完成复制依赖包到lib目录");

            //创建logs目录
            File logs = new File(javasDir + "/logs");
            logs.mkdir();
            getLog().info("完成创建logs目录");

            FileToZip.zip(javasDir, builddirPath + "/" + output + ".zip");
            getLog().info("javas打包完成");

        } catch (Exception e) {
            getLog().error("javas打包发送错误", e);
        }

    }
}
