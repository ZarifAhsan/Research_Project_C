import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        String filePathSP = "/Users/Zarif/IdeaProjects/Research_Project_C/src/CSV/SP_500.csv";
        String filePathVIX = "/Users/Zarif/IdeaProjects/Research_Project_C/src/CSV/VIX.csv";

        String[][] spData = loadCSV(filePathSP);
        String[][] vixData = loadCSV(filePathVIX);

        List<Double> closesSP = new ArrayList<>();
        List<Double> closesVIX = new ArrayList<>();

        int i = 0, j = 0;
        while (i < spData.length && j < vixData.length) {

            String dateSP = spData[i][0];
            String dateVIX = vixData[j][0];

            if (dateSP.equals(dateVIX)) {
                closesSP.add(Double.parseDouble(spData[i][1]));
                closesVIX.add(Double.parseDouble(vixData[j][1]));
                i++;
                j++;
            } else if (dateSP.compareTo(dateVIX) < 0) {
                i++;
            } else {
                j++;
            }
        }

        List<Double> spChanges = getPercentChanges(closesSP);
        List<Double> vixChanges = getPercentChanges(closesVIX);

        List<Integer> upUpDays = new ArrayList<>();
        for (int k = 0; k < spChanges.size(); k++) {
            if (spChanges.get(k) > 0 && vixChanges.get(k) > 0) {
                upUpDays.add(k);
            }
        }

        List<Streak> streaks = findStreaks(upUpDays);

        System.out.println("Consecutive Up–Up Days | # Cases | Avg +3-day SPX Return | Win Rate");
        System.out.println("---------------------------------------------------------------");

        for (int streakLen = 1; streakLen <= 5; streakLen++) {
            List<Integer> starts = new ArrayList<>();

            for (Streak s : streaks) {
                if (s.length == streakLen) starts.add(s.startIndex);
            }

            if (starts.isEmpty()) continue;
            int[] DayForward = {1, 2, 3, 5, 10};
            for (int z = 0; z < 5; z++) {
                double totalReturn3 = 0;
                int wins3 = 0;

                for (int start : starts) {
                    start+=streakLen;
                    int nextIdx = start + DayForward[z];
                    if (nextIdx < closesSP.size()) {
                        double futureReturn = (closesSP.get(nextIdx) - closesSP.get(start))
                                / closesSP.get(start) * 100.0;

                        totalReturn3 += futureReturn;
                        if (futureReturn > 0) wins3++;
                    }else {
                        double futureReturn = (closesSP.getLast() - closesSP.getFirst())
                                / closesSP.get(start) * 100.0;

                        totalReturn3 += futureReturn;
                        if (futureReturn > 0) wins3++;

                    }
                }

                int total = starts.size();
                double avg3 = totalReturn3 / total;
                double winRate3 = (wins3 * 100.0) / total;

                System.out.printf("%19d | %2d | %6d | %+8.2f%%             | %.1f%%\n",
                        streakLen,DayForward[z], total, avg3, winRate3);
            }
        }
    }

    private static String[][] loadCSV(String path) {
        try {
            List<String> lines = Files.readAllLines(new File(path).toPath());
            lines.removeFirst();

            String[][] data = new String[lines.size()][2];

            for (int i = 0; i < lines.size(); i++) {
                String[] fields = lines.get(i).split(",");
                data[i][0] = fields[0];
                data[i][1] = fields[1];
            }
            return data;

        } catch (Exception e) {
            System.err.println("Error loading CSV: " + e.getMessage());
            return new String[0][0];
        }
    }

    private static List<Double> getPercentChanges(List<Double> closes) {
        List<Double> changes = new ArrayList<>();
        for (int i = 1; i < closes.size(); i++) {
            double pct = (closes.get(i) - closes.get(i - 1))
                    / closes.get(i - 1) * 100.0;
            changes.add(pct);
        }
        return changes;
    }

    private static List<Streak> findStreaks(List<Integer> upUpDays) {
        List<Streak> streaks = new ArrayList<>();
        if (upUpDays.isEmpty()) return streaks;

        int length = 1;
        for (int i = 1; i < upUpDays.size(); i++) {
            if (upUpDays.get(i) == upUpDays.get(i - 1) + 1) {
                length++;
            } else {
                int start = upUpDays.get(i - length);
                streaks.add(new Streak(start, length));
                length = 1;
            }
        }
        streaks.add(new Streak(upUpDays.get(upUpDays.size() - length), length));
        return streaks;
    }

}