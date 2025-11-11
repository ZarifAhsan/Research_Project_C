//import java.io.File;
//import java.io.FileNotFoundException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Scanner;
//
//public class Main {
//    public static void main(String[] args) {
//
//        String filePathSP = "/Users/Zarif/IdeaProjects/Research_Project_C/src/CSV/SP_500.csv";
//        String filePathVIX = "/Users/Zarif/IdeaProjects/Research_Project_C/src/CSV/VIX.csv";
//
//        File fileSP = new File(filePathSP);
//        File fileVIX = new File(filePathVIX);
//
//        String[][] csvDataSP = new String[][]{csvToString(fileSP).toArray(new String[0])};
//        String[][] csvDataVIX = new String[][]{csvToString(fileVIX).toArray(new String[0])};
//
//
//        for (String[] row : csvDataSP) {
//            System.out.println(Arrays.toString(row));
//        }
//        for (String[] row : csvDataVIX) {
//            System.out.println(Arrays.toString(row));
//        }
//
//    }
//
//    private static List<String> csvToString(File csv) {
//        List<String> records = new ArrayList<>();
//
//        try (Scanner scanner = new Scanner(csv)) {
//            while (scanner.hasNextLine()) {
//                String line = scanner.nextLine();
//                String[] values = line.split(",");
//                String closing = values[1];
//                records.add(closing);
//            }
//        } catch (FileNotFoundException e) {
//            System.err.println("CSV file not found: " + e.getMessage());
//        }
//        return records;
//    }
//
//
//
//}

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // File paths for SPX and VIX
        String filePathSP = "/Users/Zarif/IdeaProjects/Research_Project_C/src/CSV/SP_500.csv";
        String filePathVIX = "/Users/Zarif/IdeaProjects/Research_Project_C/src/CSV/VIX.csv";

        File fileSP = new File(filePathSP);
        File fileVIX = new File(filePathVIX);

        // Convert to doubles
        List<Double> closesSP = toDoubleList(csvToString(fileSP));
        List<Double> closesVIX = toDoubleList(csvToString(fileVIX));

        List<Double> spChanges = getPercentChanges(closesSP);
        List<Double> vixChanges = getPercentChanges(closesVIX);

        List<Integer> upUpDays = new ArrayList<>();
        for (int i = 0; i < spChanges.size(); i++) {
            if (spChanges.get(i) > 0 && vixChanges.get(i) > 0) {
                upUpDays.add(i);
            }
        }

        // Find consecutive Up–Up streaks
        List<Streak> streaks = findStreaks(upUpDays);

        // Evaluate future SPX returns for 1–5 day streaks
        int[] forwardDays = {1, 3, 5, 10, 20};

        System.out.println("Consecutive Up–Up Days | # Cases | Avg +3-day SPX Return | Win Rate");
        System.out.println("---------------------------------------------------------------");

        for (int streakLen = 1; streakLen <= 5; streakLen++) {
            List<Integer> starts = new ArrayList<>();

            // Collect starting indices for this streak length
            for (Streak s : streaks) {
                if (s.length == streakLen) starts.add(s.startIndex);
            }

            if (starts.isEmpty()) continue;

            double totalReturn3 = 0;
            int wins3 = 0;

            for (int start : starts) {
                int nextIdx = start + 3; // 3-day forward return
                if (nextIdx < closesSP.size()) {
                    double futureReturn = (closesSP.get(nextIdx) - closesSP.get(start)) / closesSP.get(start) * 100.0;
                    totalReturn3 += futureReturn;
                    if (futureReturn > 0) wins3++;
                }
            }

            int total = starts.size();
            double avg3 = totalReturn3 / total;
            double winRate3 = (wins3 * 100.0) / total;

            System.out.printf("%19d | %6d | %+8.2f%%             | %.1f%%\n",
                    streakLen, total, avg3, winRate3);
        }
    }

    // Reads only the “Close” column from CSV file
    private static List<String> csvToString(File csv) {
        List<String> records = new ArrayList<>();
        try {
            java.nio.file.Files.lines(csv.toPath())
                    .skip(1)
                    .forEach(line -> {
                        String[] values = line.split(",");
                        if (values.length > 1) records.add(values[1]);
                    });
        } catch (Exception e) {
            System.err.println("CSV file not found: " + e.getMessage());
        }
        return records;
    }

    // Converts List<String> → List<Double>
    private static List<Double> toDoubleList(List<String> list) {
        List<Double> doubles = new ArrayList<>();
        for (String s : list) {
            try {
                doubles.add(Double.parseDouble(s));
            } catch (NumberFormatException ignored) {}
        }
        return doubles;
    }

    // Computes daily percent change list
    private static List<Double> getPercentChanges(List<Double> closes) {
        List<Double> changes = new ArrayList<>();
        for (int i = 1; i < closes.size(); i++) {
            double pct = (closes.get(i) - closes.get(i - 1)) / closes.get(i - 1) * 100.0;
            changes.add(pct);
        }
        return changes;
    }

    // Finds consecutive Up–Up streaks
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

    // Helper Class
    static class Streak {
        int startIndex;
        int length;

        Streak(int startIndex, int length) {
            this.startIndex = startIndex;
            this.length = length;
        }
    }
}
