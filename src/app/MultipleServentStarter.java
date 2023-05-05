package app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class implements the logic for starting multiple servent instances.
 * <p>
 * To use it, invoke startServentTest with a directory name as parameter.
 * This directory should include:
 * <ul>
 * <li>A <code>servent_list.properties</code> file (explained in {@link AppConfig} class</li>
 * <li>A directory called <code>output</code> </li>
 * <li>A directory called <code>error</code> </li>
 * <li>A directory called <code>input</code> with text files called
 * <code> servent0_in.txt </code>, <code>servent1_in.txt</code>, ... and so on for each servent.
 * These files should contain the commands for each servent, as they would be entered in console.</li>
 * </ul>
 *
 * @author bmilojkovic
 */
public class MultipleServentStarter {

    /**
     * We will wait for user stop in a separate thread.
     * The main thread is waiting for processes to end naturally.
     */
    private static class ServentCLI implements Runnable {

        private final List<Process> serventProcesses;

        public ServentCLI(List<Process> serventProcesses) {
            this.serventProcesses = serventProcesses;
        }

        @Override
        public void run() {
            Scanner sc = new Scanner(System.in);

            while (true) {
                String line = sc.nextLine();

                if (line.equals("stop")) {
                    for (Process process : serventProcesses) {
                        process.destroy();
                    }
                    break;
                }
            }

            sc.close();
        }
    }

    /**
     * The parameter for this function should be the name of a directory that
     * contains a servent_list.properties file which will describe our distributed system.
     */
    private static void startServentTest(String snapshotFolderName) {
        List<Process> serventProcesses = new ArrayList<>();

        AppConfig.readConfig(snapshotFolderName + "/servent_list.properties");

        AppConfig.timestampedStandardPrint("Starting multiple servent runner. If servents do not finish on their own, type \"stop\" to finish them");

        int serventCount = AppConfig.getServentCount();

        for (int i = 0; i < serventCount; i++) {
            try {
                ProcessBuilder builder = new ProcessBuilder("java", "-cp", "out\\production\\KiDS-Domaci2-kostur", "app.ServentMain",
                        snapshotFolderName + "/servent_list.properties", String.valueOf(i));

                //We use files to read and write.
                //System.out, System.err and System.in will point to these files.
                builder.redirectOutput(new File(snapshotFolderName + "/output/servent" + i + "_out.txt"));
                builder.redirectError(new File(snapshotFolderName + "/error/servent" + i + "_err.txt"));
                builder.redirectInput(new File(snapshotFolderName + "/input/servent" + i + "_in.txt"));

                //Starts the servent as a completely separate process.
                Process p = builder.start();
                serventProcesses.add(p);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Thread t = new Thread(new ServentCLI(serventProcesses));
        t.start(); //CLI thread waiting for user to type "stop".

        for (Process process : serventProcesses) {
            try {
                process.waitFor(); //Wait for graceful process finish.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        AppConfig.timestampedStandardPrint("All servent processes finished. Type \"stop\" to exit.");
    }

    public static void main(String[] args) {
//        startServentTest("ab_snapshot");
        startServentTest("av_snapshot");
    }

}
