
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class case1 {

    public static void main(String[] args) {

        runAll("odev1_case1.txt", "case1");
        runAll("odev1_case2.txt", "case2");

        System.out.println("Tum algoritmalar icin ciktilar hazirlandi.");
    }

    static void runAll(String inputFile, String tag) {

        ArrayList<MyProcess> processList = readProcesses(inputFile);

        fcfs(copyList(processList), tag);
        sjfPreemptive(copyList(processList), tag);
        sjfNonPreemptive(copyList(processList), tag);
        roundRobin(copyList(processList), 2, tag);
        priorityPreemptive(copyList(processList), tag);
        priorityNonPreemptive(copyList(processList), tag);
    }

    static ArrayList<MyProcess> readProcesses(String fileName) {

        ArrayList<MyProcess> list = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            br.readLine(); // header

            String line;
            while ((line = br.readLine()) != null) {

                String[] data = line.split(",");

                MyProcess p = new MyProcess();
                p.id = data[0];
                p.arrivalTime = Integer.parseInt(data[1]);
                p.burstTime = Integer.parseInt(data[2]);
                p.remainingTime = p.burstTime;

                if (data[3].trim().equalsIgnoreCase("high"))
                    p.priority = 3;
                else if (data[3].trim().equalsIgnoreCase("normal"))
                    p.priority = 2;
                else
                    p.priority = 1;

                list.add(p);
            }

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    static ArrayList<MyProcess> copyList(ArrayList<MyProcess> original) {

        ArrayList<MyProcess> temp = new ArrayList<>();

        for (int i = 0; i < original.size(); i++) {
            MyProcess o = original.get(i);
            MyProcess n = new MyProcess();

            n.id = o.id;
            n.arrivalTime = o.arrivalTime;
            n.burstTime = o.burstTime;
            n.remainingTime = o.burstTime;
            n.priority = o.priority;

            temp.add(n);
        }
        return temp;
    }

    static void summary(FileWriter fw, ArrayList<MyProcess> list,
                        int totalTime, int contextSwitch) throws Exception {

        int totalWaiting = 0;
        int totalTurnaround = 0;
        int maxWaiting = 0;
        int maxTurnaround = 0;

        for (MyProcess p : list) {

            p.turnaroundTime = p.finishTime - p.arrivalTime;
            p.waitingTime = p.turnaroundTime - p.burstTime;

            totalWaiting += p.waitingTime;
            totalTurnaround += p.turnaroundTime;

            if (p.waitingTime > maxWaiting)
                maxWaiting = p.waitingTime;

            if (p.turnaroundTime > maxTurnaround)
                maxTurnaround = p.turnaroundTime;
        }

        fw.write("\nWaiting Time\n");
        fw.write("Average: " + (totalWaiting / (double) list.size()) + "\n");
        fw.write("Maximum: " + maxWaiting + "\n");

        fw.write("\nTurnaround Time\n");
        fw.write("Average: " + (totalTurnaround / (double) list.size()) + "\n");
        fw.write("Maximum: " + maxTurnaround + "\n");

        fw.write("\nThroughput\n");
        int[] times = {50, 100, 150, 200};

        for (int t : times) {
            int count = 0;
            for (MyProcess p : list)
                if (p.finishTime <= t)
                    count++;
            fw.write("T=" + t + " -> " + count + "\n");
        }

        double cpuUtil = (totalTime / (totalTime + contextSwitch * 0.001)) * 100;
        fw.write("\nCPU Utilization: %" + cpuUtil + "\n");
        fw.write("Total Context Switch: " + contextSwitch + "\n");
    }

    static void fcfs(ArrayList<MyProcess> list, String tag) {

        list.sort((a, b) -> a.arrivalTime - b.arrivalTime);

        try {
            FileWriter fw = new FileWriter("FCFS_" + tag + ".txt");
            fw.write("FCFS Zaman Tablosu\n");

            int time = 0;
            int cs = 0;

            for (MyProcess p : list) {

                if (time < p.arrivalTime) {
                    fw.write("[" + time + "] -- IDLE -- [" + p.arrivalTime + "]\n");
                    time = p.arrivalTime;
                }

                fw.write("[" + time + "] -- " + p.id +
                        " -- [" + (time + p.burstTime) + "]\n");

                time += p.burstTime;
                p.finishTime = time;
                cs++;
            }

            summary(fw, list, time, cs);
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void sjfPreemptive(ArrayList<MyProcess> list, String tag) {

        try {
            FileWriter fw = new FileWriter("SJF_Preemptive_" + tag + ".txt");
            fw.write("SJF Preemptive Zaman Tablosu\n");

            int time = 0;
            int finished = 0;
            int cs = 0;
            MyProcess current = null;

            while (finished < list.size()) {

                MyProcess shortest = null;

                for (MyProcess p : list)
                    if (p.arrivalTime <= time && p.remainingTime > 0)
                        if (shortest == null || p.remainingTime < shortest.remainingTime)
                            shortest = p;

                if (shortest == null) {
                    time++;
                    continue;
                }

                if (current != shortest) {
                    fw.write("[" + time + "] -- " + shortest.id + "\n");
                    current = shortest;
                    cs++;
                }

                shortest.remainingTime--;
                time++;

                if (shortest.remainingTime == 0) {
                    shortest.finishTime = time;
                    finished++;
                }
            }

            summary(fw, list, time, cs);
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void sjfNonPreemptive(ArrayList<MyProcess> list, String tag) {

        try {
            FileWriter fw = new FileWriter("SJF_NonPreemptive_" + tag + ".txt");
            fw.write("SJF Non-Preemptive Zaman Tablosu\n");

            int time = 0;
            int cs = 0;
            ArrayList<MyProcess> completed = new ArrayList<>();

            while (completed.size() < list.size()) {

                MyProcess shortest = null;

                for (MyProcess p : list)
                    if (!completed.contains(p) && p.arrivalTime <= time)
                        if (shortest == null || p.burstTime < shortest.burstTime)
                            shortest = p;

                if (shortest == null) {
                    time++;
                    continue;
                }

                fw.write("[" + time + "] -- " + shortest.id +
                        " -- [" + (time + shortest.burstTime) + "]\n");

                time += shortest.burstTime;
                shortest.finishTime = time;
                completed.add(shortest);
                cs++;
            }

            summary(fw, list, time, cs);
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void roundRobin(ArrayList<MyProcess> list, int quantum, String tag) {

        try {
            FileWriter fw = new FileWriter("RR_" + tag + ".txt");
            fw.write("Round Robin Zaman Tablosu\n");

            int time = 0;
            int cs = 0;
            boolean allDone;

            do {
                allDone = true;

                for (MyProcess p : list) {

                    if (p.remainingTime > 0 && p.arrivalTime <= time) {

                        allDone = false;
                        int slice = Math.min(quantum, p.remainingTime);

                        fw.write("[" + time + "] -- " + p.id +
                                " -- [" + (time + slice) + "]\n");

                        time += slice;
                        p.remainingTime -= slice;
                        cs++;

                        if (p.remainingTime == 0)
                            p.finishTime = time;
                    }
                }

            } while (!allDone);

            summary(fw, list, time, cs);
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void priorityPreemptive(ArrayList<MyProcess> list, String tag) {

        try {
            FileWriter fw = new FileWriter("Priority_Preemptive_" + tag + ".txt");
            fw.write("Priority Preemptive Zaman Tablosu\n");

            int time = 0;
            int finished = 0;
            int cs = 0;
            MyProcess current = null;

            while (finished < list.size()) {

                MyProcess selected = null;

                for (MyProcess p : list)
                    if (p.arrivalTime <= time && p.remainingTime > 0)
                        if (selected == null || p.priority > selected.priority)
                            selected = p;

                if (selected == null) {
                    time++;
                    continue;
                }

                if (current != selected) {
                    fw.write("[" + time + "] -- " + selected.id + "\n");
                    current = selected;
                    cs++;
                }

                selected.remainingTime--;
                time++;

                if (selected.remainingTime == 0) {
                    selected.finishTime = time;
                    finished++;
                }
            }

            summary(fw, list, time, cs);
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void priorityNonPreemptive(ArrayList<MyProcess> list, String tag) {

        try {
            FileWriter fw = new FileWriter("Priority_NonPreemptive_" + tag + ".txt");
            fw.write("Priority Non-Preemptive Zaman Tablosu\n");

            int time = 0;
            int cs = 0;
            ArrayList<MyProcess> completed = new ArrayList<>();

            while (completed.size() < list.size()) {

                MyProcess selected = null;

                for (MyProcess p : list)
                    if (!completed.contains(p) && p.arrivalTime <= time)
                        if (selected == null || p.priority > selected.priority)
                            selected = p;

                if (selected == null) {
                    time++;
                    continue;
                }

                fw.write("[" + time + "] -- " + selected.id +
                        " -- [" + (time + selected.burstTime) + "]\n");

                time += selected.burstTime;
                selected.finishTime = time;
                completed.add(selected);
                cs++;
            }

            summary(fw, list, time, cs);
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

