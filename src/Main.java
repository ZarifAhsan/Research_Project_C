import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        String filePathSP = "/Users/Zarif/IdeaProjects/Research_Project_C/src/CSV/SP_500.csv";
        String filePathVIX = "/Users/Zarif/IdeaProjects/Research_Project_C/src/CSV/VIX.csv";

        File fileSP = new File(filePathSP);
        File fileVIX = new File(filePathVIX);

        String[][] csvDataSP = new String[][]{csvToString(fileSP).toArray(new String[0])};
        String[][] csvDataVIX = new String[][]{csvToString(fileVIX).toArray(new String[0])};


        for (String[] row : csvDataSP) {
            System.out.println(Arrays.toString(row));
        }
        for (String[] row : csvDataVIX) {
            System.out.println(Arrays.toString(row));
        }

    }

    private static List<String> csvToString(File csv) {
        List<String> records = new ArrayList<>();

        try (Scanner scanner = new Scanner(csv)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] values = line.split(",");
                String closing = values[1];
                records.add(closing);
            }
        } catch (FileNotFoundException e) {
            System.err.println("CSV file not found: " + e.getMessage());
        }
        return records;
    }



}